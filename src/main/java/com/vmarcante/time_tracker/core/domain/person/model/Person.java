package com.vmarcante.time_tracker.core.domain.person.model;

import java.util.UUID;

import com.vmarcante.time_tracker.base.domain.model.BaseAuthDomainModel;
import com.vmarcante.time_tracker.core.shared.vo.Email;
import com.vmarcante.time_tracker.core.shared.vo.Phone;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Person extends BaseAuthDomainModel<UUID, Integer> {

    private String name;
    private Email email;
    private Phone phone;
    private String locale;
}
