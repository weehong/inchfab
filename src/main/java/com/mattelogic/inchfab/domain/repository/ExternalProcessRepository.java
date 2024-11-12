package com.mattelogic.inchfab.domain.repository;

import com.mattelogic.inchfab.domain.entity.ExternalProcess;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalProcessRepository extends MongoRepository<ExternalProcess, String> {

  @Aggregation(pipeline = {
      "{ $match: { name: ?0 } }",
      "{ $project: { value: { $toDouble: { $getField: ?1 } } } }"
  })
  Optional<Double> findFieldByName(String name, String fieldName);

  @Aggregation(pipeline = {
      "{ $match: { name: ?0 } }",
      "{ $unwind: '$pricing' }",
      "{ $match: { 'pricing.quantity': ?1 } }",
      "{ $project: { unitCost: '$pricing.unitCost' } }"
  })
  Optional<Double> findUnitCostByNameAndQuantity(String name, Integer quantity);
}