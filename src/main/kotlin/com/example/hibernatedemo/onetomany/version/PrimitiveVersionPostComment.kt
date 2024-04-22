package com.example.hibernatedemo.onetomany.version

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = PrimitiveVersionPostComment.TABLE_NAME)
class PrimitiveVersionPostComment(@Column(name = "comment") var comment: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "primitive_ver_post_comments"
    }

    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: PrimitiveVersionPost? = null
}