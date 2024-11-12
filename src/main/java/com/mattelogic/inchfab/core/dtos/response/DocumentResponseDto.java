package com.mattelogic.inchfab.core.dtos.response;

import java.util.List;

public record DocumentResponseDto(
    String rootFolderId,
    String projectFolderId,
    List<String> uploadFilePaths
) {

}
