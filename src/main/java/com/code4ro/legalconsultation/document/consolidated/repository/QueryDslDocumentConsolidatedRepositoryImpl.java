package com.code4ro.legalconsultation.document.consolidated.repository;

import com.code4ro.legalconsultation.document.configuration.model.persistence.QDocumentConfiguration;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.QDocumentConsolidated;
import com.code4ro.legalconsultation.user.model.persistence.QUser;
import com.code4ro.legalconsultation.user.model.persistence.User;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.ListPath;
import com.querydsl.jpa.JPQLQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

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
                .select(Projections.fields(dc, dc.id, dConf, dc.documentMetadata))
                .from(dc)
                .innerJoin(dConf)
                .where(dConf.consultationStartDate.loe(Expressions.currentDate())
                        .and(dConf.consultationDeadline.goe(Expressions.currentDate())
                                .or(dConf.consultationDeadline.isNull()))
                        .and(dConf.excludedFromConsultation.isFalse())
                        .and(dConf.consultationEmailsSent.isFalse())
                )
                .fetch();
    }

    @Override
    public List<User> findUsersByDocumentId(UUID documentId) {
        QDocumentConsolidated dc = QDocumentConsolidated.documentConsolidated;
        ListPath<User, QUser> au = dc.assignedUsers;
        return queryFactory
                .select(Projections.fields(dc, au))
                .from(dc)
                .innerJoin(au)
                .where(dc.id.eq(documentId))
                .fetchOne().getAssignedUsers();
    }
}
