package com.mattelogic.inchfab.base.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface for entities with total effective process and latent settings.
 *
 * @param <T> the domain type the repository manages
 */
@NoRepositoryBean
public interface TotalEffectiveRepository<T> extends FieldValueRepository<T> {

  /**
   * Retrieves the total effective process value for a specific settings entry.
   *
   * @param name         the name of the configuration
   * @param settingsName the name of the settings entry
   * @return an Optional containing the total effective process value if found, empty Optional
   * otherwise
   */
  @Aggregation(pipeline = {
      "{ $match: { 'name': ?0 } }",
      "{ $unwind: '$settings' }",
      "{ $match: { 'settings.name': ?1 } }",
      "{ $replaceRoot: { newRoot: { value: '$settings.totalEffectiveProcess' } } }",
      "{ $project: { value: 1, _id: 0 } }"
  })
  Optional<Double> findTotalEffectiveProcessValueBySettingsName(String name, String settingsName);

  /**
   * Retrieves the total effective latent value for a specific settings entry.
   *
   * @param name         the name of the configuration
   * @param settingsName the name of the settings entry
   * @return an Optional containing the total effective latent value if found, empty Optional
   * otherwise
   */
  @Aggregation(pipeline = {
      "{ $match: { 'name': ?0 } }",
      "{ $unwind: '$settings' }",
      "{ $match: { 'settings.name': ?1 } }",
      "{ $replaceRoot: { newRoot: { value: '$settings.totalEffectiveLatent' } } }",
      "{ $project: { value: 1, _id: 0 } }"
  })
  Optional<Double> findTotalEffectiveLatentValueBySettingsName(String name, String settingsName);
}