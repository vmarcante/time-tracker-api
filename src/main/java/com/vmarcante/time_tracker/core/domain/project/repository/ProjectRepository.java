package com.vmarcante.time_tracker.core.domain.project.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.project.model.Project;

public interface ProjectRepository {

    Project save(Project project);

    Optional<Project> findById(UUID id);

    Page<Project> findActiveByCompanyId(UUID companyId, Pageable pageable);

    Page<Project> findActiveByUserId(UUID userId, Pageable pageable);

    Page<Project> findActiveProjectsByUserId(UUID userId, UUID companyId, Pageable pageable);

    Page<Project> findActiveByTeamId(UUID teamId, Pageable pageable);

    boolean existsActiveByIdAndCompanyId(UUID projectId, UUID companyId);

    boolean existsActiveByCompanyIdAndName(UUID companyId, String name);

    boolean existsActiveByUserIdAndName(UUID userId, String name);

    Map<UUID, String> findNamesByIds(Collection<UUID> projectIds);

    List<Project> findAllByIds(Collection<UUID> projectIds);
}
