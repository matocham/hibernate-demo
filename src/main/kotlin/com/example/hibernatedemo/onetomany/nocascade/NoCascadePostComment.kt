package com.example.hibernatedemo.onetomany.nocascade

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = NoCascadePostComment.TABLE_NAME)
class NoCascadePostComment(@Column(name = "comment") var comment: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "no_cascade_post_comments"
    }
    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: NoCascadePost? = null
}