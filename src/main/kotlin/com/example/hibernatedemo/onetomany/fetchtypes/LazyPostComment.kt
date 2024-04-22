package com.example.hibernatedemo.onetomany.fetchtypes

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = LazyPostComment.TABLE_NAME)
class LazyPostComment(@Column(name = "comment") var comment: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "lazy_post_comments"
    }
    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: LazyPost? = null
}