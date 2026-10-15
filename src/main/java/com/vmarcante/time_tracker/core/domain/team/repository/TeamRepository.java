package com.vmarcante.time_tracker.core.domain.team.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.team.model.Team;

public interface TeamRepository {

    Team save(Team team);

    Optional<Team> findById(UUID id);

    List<Team> findAllByIds(Collection<UUID> teamIds);

    Page<Team> findActiveByCompanyId(UUID companyId, Pageable pageable);

    Page<Team> findActiveTeamsByUserId(UUID userId, UUID companyId, Pageable pageable);

    Page<Team> findActiveByProjectId(UUID projectId, Pageable pageable);

    boolean existsActiveByIdAndCompanyId(UUID teamId, UUID companyId);

    boolean existsActiveByCompanyIdAndName(UUID companyId, String name);
}
