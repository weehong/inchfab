package com.mattelogic.inchfab.core.service;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxCCGAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mattelogic.inchfab.common.dto.ApiResponseDto;
import com.mattelogic.inchfab.core.dtos.request.DocumentRequestDto;
import com.mattelogic.inchfab.core.dtos.response.DocumentResponseDto;
import com.mattelogic.inchfab.core.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl {

  private final ProjectRepository projectRepository;

  @Value("${box.client-id}")
  private String clientId;
  @Value("${box.client-secret}")
  private String accessToken;
  @Value("${box.enterprise-id}")
  private String enterpriseId;
  @Value("${box.folder-id}")
  private String boxFolderId;

  @Transactional
  public ApiResponseDto<DocumentResponseDto> upload(DocumentRequestDto documentRequestDto)
      throws JsonProcessingException {
    String companyFolderId;
    String projectFolderId;
    List<String> uploadedFilesIds = new ArrayList<>();

    try {
      BoxCCGAPIConnection connection =
          BoxCCGAPIConnection.applicationServiceAccountConnection(
              clientId, accessToken, enterpriseId);

      connection.asUser("28982507807");

      companyFolderId = isFolderExist(connection, documentRequestDto.companyName(), boxFolderId);
      projectFolderId =
          isFolderExist(
              connection,
              documentRequestDto.projectName() + " - " + documentRequestDto.projectId(),
              companyFolderId);

      List<MultipartFile> filesToUpload = documentRequestDto.files();

      String fileName = documentRequestDto.projectName() + ".zip";
      BoxFolder folder = new BoxFolder(connection, projectFolderId);
      for (BoxItem.Info itemInfo : folder) {
        if (itemInfo instanceof BoxFile.Info fileInfo) {
          if (fileInfo.getName().equals(fileName)) {
            log.info("File already exists: {}", fileInfo.getName());
            fileName = interpolateFilename(fileName);
          }
        }
      }

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream);

      for (MultipartFile multipartFile : filesToUpload) {
        addFileToZip(zipOut, multipartFile);
        zipOut.closeEntry();
      }

      zipOut.close();

      byte[] zipData = byteArrayOutputStream.toByteArray();

      folder.uploadFile(new ByteArrayInputStream(zipData), fileName);
      List<String> files = getAllFiles(connection, projectFolderId);
      uploadedFilesIds.addAll(files);
    } catch (BoxAPIException | IOException e) {
      System.out.println("Exception: " + e.getMessage());
      throw new RuntimeException(e);
    }

    ObjectMapper objectMapper = new ObjectMapper();
    String jsonUploadFiles = objectMapper.writeValueAsString(uploadedFilesIds);

    projectRepository.updateProjectByUploadedDocument(
        companyFolderId, projectFolderId, jsonUploadFiles, documentRequestDto.projectId());

    return ApiResponseDto.<DocumentResponseDto>builder()
        .status(HttpStatus.OK.value())
        .message("Company fetched successfully")
        .data(new DocumentResponseDto(companyFolderId, projectFolderId, uploadedFilesIds))
        .build();
  }

  private String createFolder(
      BoxAPIConnection connection, String parentFolderId, String folderName) {
    BoxFolder parentFolder = new BoxFolder(connection, parentFolderId);
    BoxFolder.Info childFolderInfo = parentFolder.createFolder(folderName);
    return childFolderInfo.getResource().getID();
  }

  private List<String> getAllFiles(BoxAPIConnection connection, String folderId) {
    List<String> files = new ArrayList<>();
    BoxFolder folder = new BoxFolder(connection, folderId);
    for (BoxItem.Info itemInfo : folder) {
      if (itemInfo instanceof BoxFile.Info fileInfo) {
        files.add(fileInfo.getID());
      }
    }
    return files;
  }

  private String isFolderExist(BoxAPIConnection connection, String name, String folderId) {
    String isExists = listFoldersAndFiles(connection, name, folderId);

    if (isExists.isEmpty()) {
      return createFolder(connection, folderId, name);
    } else {
      return isExists;
    }
  }

  private void addFileToZip(ZipOutputStream zipOut, MultipartFile multipartFile)
      throws IOException {
    zipOut.putNextEntry(new ZipEntry(Objects.requireNonNull(multipartFile.getOriginalFilename())));

    byte[] buffer = new byte[1024];
    int bytesRead;
    InputStream inputStream = multipartFile.getInputStream();
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      zipOut.write(buffer, 0, bytesRead);
    }

    zipOut.closeEntry();
    inputStream.close();
  }

  private String interpolateFilename(String filename) {
    int lastDotIndex = filename.lastIndexOf(".");

    if (lastDotIndex != -1) {
      String extension = filename.substring(lastDotIndex);

      String filenameWithoutExtension = filename.substring(0, lastDotIndex);

      SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMMyyyyhhmmssSa", Locale.ENGLISH);
      String timestamp = " - " + dateFormat.format(new Date());

      return filenameWithoutExtension + timestamp + extension;
    }

    return "";
  }

  private String listFoldersAndFiles(BoxAPIConnection api, String name, String folderId) {
    BoxFolder folder = new BoxFolder(api, folderId);
    for (BoxItem.Info itemInfo : folder) {
      if (itemInfo.getName().equals(name)) {
        return itemInfo.getID();
      }
    }
    return "";
  }
}
