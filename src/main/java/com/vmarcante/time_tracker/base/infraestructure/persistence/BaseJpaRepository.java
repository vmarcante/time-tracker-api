package com.vmarcante.time_tracker.base.infraestructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseJpaRepository<T, ID, SEQ> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    Optional<T> findById(ID id);

    Optional<T> findBySeqId(SEQ seqId);

    boolean existsById(ID id);

    boolean existsBySeqId(SEQ seqId);
}
