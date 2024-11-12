package com.mattelogic.inchfab.base.repository;

import com.mattelogic.inchfab.base.entity.LithographyData;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

@Repository
public interface LithographyDataRepository extends TotalEffectiveRepository<LithographyData> {

  @Aggregation(pipeline = {
      "{ $unwind: '$photoresists' }",
      "{ $match: { 'photoresists.name': ?0 } }",
      "{ $project: { totalValue: { $getField: { input: { $getField: { input: '$photoresists', field: ?1 } }, field: ?2 } } } }"
  })
  Optional<Double> findFieldByNameAndField(
      String photoresistName,
      String fieldName,
      String type
  );

  @Aggregation(pipeline = {
      "{ $unwind: '$photoresists' }",
      "{ $match: { 'photoresists.name': ?0 } }",
      "{ $match: { $expr: { $ne: [{ $getField: { field: ?1, input: '$photoresists.materials' } }, null] } } }",
      "{ $replaceRoot: { newRoot: { value: { $getField: { field: ?2, input: { $getField: { field: ?1, input: '$photoresists.materials' } } } } } } }",
      "{ $project: { value: 1, _id: 0 } }"
  })
  Optional<Double> findMaterialValue(String photoresistName, String materialType, String fieldName);
}
