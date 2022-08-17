package com.devit.mscore.notification.mail;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

public class EmailExtractorTest {

  @Test
  public void testExtract() {
    var possibleAttributes = List.of("email");

    // no email
    var json = new JSONObject();
    json.put("name", "first-last name");

    var optional = EmailExtractor.extract(json, possibleAttributes);
    assertTrue(optional.isEmpty());

    // with email in nested object
    var contact = new JSONObject();
    contact.put("email", "email@contact.com");
    contact.put("phone", "12345");
    json.put("contact", contact);

    optional = EmailExtractor.extract(json, possibleAttributes);
    assertTrue(optional.isPresent());
    assertThat(optional.get(), is(List.of("email@contact.com")));

    // with email at top level
    json.put("email", "firstname@lastname.com");
    optional = EmailExtractor.extract(json, possibleAttributes);
    assertTrue(optional.isPresent());
    assertThat(optional.get(), is(List.of("firstname@lastname.com")));
  }
}
