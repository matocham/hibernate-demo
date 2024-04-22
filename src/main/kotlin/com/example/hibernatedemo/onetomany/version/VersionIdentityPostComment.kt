package com.example.hibernatedemo.onetomany.version

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = VersionIdentityPostComment.TABLE_NAME)
class VersionIdentityPostComment(@Column(name = "comment") var comment: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "ver_identity_post_comments"
    }

    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: VersionIdentityPost? = null
}