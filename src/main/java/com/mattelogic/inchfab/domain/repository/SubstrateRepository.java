package com.mattelogic.inchfab.domain.repository;

import com.mattelogic.inchfab.domain.entity.Substrate;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubstrateRepository extends MongoRepository<Substrate, String> {

  @Aggregation(pipeline = {
      "{ $match: { name: ?0 } }",
      "{ $unwind: '$pricing' }",
      "{ $addFields: { difference: { $abs: { $subtract: ['$pricing.quantity', ?1] } } } }",
      "{ $sort: { difference: 1 } }",
      "{ $limit: 1 }",
      "{ $project: { _id: 0, unitCost: '$pricing.unitCost'} }"
  })
  Optional<Double> findNearestPriceForMethod(String methodName, Integer targetQuantity);
}