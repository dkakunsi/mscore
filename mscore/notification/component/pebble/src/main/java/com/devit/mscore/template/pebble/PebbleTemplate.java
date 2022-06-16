package com.devit.mscore.template.pebble;

import static com.devit.mscore.util.JsonUtils.flatten;

import java.io.StringWriter;

import com.devit.mscore.ApplicationContext;
import com.devit.mscore.Template;
import com.devit.mscore.exception.TemplateException;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.StringLoader;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PebbleTemplate implements Template {

    private static final Logger LOGGER = LoggerFactory.getLogger(PebbleTemplate.class);

    PebbleTemplate() {
    }

    @Override
    public String build(ApplicationContext context, String template, JSONObject object) throws TemplateException {
        var engine = new PebbleEngine.Builder().loader(new StringLoader()).build();
        var writer = new StringWriter();

        try {
            engine.getTemplate(template).evaluate(writer, flatten(object).toMap());
            return writer.toString();
        } catch (Exception ex) {
            LOGGER.error("BreadcrumbId: {}. Cannot load template.", context.getBreadcrumbId());
            throw new TemplateException("Cannot load template.", ex);
        }
    }
}
