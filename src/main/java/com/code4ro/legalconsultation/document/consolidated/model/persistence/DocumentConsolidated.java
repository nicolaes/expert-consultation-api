package com.code4ro.legalconsultation.document.consolidated.model.persistence;

import com.code4ro.legalconsultation.core.model.persistence.BaseEntity;
import com.code4ro.legalconsultation.document.configuration.model.persistence.DocumentConfiguration;
import com.code4ro.legalconsultation.document.metadata.model.persistence.DocumentMetadata;
import com.code4ro.legalconsultation.document.node.model.persistence.DocumentNode;
import com.code4ro.legalconsultation.user.model.persistence.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "consolidated_document")
@Getter
@Setter
public class DocumentConsolidated extends BaseEntity {

    @OneToOne(fetch = FetchType.EAGER, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "metadata_id")
    private DocumentMetadata documentMetadata;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "document_node_id")
    private DocumentNode documentNode;

    @OneToOne(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "documentConsolidated")
    private DocumentConfiguration documentConfiguration;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "document_users_assignment",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> assignedUsers;

    public DocumentConsolidated(final DocumentMetadata documentMetadata,
                                final DocumentNode documentNode,
                                final DocumentConfiguration documentConfiguration) {
        this.documentMetadata = documentMetadata;
        this.documentNode = documentNode;
        this.documentConfiguration = documentConfiguration;
    }

    public DocumentConsolidated(final DocumentMetadata documentMetadata,
                                final DocumentNode documentNode) {
        this.documentMetadata = documentMetadata;
        this.documentNode = documentNode;
    }

    public DocumentConsolidated() {
    }
}
