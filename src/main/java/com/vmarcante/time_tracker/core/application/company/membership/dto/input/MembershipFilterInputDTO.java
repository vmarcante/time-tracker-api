package com.vmarcante.time_tracker.core.application.company.membership.dto.input;

import com.vmarcante.time_tracker.base.application.dto.input.BaseInputFilterDTO;
import com.vmarcante.time_tracker.core.application.company.membership.enums.MembershipSortType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MembershipFilterInputDTO extends BaseInputFilterDTO<MembershipSortType> {
}
