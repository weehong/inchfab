package com.mattelogic.inchfab.base.repository;

import com.mattelogic.inchfab.base.entity.Constant;
import java.util.Optional;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConstantRepository extends MongoRepository<Constant, String> {

  @Aggregation(pipeline = {
      "{ $match: { ':#{#type}.metric': :#{#metric} } }",
      "{ $project: { _id: 0, value: { $arrayElemAt: [ { $filter: { input: '$:#{#type}', as: 'item', cond: { $eq: ['$$item.metric', :#{#metric}] } } }, 0] } } }",
      "{ $replaceRoot: { newRoot: '$value' } }",
      "{ $project: { value: 1, _id: 0 } }"
  })
  Optional<Double> findValueByTypeAndMetric(
      @Param("type") String type,
      @Param("metric") String metric
  );
}