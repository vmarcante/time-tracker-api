package com.vmarcante.time_tracker.core.application.company.dto.input;

import com.vmarcante.time_tracker.base.application.dto.input.BaseInputFilterDTO;
import com.vmarcante.time_tracker.core.application.company.enums.CompanySortType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyFilterInputDTO extends BaseInputFilterDTO<CompanySortType> {
}
