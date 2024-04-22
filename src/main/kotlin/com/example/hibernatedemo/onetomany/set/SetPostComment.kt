package com.example.hibernatedemo.onetomany.set

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = SetPostComment.TABLE_NAME)
class SetPostComment(@Column(name = "comment") var comment: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "set_post_comments"
    }
    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: SetPost? = null
}