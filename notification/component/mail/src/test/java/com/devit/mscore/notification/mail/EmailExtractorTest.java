package com.devit.mscore.notification.mail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class EmailExtractorTest {

  @Test
  public void testExtract_FromTopLevel() {
    var possibleAttributes = List.of("email");
    var json = new JSONObject();
    json.put("name", "first-last name");
    json.put("email", "firstname@lastname.com");

    var optional = EmailExtractor.extract(json, possibleAttributes);
    assertTrue(optional.isPresent());
    assertThat(optional.get(), is(List.of("firstname@lastname.com")));
  }

  @Test
  public void testExtract_FromNestedObject() {
    var possibleAttributes = List.of("email");

    var contact = new JSONObject();
    contact.put("email", "email@contact.com");
    contact.put("phone", "12345");

    var json = new JSONObject();
    json.put("name", "first-last name");
    json.put("contact", contact);

    var optional = EmailExtractor.extract(json, possibleAttributes);
    assertTrue(optional.isPresent());
    assertThat(optional.get(), is(List.of("email@contact.com")));
  }

  @Test
  public void testExtract_FromNestedArray() {
    var possibleAttributes = List.of("email");

    var email = new JSONObject();
    email.put("email", "email@contact.com");
    email.put("name", "EMAIL");
    var phone = new JSONObject();
    phone.put("phone", "080989999");
    phone.put("name", "PHONE");

    var contacts = new JSONArray();
    contacts.put(email);
    contacts.put(phone);

    var arr = new JSONArray();
    arr.put("one");
    arr.put("two");
    arr.put("three");

    var json = new JSONObject();
    json.put("name", "first-last name");
    json.put("contacts", contacts);
    json.put("arr", arr);

    var optional = EmailExtractor.extract(json, possibleAttributes);
    assertTrue(optional.isPresent());
    assertThat(optional.get(), is(List.of("email@contact.com")));
  }

  @Test
  public void testExtract_Empty() {
    var possibleAttributes = List.of("email");

    var json = new JSONObject();
    json.put("name", "first-last name");

    var optional = EmailExtractor.extract(json, possibleAttributes);
    assertTrue(optional.isEmpty());
  }
}
