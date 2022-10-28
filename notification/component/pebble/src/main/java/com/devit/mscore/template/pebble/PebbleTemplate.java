package com.devit.mscore.template.pebble;

import static com.devit.mscore.util.JsonUtils.flatten;

import com.devit.mscore.Logger;
import com.devit.mscore.Template;
import com.devit.mscore.exception.TemplateException;
import com.devit.mscore.logging.ApplicationLogger;

import java.io.StringWriter;

import org.json.JSONObject;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.StringLoader;

public class PebbleTemplate implements Template {

  private static final Logger LOGGER = ApplicationLogger.getLogger(PebbleTemplate.class);

  PebbleTemplate() {
  }

  @Override
  public String build(String template, JSONObject object) throws TemplateException {
    var engine = new PebbleEngine.Builder().loader(new StringLoader()).build();
    var writer = new StringWriter();

    try {
      engine.getTemplate(template).evaluate(writer, flatten(object).toMap());
      return writer.toString();
    } catch (Exception ex) {
      LOGGER.error("Cannot load template");
      throw new TemplateException("Cannot load template", ex);
    }
  }
}
