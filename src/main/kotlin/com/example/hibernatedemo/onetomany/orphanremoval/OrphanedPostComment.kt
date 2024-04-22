package com.example.hibernatedemo.onetomany.orphanremoval

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = OrphanedPostComment.TABLE_NAME)
class OrphanedPostComment(@Column(name = "comment") var comment: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "orphaned_post_comments"
    }
    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: PostWithOrphanRemoval? = null
}