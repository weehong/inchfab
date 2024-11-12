package com.mattelogic.inchfab.core.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record DocumentRequestDto(
    @NotNull(message = "Project ID is required")
    Long projectId,

    @NotBlank(message = "Project name cannot be blank.")
    @NotNull(message = "Project name cannot be null.")
    String projectName,

    @NotBlank(message = "Company name cannot be blank.")
    @NotNull(message = "Company name cannot be null.")
    String companyName,

    @NotBlank(message = "Files is cannot be blank.")
    @NotNull(message = "Files cannot be null.")
    @Size(min = 1, message = "Files should have minimum 1 file.")
    List<MultipartFile> files
) {

}
