package com.mattelogic.inchfab.core.repository;

import com.mattelogic.inchfab.core.entity.Company;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

  @Query("SELECT c FROM Company c LEFT JOIN FETCH c.projects WHERE c.id = :companyId")
  Optional<Company> findByIdWithProjects(@Param("companyId") Long companyId);
}
