package com.vmarcante.time_tracker.core.domain.team.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.core.domain.team.model.Team;
import com.vmarcante.time_tracker.core.domain.team.model.filter.TeamFilter;

public interface TeamRepository {

    Team save(Team team);

    Optional<Team> findById(UUID id);

    Optional<Team> findOne(TeamFilter filter);

    List<Team> search(TeamFilter filter);

    Page<Team> search(TeamFilter filter, Pageable pageable);

    boolean exists(TeamFilter filter);
}
