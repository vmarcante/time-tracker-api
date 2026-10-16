package com.vmarcante.time_tracker.core.application.company.overview.dto.output;

import java.util.List;

import com.vmarcante.time_tracker.core.application.company.membership.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.project.dto.output.ProjectSummaryOutputDTO;

public record CompanyOverviewOutputDTO(
        long membersCount,
        long pendingMembershipsCount,
        long teamsCount,
        long projectsCount,
        List<CompanyMemberOutputDTO> recentMembers,
        List<CompanyMemberOutputDTO> pendingRequests,
        List<ProjectSummaryOutputDTO> recentProjects) {
}
