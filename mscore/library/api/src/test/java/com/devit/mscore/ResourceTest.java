package com.devit.mscore;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URISyntaxException;

import com.devit.mscore.exception.ResourceException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class ResourceTest {

  @Test
  public void testGetMessage() throws URISyntaxException, ResourceException {
    var resourceFile = getResourceFile("resource/resource.json");
    var actual = new Resource(resourceFile).getMessage();

    assertThat(actual.getString("domain"), is("resource"));
    assertThat(actual.getString("name"), is("resource.json"));
    assertTrue(StringUtils.isNotBlank(actual.getString("content")));
  }

  @Test
  public void testCreate_ThrowResourceException() throws URISyntaxException, ResourceException {
    var resourceFile = mock(File.class);
    doThrow(new RuntimeException()).when(resourceFile).toPath();

    var ex = assertThrows(ResourceException.class, () -> new Resource(resourceFile).getMessage());
    assertThat(ex.getMessage(), is("Cannot read resource file."));
    assertThat(ex.getCause(), instanceOf(RuntimeException.class));
  }

  public static File getResourceFile(String resourceName) throws URISyntaxException {
    var resource = ResourceTest.class.getClassLoader().getResource(resourceName);
    return new File(resource.toURI());
  }

  @Test
  public void testGetFiles() throws URISyntaxException, ResourceException {
    var directory = getResourceFile("resource");
    var files = Resource.getFiles(directory);
    assertThat(files.size(), is(1));
  }

  @Test
  public void testGetFiles_NotDirectory() throws URISyntaxException, ResourceException {
    var directory = mock(File.class);
    doReturn(false).when(directory).isDirectory();

    var ex = assertThrows(ResourceException.class, () -> Resource.getFiles(directory));
    assertThat(ex.getMessage(), is("Location is not a directory."));
  }

  @Test
  public void testGetFiles_Empty() throws URISyntaxException, ResourceException {
    var directory = mock(File.class);
    doReturn(true).when(directory).isDirectory();
    doReturn("resource").when(directory).getName();
    doReturn(new File[0]).when(directory).listFiles();

    var ex = assertThrows(ResourceException.class, () -> Resource.getFiles(directory));
    assertThat(ex.getMessage(), is("No file available in directory: resource"));
  }
}
