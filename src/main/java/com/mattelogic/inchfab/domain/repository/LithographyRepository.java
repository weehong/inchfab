package com.mattelogic.inchfab.domain.repository;

import com.mattelogic.inchfab.domain.entity.Lithography;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LithographyRepository extends MongoRepository<Lithography, String> {

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

  @Aggregation(pipeline = {
      "{ $project: { _id: 0, value: { $getField: { field: ?0, input: '$price' } } } }",
      "{ $project: { value: 1, _id: 0 } }"
  })
  Optional<Double> findPrice(String priceType);

  @Aggregation(pipeline = {
      "{ $match: { 'name': ?0 } }",
      "{ $unwind: '$settings' }",
      "{ $match: { 'settings.name': ?1 } }",
      "{ $unwind: '$settings.steps' }",
      "{ $match: { 'settings.steps.name': ?2 } }",
      "{ $project: { value: '$settings.steps.effectiveLatent', _id: 0 } }"
  })
  Optional<Double> findEffectiveLatentBySettingsAndStepName(String name, String settingsName,
      String stepName);

  @Aggregation(pipeline = {
      "{ $match: { 'name': ?0 } }",
      "{ $unwind: '$settings' }",
      "{ $match: { 'settings.name': ?1 } }",
      "{ $unwind: '$settings.steps' }",
      "{ $match: { 'settings.steps.name': ?2 } }",
      "{ $project: { value: { $cond: { if: { $eq: ['$settings.steps.effectiveProcess', 'calculated_separately'] }, then: null, else: { $convert: { input: '$settings.steps.effectiveProcess', to: 'double' } } } }, _id: 0 } }"
  })
  Optional<Double> findEffectiveProcessBySettingsAndStepName(
      String name,
      String settingsName,
      String stepName);
}