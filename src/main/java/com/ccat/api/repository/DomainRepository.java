package com.ccat.api.repository;

import com.ccat.api.model.entity.Domain;
import com.ccat.api.model.enums.DomainStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DomainRepository extends JpaRepository<Domain, String> {
    List<Domain> findByEmStatusOrderByIntSortOrderAsc(DomainStatus status);
    List<Domain> findAllByOrderByIntSortOrderAsc();
    boolean existsByStrDomainCode(String strDomainCode);
}
