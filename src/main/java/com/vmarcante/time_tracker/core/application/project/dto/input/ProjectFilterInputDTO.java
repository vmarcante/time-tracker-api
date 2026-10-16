package com.vmarcante.time_tracker.core.application.project.dto.input;

import com.vmarcante.time_tracker.base.application.dto.input.BaseInputFilterDTO;
import com.vmarcante.time_tracker.core.application.project.enums.ProjectSortType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectFilterInputDTO extends BaseInputFilterDTO<ProjectSortType> {
}
