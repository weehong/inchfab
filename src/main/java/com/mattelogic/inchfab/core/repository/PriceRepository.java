package com.mattelogic.inchfab.core.repository;

import com.mattelogic.inchfab.core.entity.Price;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

  @Query("SELECT p FROM Price p WHERE p.project.id = :projectId")
  Optional<Price> findByProjectId(@Param("projectId") Long projectId);
}
