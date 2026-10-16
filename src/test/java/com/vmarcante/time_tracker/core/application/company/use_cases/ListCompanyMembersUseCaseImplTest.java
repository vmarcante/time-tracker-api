package com.vmarcante.time_tracker.core.application.company.use_cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.vmarcante.time_tracker.base.domain.response.PageWrapperDTO;
import com.vmarcante.time_tracker.core.application.company.dto.input.MembershipFilterInputDTO;
import com.vmarcante.time_tracker.core.application.company.dto.output.CompanyMemberOutputDTO;
import com.vmarcante.time_tracker.core.application.exception.ApplicationException;
import com.vmarcante.time_tracker.core.domain.company.enums.CompanyRole;
import com.vmarcante.time_tracker.core.domain.company.enums.MembershipOrigin;
import com.vmarcante.time_tracker.core.domain.company.model.CompanyMembership;
import com.vmarcante.time_tracker.core.domain.company.repository.CompanyMembershipRepository;
import com.vmarcante.time_tracker.core.domain.person.model.Person;
import com.vmarcante.time_tracker.core.domain.person.repository.PersonRepository;
import com.vmarcante.time_tracker.core.domain.user.auth.port.SecurityContextPort;
import com.vmarcante.time_tracker.core.shared.vo.Email;

class ListCompanyMembersUseCaseImplTest {

    private CompanyMembershipRepository membershipRepository;
    private PersonRepository personRepository;
    private SecurityContextPort securityContext;
    private ListCompanyMembersUseCaseImpl useCase;

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID ACTOR_ID = UUID.randomUUID();
    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final UUID APPROVER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        membershipRepository = mock(CompanyMembershipRepository.class);
        personRepository = mock(PersonRepository.class);
        securityContext = mock(SecurityContextPort.class);
        useCase = new ListCompanyMembersUseCaseImpl(membershipRepository, personRepository, securityContext);
    }

    private CompanyMembership approvedMembership() {
        CompanyMembership membership = new CompanyMembership();
        membership.setId(UUID.randomUUID());
        membership.setUserId(MEMBER_ID);
        membership.setCompanyId(COMPANY_ID);
        membership.setRole(CompanyRole.MEMBER);
        membership.setOrigin(MembershipOrigin.INVITE);
        membership.setApproved(true);
        membership.setApprovedBy(APPROVER_ID);
        return membership;
    }

    private Person personWithEmail(UUID id, String name, String email) {
        Person person = new Person();
        person.setId(id);
        person.setName(name);
        person.setEmail(new Email(email));
        return person;
    }

    @Test
    void givenApprovedMembers_whenExecuting_thenReturnsMembersWithEmail() {
        // Arrange
        CompanyMembership membership = approvedMembership();
        Page<CompanyMembership> page = new PageImpl<>(List.of(membership));
        Person member = personWithEmail(MEMBER_ID, "Jane Doe", "jane@example.com");
        Person approver = personWithEmail(APPROVER_ID, "Admin", "admin@example.com");

        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(ACTOR_ID));
        when(membershipRepository.isMember(ACTOR_ID, COMPANY_ID)).thenReturn(true);
        when(membershipRepository.findApprovedMembershipsByCompanyId(eq(COMPANY_ID), any(Pageable.class)))
                .thenReturn(page);
        when(personRepository.findAllByIds(any())).thenReturn(List.of(member, approver));

        // Act
        PageWrapperDTO<CompanyMemberOutputDTO> result = useCase.execute(COMPANY_ID, new MembershipFilterInputDTO());

        // Assert
        assertThat(result.content()).hasSize(1);
        assertThat(result.pageNumber()).isEqualTo(1);
        assertThat(result.totalElements()).isEqualTo(1);
        CompanyMemberOutputDTO dto = result.content().get(0);
        assertThat(dto.userId()).isEqualTo(MEMBER_ID);
        assertThat(dto.name()).isEqualTo("Jane Doe");
        assertThat(dto.email()).isEqualTo("jane@example.com");
        assertThat(dto.approvedByName()).isEqualTo("Admin");
    }

    @Test
    void givenNoMembers_whenExecuting_thenReturnsEmptyPage() {
        // Arrange
        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(ACTOR_ID));
        when(membershipRepository.isMember(ACTOR_ID, COMPANY_ID)).thenReturn(true);
        when(membershipRepository.findApprovedMembershipsByCompanyId(eq(COMPANY_ID), any(Pageable.class)))
                .thenReturn(Page.empty());

        // Act
        PageWrapperDTO<CompanyMemberOutputDTO> result = useCase.execute(COMPANY_ID, new MembershipFilterInputDTO());

        // Assert
        assertThat(result.content()).isEmpty();
        assertThat(result.empty()).isTrue();
    }

    @Test
    void givenNonMemberActor_whenExecuting_thenDeniesAccess() {
        // Arrange
        when(securityContext.getCurrentUserId()).thenReturn(Optional.of(ACTOR_ID));
        when(membershipRepository.isMember(ACTOR_ID, COMPANY_ID)).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> useCase.execute(COMPANY_ID, new MembershipFilterInputDTO()))
                .isInstanceOf(ApplicationException.class);
    }
}
