package com.mattelogic.inchfab.base.repository;

import com.mattelogic.inchfab.base.entity.Gas;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing Gas entities in MongoDB.
 */
@Repository
public interface GasRepository extends MongoRepository<Gas, String> {

  /**
   * Finds the price of a gas by its name.
   * <p>
   * Pipeline explanation:
   * 1. Unwinds the gases array into separate documents
   * 2. Matches documents with the specified gas name
   * 3. Replaces root to elevate gas document
   * 4. Projects only the price field
   *
   * @param name the name of the gas
   * @return Optional containing the gas price if found
   */
  @Aggregation(pipeline = {
      "{ $unwind: '$gases' }",
      "{ $match: { 'gases.name': :#{#name} } }",
      "{ $replaceRoot: { newRoot: '$gases' } }",
      "{ $project: { _id: 0, price: 1 } }"
  })
  Optional<Double> findPriceByName(@Param("name") String name);
}