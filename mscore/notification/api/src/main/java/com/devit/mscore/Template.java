package com.devit.mscore;

import com.devit.mscore.exception.TemplateException;

import org.json.JSONObject;

/**
 * Interface to build message from a template.
 * 
 * @author dkakunsi
 */
public interface Template {

    /**
     * Build message from {@code template} and {@code object}.
     * 
     * @param context  of the request.
     * @param template message template.
     * @param object   data.
     * @return message.
     * @throws TemplateException error in template processing.
     */
    String build(ApplicationContext context, String template, JSONObject object) throws TemplateException;
}
