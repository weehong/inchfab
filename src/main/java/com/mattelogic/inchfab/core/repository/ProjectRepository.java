package com.mattelogic.inchfab.core.repository;

import com.mattelogic.inchfab.core.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

  @Query(
      """
          SELECT a
          FROM Project a
          WHERE a.company.id = :companyId
          """)
  List<Project> findAllByCompanyId(Long companyId);

  @Modifying
  @Query(
      """
          UPDATE Project p SET p.rootFolderId = :rootFolderId, p.projectFolderId = :projectFolderId, p.uploadFile = :uploadFiles WHERE p.id = :projectID
          """)
  void updateProjectByUploadedDocument(
      String rootFolderId, String projectFolderId, String uploadFiles, Long projectID);
}