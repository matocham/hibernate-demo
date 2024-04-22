package com.example.hibernatedemo.onetomany.sequenceid

import com.example.hibernatedemo.base.ComparableEntity
import jakarta.persistence.*

@Entity
@Table(name = SequencePostComment.TABLE_NAME)
class SequencePostComment(@Column(name = "comment") var comment: String) : ComparableEntity() {
    companion object {
        const val TABLE_NAME = "sequence_post_comments"
        const val SEQUENCE_NAME = "seq_post_comments"
    }

    // using pooled-lo algorithm because of setup in application.yaml
    // otherwise @GenericGenerator has to be used to achieve the same
    // https://vladmihalcea.com/hibernate-hidden-gem-the-pooled-lo-optimizer/
    // https://docs.jboss.org/hibernate/orm/6.3/javadocs/org/hibernate/cfg/MappingSettings.html#PREFERRED_POOLED_OPTIMIZER
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_post_comment")
    @SequenceGenerator(
        name = "seq_post_comment",
        allocationSize = 5,
        sequenceName = SEQUENCE_NAME
    )
    override var id: Long? = null

    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: SequencePost? = null
}