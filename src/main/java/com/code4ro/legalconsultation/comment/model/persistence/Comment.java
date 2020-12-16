package com.code4ro.legalconsultation.comment.model.persistence;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import com.code4ro.legalconsultation.core.model.persistence.BaseEntity;
import com.code4ro.legalconsultation.authentication.model.persistence.ApplicationUser;
import com.code4ro.legalconsultation.document.node.model.persistence.DocumentNode;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@Setter
public class Comment extends BaseEntity {
    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_node_id")
    private DocumentNode documentNode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private ApplicationUser owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Comment> replies;

    @Column(name = "last_edit_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date lastEditDateTime;

    @Column(name = "status")
    private CommentStatus status;
}
