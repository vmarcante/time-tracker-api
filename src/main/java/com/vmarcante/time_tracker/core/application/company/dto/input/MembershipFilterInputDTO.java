package com.vmarcante.time_tracker.core.application.company.dto.input;

import com.vmarcante.time_tracker.base.application.dto.input.BaseInputFilterDTO;
import com.vmarcante.time_tracker.core.application.company.enums.MembershipSortType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MembershipFilterInputDTO extends BaseInputFilterDTO<MembershipSortType> {
}
