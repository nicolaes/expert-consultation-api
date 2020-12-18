package com.code4ro.legalconsultation.mail.service.impl;

import com.code4ro.legalconsultation.core.exception.LegalValidationException;
import com.code4ro.legalconsultation.document.metadata.model.persistence.DocumentMetadata;
import com.code4ro.legalconsultation.invitation.model.persistence.Invitation;
import com.code4ro.legalconsultation.i18n.service.I18nService;
import com.code4ro.legalconsultation.user.model.persistence.User;
import com.code4ro.legalconsultation.mail.service.MailApi;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Profile("production")
@Slf4j
public class MailService implements MailApi {
    @Value("${app.email.signupurl}")
    private String signupUrl;
    @Value("${app.email.documenturl}")
    private String documentUrl;
    @Value("${spring.mvc.locale}")
    private String configuredLocale;
    @Value("${app.email.sender}")
    private String from;
  
    private final JavaMailSender mailSender;
    private final I18nService i18nService;
    private final Configuration freemarkerConfig;


    @Autowired
    public MailService(final JavaMailSender mailSender,
                       final I18nService i18nService,
                       final Configuration freemarkerConfig) {
        this.mailSender = mailSender;
        this.i18nService = i18nService;
        this.freemarkerConfig = freemarkerConfig;
    }

    @Override
    public void sendRegisterMail(final List<Invitation> invitations) {
        final List<String> failedEmails = new ArrayList<>();

        final String translatedSubject = i18nService.translate("register.User.confirmation.subject");
        final String registerTemplate = getRegisterTemplate();
        invitations.forEach(invitation ->
            buildAndSendEmail(translatedSubject,
                    registerTemplate,
                    getRegisterModel(invitation),
                    invitation.getUser().getEmail())
                    .ifPresent(failedEmails::add));

        if (!failedEmails.isEmpty()) {
            throw LegalValidationException.builder()
                    .i18nKey("user.Email.send.failed")
                    .i8nArguments(failedEmails)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    @Override
    public void sendDocumentAssignedEmail(final DocumentMetadata documentMetadata, final List<User> users) {
        final List<String> failedEmails = new ArrayList<>();

        final String translatedSubject = i18nService.translate("email.documentAssigned.subject");
        final String documentAssignedTemplate = getDocumentAssignedTemplate();
        users.forEach(user ->
                buildAndSendEmail(translatedSubject, documentAssignedTemplate, getDocumentInfoMap(documentMetadata, user), user.getEmail())
                    .ifPresent(failedEmails::add)
        );

        if (!failedEmails.isEmpty()) {
            throw LegalValidationException.builder()
                    .i18nKey("user.Email.send.failed")
                    .i8nArguments(failedEmails)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    @Override
    public void sendDocumentConsultationEmail(final DocumentMetadata documentMetadata, final List<User> users) {
        final List<String> failedEmails = new ArrayList<>();

        users.forEach(user -> buildAndSendEmail(
                i18nService.translate("email.documentConsultation.subject")
                        .replace("$documentTitle", documentMetadata.getDocumentTitle()),
                "document-consultation-email-" + configuredLocale + ".ftl",
                getDocumentInfoMap(documentMetadata, user),
                user.getEmail()
        ).ifPresent(failedEmails::add));

        if (!failedEmails.isEmpty()) {
            throw LegalValidationException.builder()
                    .i18nKey("user.Email.send.failed")
                    .i8nArguments(failedEmails)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    private Optional<String> buildAndSendEmail(String subject, String templateName, Map<String, String> model, String userEmail) {
        try {
            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message);
            //TODO: add From here from previous #121 issue
            helper.setFrom(from);
            helper.setTo(userEmail);
            helper.setSubject(subject);
            final Template template = freemarkerConfig.getTemplate(templateName);
            final String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            helper.setText(content, true);
            mailSender.send(message);
            return Optional.empty();
        } catch (final Exception e) {
            log.error("Problem preparing or sending email to user with address {}", userEmail, e);
            return Optional.of(userEmail);
        }
    }

    private String getRegisterTemplate() {
        return "register-email-" + configuredLocale + ".ftl";
    }

    private Map<String, String> getRegisterModel(final Invitation invitation) {
        return Map.of(
                "username", getUserName(invitation.getUser()),
                "signupurl", getSignupUrl(invitation)
        );
    }

    private String getSignupUrl(final Invitation invitation) {
        return signupUrl + '/' + invitation.getCode();
    }

    private String getUserName(final User user) {
        final String USERNAME_SEPARATOR = " ";
        return Stream.of(user.getFirstName(), user.getLastName())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(USERNAME_SEPARATOR));
    }

    private String getDocumentAssignedTemplate() {
        return "document-assigned-email-" + configuredLocale + ".ftl";
    }

    private Map<String, String> getDocumentInfoMap(final DocumentMetadata documentMetadata, final User user) {
        return Map.of(
                "username", getUserName(user),
                "documentUrl", documentUrl + "/" + documentMetadata.getId(),
                "documentTitle", documentMetadata.getDocumentTitle()
        );
    }
}
