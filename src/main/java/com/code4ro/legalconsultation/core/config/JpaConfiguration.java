package com.code4ro.legalconsultation.core.config;

import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class JpaConfiguration {
    private final EntityManager entityManager;

    public JpaConfiguration(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Bean
    public JPQLQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
