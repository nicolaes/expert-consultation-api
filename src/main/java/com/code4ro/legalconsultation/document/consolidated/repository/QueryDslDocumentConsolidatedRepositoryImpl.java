package com.code4ro.legalconsultation.document.consolidated.repository;

import com.code4ro.legalconsultation.document.configuration.model.persistence.QDocumentConfiguration;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.QDocumentConsolidated;
import com.code4ro.legalconsultation.user.model.persistence.QUser;
import com.code4ro.legalconsultation.user.model.persistence.User;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import org.springframework.stereotype.Repository;
import static com.querydsl.core.group.GroupBy.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class QueryDslDocumentConsolidatedRepositoryImpl implements QueryDslDocumentConsolidatedRepository {
    private final JPQLQueryFactory queryFactory;

    public QueryDslDocumentConsolidatedRepositoryImpl(JPQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<DocumentConsolidated> findAllInConsultationForEmailNotification() {
        QDocumentConsolidated qDocumentConsolidated = QDocumentConsolidated.documentConsolidated;
        QDocumentConfiguration qDocumentConfiguration = qDocumentConsolidated.documentConfiguration;
        return queryFactory
                .select(qDocumentConsolidated, qDocumentConfiguration, qDocumentConsolidated.documentMetadata)
                .from(qDocumentConsolidated)
                .innerJoin(qDocumentConfiguration)
                .where(qDocumentConfiguration.consultationStartDate.loe(Expressions.currentDate())
                        .and(qDocumentConfiguration.consultationDeadline.goe(Expressions.currentDate())
                                .or(qDocumentConfiguration.consultationDeadline.isNull()))
                        .and(qDocumentConfiguration.excludedFromConsultation.isFalse())
                        .and(qDocumentConfiguration.consultationEmailsSent.isFalse())
                )
                .fetch().stream()
                .map(tuple -> {
                    DocumentConsolidated documentConsolidated = Objects.requireNonNull(tuple.get(qDocumentConsolidated));
                    documentConsolidated.setDocumentMetadata(tuple.get(qDocumentConsolidated.documentMetadata));
                    documentConsolidated.setDocumentConfiguration(tuple.get(qDocumentConfiguration));
                    return documentConsolidated;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findUsersByDocumentId(UUID documentId) {
        QDocumentConsolidated qDocumentConsolidated = QDocumentConsolidated.documentConsolidated;
        QUser qUser = QUser.user;
        return queryFactory
                .from(qDocumentConsolidated, qUser)
                .where(
                        qDocumentConsolidated.assignedUsers.contains(qUser),
                        qDocumentConsolidated.id.eq(documentId))
                .transform(groupBy(qDocumentConsolidated.id).as(list(qUser)))
                .get(documentId);
    }
}
