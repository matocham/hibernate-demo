package com.example.hibernatedemo.onetomany.fetchtypes

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = EagerPostComment.TABLE_NAME)
class EagerPostComment(
    @Column(name = "comment") var comment: String): BaseEntity() {

    companion object {
        const val TABLE_NAME = "eager_post_comments"
    }
    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: EagerPost? = null
}