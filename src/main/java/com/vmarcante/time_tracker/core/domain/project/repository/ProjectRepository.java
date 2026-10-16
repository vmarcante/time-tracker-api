package com.vmarcante.time_tracker.core.domain.project.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.project.model.Project;
import com.vmarcante.time_tracker.core.domain.project.model.filter.ProjectFilter;

public interface ProjectRepository {

    Project save(Project project);

    Optional<Project> findById(UUID id);

    Optional<Project> findOne(ProjectFilter filter);

    List<Project> search(ProjectFilter filter);

    Page<Project> search(ProjectFilter filter, Pageable pageable);

    boolean exists(ProjectFilter filter);

    Map<UUID, String> findNamesByIds(Collection<UUID> projectIds);
}
