package com.vmarcante.time_tracker.base.application.dto.input;

import com.vmarcante.time_tracker.core.shared.interfaces.SortType;

import lombok.Data;

@Data
public class BaseInputFilterDTO<S extends SortType> {
    private Integer pageNumber = 1;
    private Integer pageSize = 20;
    private S sort;
}
