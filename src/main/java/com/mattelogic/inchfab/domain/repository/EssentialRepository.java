package com.mattelogic.inchfab.domain.repository;

import com.mattelogic.inchfab.domain.entity.Essential;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EssentialRepository extends MongoRepository<Essential, String> {

  @Aggregation(pipeline = {
      "{$limit: 1}",
      "{$project: {_id: 0, value: '$laborCost'}}"
  })
  Double findLaborCost();

  @Aggregation(pipeline = {
      "{$limit: 1}",
      "{$project: {_id: 0, value: '$electricityCost'}}"
  })
  Double findElectricityCost();
}

