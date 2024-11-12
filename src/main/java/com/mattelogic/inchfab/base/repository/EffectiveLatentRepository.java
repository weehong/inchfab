package com.mattelogic.inchfab.base.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EffectiveLatentRepository<T> extends FieldValueRepository<T> {

  @Aggregation(pipeline = {
      "{ $match: { 'name': ?0 } }",
      "{ $unwind: '$settings' }",
      "{ $match: { 'settings.name': ?1 } }",
      "{ $replaceRoot: { newRoot: { value: '$settings.effectiveLatent' } } }",
      "{ $project: { value: 1, _id: 0 } }"
  })
  Optional<Double> findEffectiveLatentValueBySettingsName(String name, String settingsName);
}