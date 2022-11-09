package com.devit.mscore;

import com.devit.mscore.exception.ResourceException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.json.JSONObject;

/**
 * Object to maintain resource file.
 *
 * @author dkakunsi
 */
public class Resource {

  private static final String RESOURCE_FAILURE_MESSAGE = "Cannot read resource file";

  protected String name;

  protected String content;

  protected Resource(File resourceFile) throws ResourceException {
    this(resourceFile.getName(), getContent(resourceFile));
  }

  public Resource(String name, String content) {
    this.name = name;
    this.content = content;
  }

  protected static String getContent(File resourceFile) throws ResourceException {
    try {
      return Files.readString(resourceFile.toPath());
    } catch (IOException | RuntimeException ex) {
      throw new ResourceException(RESOURCE_FAILURE_MESSAGE, ex);
    }
  }

  public static List<File> getFiles(File directory) throws ResourceException {
    if (!directory.isDirectory()) {
      throw new ResourceException("Location is not a directory", new IOException(directory.getAbsolutePath()));
    }

    var files = directory.listFiles();
    if (files == null || files.length <= 0) {
      throw new ResourceException("No file available in directory: " + directory.getName());
    }

    return List.of(files);
  }

  public String getName() {
    return name;
  }

  public String getContent() {
    return content;
  }

  /**
   * Create JSON representation for this resource as message.
   *
   * @return resource JSON representation.
   */
  public JSONObject getMessage() {
    // @formatter:off
        return new JSONObject()
                .put("domain", "resource")
                .put("name", getName())
                .put("content", getContent());
        // @formatter:on
  }

  @Override
  public String toString() {
    return getMessage().toString();
  }
}
