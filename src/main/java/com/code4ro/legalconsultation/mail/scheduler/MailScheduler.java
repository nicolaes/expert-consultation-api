package com.code4ro.legalconsultation.mail.scheduler;

import com.code4ro.legalconsultation.core.exception.LegalValidationException;
import com.code4ro.legalconsultation.document.configuration.model.persistence.DocumentConfiguration;
import com.code4ro.legalconsultation.document.configuration.repository.DocumentConfigurationRepository;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.consolidated.repository.DocumentConsolidatedRepository;
import com.code4ro.legalconsultation.mail.service.MailApi;
import com.code4ro.legalconsultation.user.model.persistence.User;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MailScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(MailScheduler.class);

    private final MailApi mailApi;
    private final DocumentConsolidatedRepository documentConsolidatedRepository;
    private final DocumentConfigurationRepository documentConfigurationRepository;

    @Autowired
    public MailScheduler(MailApi mailApi, DocumentConsolidatedRepository documentConsolidatedRepository, DocumentConfigurationRepository documentConfigurationRepository) {
        this.mailApi = mailApi;
        this.documentConsolidatedRepository = documentConsolidatedRepository;
        this.documentConfigurationRepository = documentConfigurationRepository;
    }

    @Scheduled(cron = "0 0 6 * * *")
    @SchedulerLock(name = "sendMails")
    public void sendDocumentConsultationMails() {
        LOG.info("Sending document consultation e-mails");
        AtomicReference<Integer> emailCount = new AtomicReference<>(0);

        List<DocumentConsolidated> documents = documentConsolidatedRepository.findAllInConsultation();
        documents.forEach(document -> {
            List<User> userList = documentConsolidatedRepository.findUsersByDocumentId(document.getId());
            emailCount.updateAndGet(v -> v + userList.size());
            try {
                mailApi.sendDocumentConsultationEmail(document.getDocumentMetadata(), userList);
                DocumentConfiguration configuration = document.getDocumentConfiguration();
                configuration.setConsultationEmailsSent(true);
                documentConfigurationRepository.save(configuration);
            } catch (LegalValidationException e) {
                LOG.error("Error sending emails to {}", String.join(", ", e.getI8nArguments()), e);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });

        LOG.info("Sent e-mails for {} documents in consultation, {} emails", documents.size(), emailCount);
    }
}
