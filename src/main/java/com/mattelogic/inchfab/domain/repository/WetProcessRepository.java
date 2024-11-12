package com.mattelogic.inchfab.domain.repository;

import com.mattelogic.inchfab.base.repository.EffectiveLatentRepository;
import com.mattelogic.inchfab.domain.entity.WetProcess;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Wet Process configurations in MongoDB. Provides custom
 * aggregation queries for retrieving specific field values and settings related to wet chemical
 * processing operations.
 */
@Repository
public interface WetProcessRepository extends EffectiveLatentRepository<WetProcess> {

  @Aggregation(pipeline = {
      "{ $match: { name: ?0 } }",
      "{ $project: { totalValue: { $toDouble: { $getField: { input: '$clean', field: ?1 } } } } }"
  })
  Optional<Double> findCleanFieldByName(String name, String fieldName);
}