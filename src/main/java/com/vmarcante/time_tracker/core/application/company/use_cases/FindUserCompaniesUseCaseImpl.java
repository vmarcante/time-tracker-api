package com.vmarcante.time_tracker.core.application.company.use_cases;

import java.util.Optional;
import java.util.UUID;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyFilterMapper;
import com.vmarcante.time_tracker.core.domain.company.model.filter.CompanyFilter;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.vmarcante.time_tracker.base.application.pagination.PageableUtils;
import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.CompanyFilterInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanySummaryOutputDTO;
import com.vmarcante.time_tracker.core.application.company.in.FindUserCompaniesUseCase;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;

@Service
public class FindUserCompaniesUseCaseImpl implements FindUserCompaniesUseCase {

    private final CompanyRepository companyRepository;
    private final SecurityContextPort securityContext;

    public FindUserCompaniesUseCaseImpl(
            CompanyRepository companyRepository,
            SecurityContextPort securityContext) {
        this.companyRepository = companyRepository;
        this.securityContext = securityContext;
    }

    @Override
    public PageWrapperDTO<CompanySummaryOutputDTO> execute(CompanyFilterInputDTO filter)
            throws ApplicationException {
        Optional<UUID> currentUserId = securityContext.getCurrentUserId();
        if (currentUserId.isEmpty()) {
            throw new ApplicationException("user.authenticated.not", null);
        }

        Pageable pageable = PageableUtils.pageable(filter.getPageNumber(), filter.getPageSize(), filter.getSort());

        CompanyFilter companyFilter = CompanyFilterMapper.activeMemberOf(currentUserId.get());
        return PageWrapperDTO.of(companyRepository
                .search(companyFilter, pageable)
                .map(CompanySummaryOutputDTO::from));
    }
}
