package com.vmarcante.time_tracker.core.infraestructure.project.mapper;

import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.ProjectAssignment;
import com.vmarcante.time_tracker.core.domain.project.model.TeamProject;
import com.vmarcante.time_tracker.core.infraestructure.project.persistence.ProjectJpaEntity;
import com.vmarcante.time_tracker.core.infraestructure.project.persistence.ProjectTeamJpaEntity;
import com.vmarcante.time_tracker.core.infraestructure.project.persistence.UserProjectJpaEntity;

public class ProjectPersistenceMapper {

    public static Project toDomain(ProjectJpaEntity entity) {
        Project project = new Project();
        project.setId(entity.getId());
        project.setSeqId(entity.getSeqId());
        project.setCompanyId(entity.getCompanyId());
        project.setUserId(entity.getUserId());
        project.setName(entity.getName());
        project.setClientName(entity.getClientName());
        project.setDescription(entity.getDescription());
        project.setStatus(entity.getStatus());
        project.setStartDate(entity.getStartDate());
        project.setEndDate(entity.getEndDate());
        project.setActive(entity.getActive());
        project.setCreatedAt(entity.getCreatedAt());
        project.setUpdatedAt(entity.getUpdatedAt());
        project.setCreatedBy(entity.getCreatedBy());
        project.setUpdatedBy(entity.getUpdatedBy());
        return project;
    }

    public static ProjectJpaEntity toEntity(Project project) {
        ProjectJpaEntity entity = new ProjectJpaEntity();
        entity.setId(project.getId());
        entity.setCompanyId(project.getCompanyId());
        entity.setUserId(project.getUserId());
        entity.setName(project.getName());
        entity.setClientName(project.getClientName());
        entity.setDescription(project.getDescription());
        entity.setStatus(project.getStatus());
        entity.setStartDate(project.getStartDate());
        entity.setEndDate(project.getEndDate());
        entity.setActive(project.getActive());
        entity.setCreatedBy(project.getCreatedBy());
        entity.setUpdatedBy(project.getUpdatedBy());
        return entity;
    }

    public static TeamProject toDomain(ProjectTeamJpaEntity entity) {
        TeamProject link = new TeamProject();
        link.setId(entity.getId());
        link.setSeqId(entity.getSeqId());
        link.setProjectId(entity.getProjectId());
        link.setTeamId(entity.getTeamId());
        link.setActive(entity.getActive());
        link.setCreatedAt(entity.getCreatedAt());
        link.setUpdatedAt(entity.getUpdatedAt());
        link.setCreatedBy(entity.getCreatedBy());
        link.setUpdatedBy(entity.getUpdatedBy());
        return link;
    }

    public static ProjectTeamJpaEntity toEntity(TeamProject link) {
        ProjectTeamJpaEntity entity = new ProjectTeamJpaEntity();
        entity.setId(link.getId());
        entity.setProjectId(link.getProjectId());
        entity.setTeamId(link.getTeamId());
        entity.setActive(link.getActive());
        entity.setCreatedBy(link.getCreatedBy());
        entity.setUpdatedBy(link.getUpdatedBy());
        return entity;
    }

    public static ProjectAssignment toDomain(UserProjectJpaEntity entity) {
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setId(entity.getId());
        assignment.setSeqId(entity.getSeqId());
        assignment.setUserId(entity.getUserId());
        assignment.setProjectId(entity.getProjectId());
        assignment.setTeamId(entity.getTeamId());
        assignment.setActive(entity.getActive());
        assignment.setCreatedAt(entity.getCreatedAt());
        assignment.setUpdatedAt(entity.getUpdatedAt());
        assignment.setCreatedBy(entity.getCreatedBy());
        assignment.setUpdatedBy(entity.getUpdatedBy());
        return assignment;
    }

    public static UserProjectJpaEntity toEntity(ProjectAssignment assignment) {
        UserProjectJpaEntity entity = new UserProjectJpaEntity();
        entity.setId(assignment.getId());
        entity.setUserId(assignment.getUserId());
        entity.setProjectId(assignment.getProjectId());
        entity.setTeamId(assignment.getTeamId());
        entity.setActive(assignment.getActive());
        entity.setCreatedBy(assignment.getCreatedBy());
        entity.setUpdatedBy(assignment.getUpdatedBy());
        return entity;
    }
}
