package com.code4ro.legalconsultation.document.configuration.model.persistence;

import com.code4ro.legalconsultation.core.model.persistence.BaseEntity;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "document_configuration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentConfiguration extends BaseEntity {

    public DocumentConfiguration(Boolean openForCommenting, Boolean openForVotingComments) {
        this.openForCommenting = openForCommenting;
        this.openForVotingComments = openForVotingComments;
    }

    @Column(name = "is_open_for_commenting")
    private Boolean openForCommenting;

    @Column(name = "is_open_for_voting_comments")
    private Boolean openForVotingComments;

    @Column(name = "consultation_start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date consultationStartDate;

    @Column(name = "consultation_deadline")
    @Temporal(TemporalType.TIMESTAMP)
    private Date consultationDeadline;

    @Column(name = "is_excluded_from_consultation")
    private Boolean excludedFromConsultation;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private DocumentConsolidated documentConsolidated;
}
