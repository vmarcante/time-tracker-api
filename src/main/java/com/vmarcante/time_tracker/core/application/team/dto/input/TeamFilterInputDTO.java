package com.vmarcante.time_tracker.core.application.team.dto.input;

import com.vmarcante.time_tracker.base.application.dto.input.BaseInputFilterDTO;
import com.vmarcante.time_tracker.core.application.team.enums.TeamSortType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamFilterInputDTO extends BaseInputFilterDTO<TeamSortType> {
}
