package com.mattelogic.inchfab.base.repository;

import com.mattelogic.inchfab.base.entity.Target;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TargetRepository extends MongoRepository<Target, String> {

  @Aggregation(pipeline = {
      "{ $match: { name: ?0 } }",
      "{ $project: { totalValue: { $toDouble: { $getField: ?1 } } } }"
  })
  Optional<Double> findFieldByName(String name, String fieldName);
}