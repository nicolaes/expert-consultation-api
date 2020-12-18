package com.code4ro.legalconsultation.core.model.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "shedlock")
public class ShedLock {
    @Id
    @Column(length = 64, nullable = false)
    private String name;

    @Column(nullable = false)
    private Timestamp lock_until;

    @Column(nullable = false)
    private Timestamp locked_at;

    @Column(nullable = false)
    private String locked_by;
}
