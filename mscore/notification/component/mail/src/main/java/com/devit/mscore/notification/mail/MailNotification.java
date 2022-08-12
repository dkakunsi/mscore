package com.devit.mscore.notification.mail;

import static com.devit.mscore.ApplicationContext.getContext;
import static com.devit.mscore.notification.mail.EmailExtractor.extract;
import static com.devit.mscore.util.AttributeConstants.getName;
import static com.devit.mscore.util.JsonUtils.flatten;
import static com.devit.mscore.util.Utils.ACTION;

import java.util.List;

import com.devit.mscore.Logger;
import com.devit.mscore.Notification;
import com.devit.mscore.Registry;
import com.devit.mscore.Template;
import com.devit.mscore.exception.NotificationException;
import com.devit.mscore.exception.RegistryException;
import com.devit.mscore.exception.TemplateException;
import com.devit.mscore.logging.ApplicationLogger;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import jakarta.mail.MessagingException;

public class MailNotification implements Notification {

    private static final Logger LOGGER = ApplicationLogger.getLogger(MailNotification.class);

    private static final String NO_TEMPLATE_MESSAGE = "Cannot send notification. No email template found in request.";

    private static final String CANNOT_LOAD_TEMPLATE_MESSAGE = "Cannot load email template.";

    private Registry registry;

    private MailSender sender;

    private Template template;

    private SendInfo sendInfo;

    private List<String> possibleAttributes;

    MailNotification(Registry registry, MailSender sender, Template template, SendInfo sendInfo) {
        this.registry = registry;
        this.sender = sender;
        this.template = template;
        this.sendInfo = sendInfo;
    }

    public MailNotification withPossibleAttributes(String possibleAttributes) {
        this.possibleAttributes = List.of(possibleAttributes.split(","));
        return this;
    }

    @Override
    public String getType() {
        return "email";
    }

    @Override
    public void send(JSONObject entity) throws NotificationException {
        var optional = extract(entity, this.possibleAttributes);
        if (optional.isEmpty()) {
            LOGGER.warn("No email available in entity.");
            return;
        }

        var emails = optional.get();
        var emailTemplate = loadTemplate(entity);
        var emailSubject = String.format("%s | %s", this.sendInfo.getSubject(), getName(entity));

        try {
            for (String to : emails) {
                var text = this.template.build(emailTemplate, flatten(entity));
                this.sender.send(this.sendInfo, to, emailSubject, text);
            }
        } catch (MessagingException | TemplateException ex) {
            throw new NotificationException("Cannot send email.", ex);
        }
    }

    private String loadTemplate(JSONObject entity) throws NotificationException {
        var context = getContext();
        var templateName = context.getAction().orElse(entity.optString(ACTION));
        if (StringUtils.isBlank(templateName)) {
            LOGGER.warn("Message: {}.", NO_TEMPLATE_MESSAGE);
            throw new NotificationException(NO_TEMPLATE_MESSAGE);
        }

        try {
            var templateRegister = this.registry.get(templateName);
            return new JSONObject(templateRegister).getString("content");
        } catch (JSONException | RegistryException ex) {
            LOGGER.error("Message: {}.", CANNOT_LOAD_TEMPLATE_MESSAGE);
            throw new NotificationException(CANNOT_LOAD_TEMPLATE_MESSAGE, ex);
        }
    }
}
