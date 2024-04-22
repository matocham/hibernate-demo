package com.example.hibernatedemo.onetomany.list

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = ListPostComment.TABLE_NAME)
class ListPostComment(@Column(name = "comment") var comment: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "list_post_comments"
    }
    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: ListPost? = null
}