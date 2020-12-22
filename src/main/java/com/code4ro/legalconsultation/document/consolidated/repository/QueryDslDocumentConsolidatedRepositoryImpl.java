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
        QDocumentConsolidated dc = QDocumentConsolidated.documentConsolidated;
        QDocumentConfiguration dConf = dc.documentConfiguration;
        return queryFactory
                .select(dc, dConf, dc.documentMetadata)
                .from(dc)
                .innerJoin(dConf)
                .where(dConf.consultationStartDate.loe(Expressions.currentDate())
                        .and(dConf.consultationDeadline.goe(Expressions.currentDate())
                                .or(dConf.consultationDeadline.isNull()))
                        .and(dConf.excludedFromConsultation.isFalse())
                        .and(dConf.consultationEmailsSent.isFalse())
                )
                .fetch().stream()
                .map(tuple -> {
                    DocumentConsolidated documentConsolidated = Objects.requireNonNull(tuple.get(dc));
                    documentConsolidated.setDocumentMetadata(tuple.get(dc.documentMetadata));
                    documentConsolidated.setDocumentConfiguration(tuple.get(dConf));
                    return documentConsolidated;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findUsersByDocumentId(UUID documentId) {
        QDocumentConsolidated dc = QDocumentConsolidated.documentConsolidated;
        QUser u = QUser.user;
        return queryFactory
                .from(dc, u)
                .where(
                        dc.assignedUsers.contains(u),
                        dc.id.eq(documentId))
                .transform(groupBy(dc.id).as(list(u)))
                .get(documentId);
    }
}
