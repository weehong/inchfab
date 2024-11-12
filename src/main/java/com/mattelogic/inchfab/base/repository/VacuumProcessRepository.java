package com.mattelogic.inchfab.base.repository;

import com.mattelogic.inchfab.base.entity.VacuumProcess;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing VacuumProcess entities in MongoDB. Provides custom aggregation
 * methods for querying process parameters and values.
 */
@Repository
public interface VacuumProcessRepository extends MongoRepository<VacuumProcess, String> {

  /**
   * Finds the total value of a specific parameter across multiple subprocesses.
   *
   * @param type            the vacuum process type
   * @param processName     the name of the process
   * @param subprocessNames list of subprocess names to include
   * @param key             the parameter key to sum
   * @return Optional containing the total value as Double if found, empty Optional otherwise
   * <p>
   * Pipeline explanation:
   * 1. Matches documents with the specified type
   * 2. Unwinds the processes array
   * 3. Matches the specific process by name
   * 4. Unwinds the subprocesses array
   * 5. Matches subprocesses with names in the provided list
   * 6. Groups and sums the parameter values
   * 7. Projects only the total value
   */
  @Aggregation(pipeline = {
      "{ $match: { 'type': :#{#type} } }",
      "{ $unwind: '$processes' }",
      "{ $match: { 'processes.name': :#{#processName} } }",
      "{ $unwind: '$processes.subprocesses' }",
      "{ $match: { 'processes.subprocesses.name': { $in: :#{#subprocessNames} } } }",
      "{ $group: { _id: null, totalValue: { $sum: '$processes.subprocesses.parameters.:#{#key}' } } }",
      "{ $project: { _id: 0, totalValue: 1 } }"
  })
  Optional<Double> findValueByTypeAndMetric(
      @Param("type") String type,
      @Param("processName") String processName,
      @Param("subprocessNames") List<String> subprocessNames,
      @Param("key") String key
  );

  /**
   * Finds the total value of a specific parameter for a process.
   *
   * @param type        the vacuum process type
   * @param processName the name of the process
   * @param key         the parameter key to sum
   * @return Optional containing the total value as Double if found, empty Optional otherwise
   * <p>
   * Pipeline explanation:
   * 1. Matches documents with the specified type
   * 2. Unwinds the processes array
   * 3. Matches the specific process by name
   * 4. Groups and sums the parameter values, converting to double and handling nulls
   * 5. Projects only the total value
   */
  @Aggregation(pipeline = {
      "{ $match: { 'type': :#{#type} } }",
      "{ $unwind: '$processes' }",
      "{ $match: { 'processes.name': :#{#processName} } }",
      "{ $group: { "
          + "_id: null, "
          + "totalValue: { $sum: { $toDouble: { $ifNull: [ '$processes.parameters.:#{#key}', 0 ] } } } "
          + "} }",
      "{ $project: { _id: 0, totalValue: 1 } }"
  })
  Optional<Double> findValueByTypeAndMetric(
      @Param("type") String type,
      @Param("processName") String processName,
      @Param("key") String key
  );

  /**
   * Gets the maximum gas value for a specific gas property across subprocesses.
   *
   * @param type        the vacuum process type
   * @param processName the name of the process
   * @param gasProperty the gas property to find maximum value for
   * @return Optional containing the maximum gas value as Double if found, empty Optional otherwise
   * <p>
   * Pipeline explanation:
   * 1. Matches documents with the specified type
   * 2. Unwinds the processes array
   * 3. Matches the specific process by name
   * 4. Unwinds the subprocesses array
   * 5. Extracts subprocess parameters
   * 6. Projects gas value, converting to double and handling nulls
   * 7. Groups to find maximum gas value
   * 8. Projects only the maximum value
   */
  @Aggregation(pipeline = {
      "{ $match: { 'type': ?0 } }",
      "{ $unwind: '$processes' }",
      "{ $match: { 'processes.name': ?1 } }",
      "{ $unwind: '$processes.subprocesses' }",
      "{ $replaceRoot: { newRoot: '$processes.subprocesses.parameters' } }",
      "{ $project: { gasValue: { $toDouble: { $ifNull: ['$?2', 0] } } } }",
      "{ $group: { _id: null, maxGasValue: { $max: '$gasValue' } } }",
      "{ $project: { _id: 0, maxGasValue: 1 } }"
  })
  Optional<Double> getMaxGasValue(
      String type,
      String processName,
      String gasProperty
  );
}