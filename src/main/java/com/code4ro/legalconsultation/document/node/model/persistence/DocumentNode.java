package com.code4ro.legalconsultation.document.node.model.persistence;

import com.code4ro.legalconsultation.core.model.persistence.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Stream;

@Entity
@Table(name = "document_nodes")
@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentNode extends BaseEntity {

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "parent")
    @ToString.Exclude
    private DocumentNode parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @OrderBy("node_index ASC")
    private List<DocumentNode> children;

    @Column(name = "document_node_type")
    private DocumentNodeType documentNodeType;

    @Column(name = "title")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "node_index")
    private Integer index;

    public Stream<DocumentNode> flattened() {
        return Stream.concat(
                Stream.of(this),
                children != null ? children.stream().flatMap(DocumentNode::flattened) : Stream.empty());
    }

}
