package com.devit.mscore.schema.everit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThrows;

import com.devit.mscore.exception.ResourceException;
import com.devit.mscore.exception.ValidationException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Test;

public class JSONSchemaTest {

  @Test
  public void testValidate() throws URISyntaxException, ResourceException, ValidationException {
    var resourceFile = getResourceFile("resource/resource.json");
    var actual = new JSONSchema(resourceFile);
    var json = "{\"domain\":\"domain\",\"id\":\"id\",\"name\":\"name\",\"reference1\":{\"domain\":\"referenceDomain1\",\"id\":\"referenceId1\"}}";
    actual.validate(new JSONObject(json));
  }

  @Test
  public void testGetReferences_SingleReference() throws URISyntaxException, ResourceException, ValidationException {
    var resourceFile = getResourceFile("resource/resource.json");
    var actual = new JSONSchema(resourceFile);

    var references = actual.getReferenceNames();
    assertThat(references.size(), is(2));
    var expectedReferences = Set.of("reference1", "reference2");
    assertThat(references, is(expectedReferences));

    var referenceDomains = actual.getReferenceDomains();
    assertThat(referenceDomains.size(), is(2));
    assertThat(referenceDomains.get(0).get(0), is("referenceDomain1"));
    assertThat(referenceDomains.get(1).get(0), is("referenceDomain2"));
  }

  @Test
  public void testGetReferences_MultipleReference() throws URISyntaxException, ResourceException, ValidationException {
    var resourceFile = getResourceFile("resource/resource_multi_references.json");
    var actual = new JSONSchema(resourceFile);

    var references = actual.getReferenceNames();
    assertThat(references.size(), is(2));
    var expectedReferences = Set.of("reference1", "reference2");
    assertThat(references, is(expectedReferences));

    var referenceDomains = actual.getReferenceDomains();
    assertThat(referenceDomains.size(), is(2));
    assertThat(referenceDomains.get(0).get(0), is("referenceDomain1a"));
    assertThat(referenceDomains.get(0).get(1), is("referenceDomain1b"));
    assertThat(referenceDomains.get(1).get(0), is("referenceDomain2a"));
    assertThat(referenceDomains.get(1).get(1), is("referenceDomain2b"));
  }

  @Test
  public void testValidate_Invalid() throws URISyntaxException, ResourceException, ValidationException {
    var resourceFile = getResourceFile("resource/resource.json");
    var actual = new JSONSchema(resourceFile);
    var json = "{\"domain\":\"unknown\",\"id\":\"toolongid\",\"name\":\"toolongname\"}";

    var ex = assertThrows(ValidationException.class, () -> actual.validate(new JSONObject(json)));
    assertThat(ex.getMessage(), is("Failed to validate JSON"));
    assertThat(ex.getCause(), instanceOf(org.everit.json.schema.ValidationException.class));
  }

  @Test
  public void testCreate_JSONConstructor() throws ResourceException, URISyntaxException {
    var resourceFile = getResourceFile("resource/resource.json");
    var content = new JSONSchema(resourceFile).getContent();
    var json = new JSONObject("{\"domain\":\"resource\",\"name\":\"name\"}")
        .put("content", content);

    var schema = new JSONSchema(json);
    var actual = schema.getMessage();

    assertThat(actual.toString(), is(json.toString()));
  }

  @Test
  public void testGetIndeces() throws URISyntaxException, ResourceException {
    var resourceFile = getResourceFile("resource/resource.json");
    var content = new JSONSchema(resourceFile).getContent();
    var json = new JSONObject("{\"domain\":\"resource\",\"name\":\"name\"}")
        .put("content", content);

    var schema = new JSONSchema(json);
    var indeces = schema.getIndeces();

    // indeces = { field: { unique: true } }
    assertThat(indeces.size(), is(1));
    assertThat(indeces.get(0).getField(), is("id"));
    assertThat(indeces.get(0).isUnique(), is(true));
  }

  public static File getResourceFile(String resourceName) throws URISyntaxException {
    var resource = JSONSchemaTest.class.getClassLoader().getResource(resourceName);
    return new File(resource.toURI());
  }
}
