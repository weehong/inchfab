package com.mattelogic.inchfab.core.exception;

public class ProjectNotFoundException extends RuntimeException {

  public ProjectNotFoundException(Long id) {
    super(String.format("Project with ID %d could not be found", id));
  }

  public ProjectNotFoundException(String message) {
    super(message);
  }

  public ProjectNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}