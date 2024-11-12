package com.mattelogic.inchfab.base.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

/**
 * Base repository interface providing common field value lookup functionality.
 *
 * @param <T> the domain type the repository manages
 */
@NoRepositoryBean
public interface FieldValueRepository<T> extends MongoRepository<T, String> {

  /**
   * Retrieves a specific field value from a document by its name.
   *
   * @param name      the name of the document
   * @param fieldName the name of the field to retrieve
   * @return an Optional containing the field value if found, empty Optional otherwise
   */
  @Aggregation(pipeline = {
      "{ $match: { 'name': ?0 } }",
      "{ $project: { _id: 0, :#{#fieldName} : 1 } }",
      "{ $replaceRoot: { newRoot: { value: '$:#{#fieldName}' } } }",
      "{ $project: { value: 1, _id: 0 } }"
  })
  Optional<Double> findValueByNameAndField(
      String name,
      @Param("fieldName") String fieldName
  );
}