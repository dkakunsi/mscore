package com.devit.mscore.notification.mail;

import com.devit.mscore.Template;
import com.devit.mscore.exception.TemplateException;

import org.json.JSONObject;

public class StringTemplate implements Template {

    @Override
    public String build(String template, JSONObject object) throws TemplateException {
        return String.format(template, object.getString("id"));
    }
}
