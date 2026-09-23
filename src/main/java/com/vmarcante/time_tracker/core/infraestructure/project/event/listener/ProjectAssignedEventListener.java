package com.vmarcante.time_tracker.core.infraestructure.project.event.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.vmarcante.time_tracker.core.domain.email.model.EmailData;
import com.vmarcante.time_tracker.core.domain.email.service.EmailService;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.project.event.ProjectAssignedEvent;
import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.repository.ProjectRepository;
import com.vmarcante.time_tracker.core.domain.team.repository.TeamRepository;
import com.vmarcante.time_tracker.core.shared.utils.StringValidationUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProjectAssignedEventListener {

    private final EmailService emailService;
    private final PersonRepository personRepository;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final String frontendUrl;

    public ProjectAssignedEventListener(
            EmailService emailService,
            PersonRepository personRepository,
            ProjectRepository projectRepository,
            TeamRepository teamRepository,
            @Value("${app.frontend.url:https://app.timetracker.com}") String frontendUrl) {
        this.emailService = emailService;
        this.personRepository = personRepository;
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.frontendUrl = frontendUrl;
    }

    @Async
    @EventListener
    public void handleProjectAssigned(ProjectAssignedEvent event) {
        try {
            Person member = personRepository.findById(event.getUserId()).orElse(null);
            if (member == null || member.getEmail() == null
                    || !StringValidationUtils.containsContent(member.getEmail().address())) {
                log.warn("[Project Assigned Event] Member person not found or without email | User: {}",
                        event.getUserId());
                return;
            }

            Project project = projectRepository.findAllByIds(List.of(event.getProjectId()))
                    .stream().findFirst().orElse(null);
            String teamName = teamRepository.findById(event.getTeamId())
                    .map(t -> t.getName())
                    .orElse("");

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("name", member.getName());
            templateData.put("projectName", project != null ? project.getName() : "");
            templateData.put("clientName", project != null ? project.getClientName() : "");
            templateData.put("teamName", teamName);
            templateData.put("projectLink",
                    frontendUrl + "/companies/" + event.getCompanyId() + "/projects/" + event.getProjectId());

            boolean isPortuguese = "pt".equalsIgnoreCase(member.getLocale());
            String subject = isPortuguese
                    ? "Você foi atribuído a um projeto - Time Tracker"
                    : "You were assigned to a project - Time Tracker";
            Locale locale = isPortuguese
                    ? Locale.forLanguageTag("pt-BR")
                    : Locale.forLanguageTag("en-US");

            emailService.sendEmail(new EmailData(
                    member.getEmail().address(), subject, "project_assigned", templateData, locale));

            log.info("[Project Assigned Event] Assignment email sent | User: {} | Project: {}",
                    event.getUserId(), event.getProjectId());
        } catch (Exception e) {
            log.error("[Project Assigned Event] Failed to send assignment email | User: {}",
                    event.getUserId(), e);
        }
    }
}
