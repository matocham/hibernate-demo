package com.example.hibernatedemo.onetomany.fetchtypes

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = EagerPost.TABLE_NAME)
class EagerPost(title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "eager_posts"
    }
    @Column(name = "title", length = 30)
    var title: String? = title

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    var comments: MutableList<EagerPostComment> = mutableListOf()

    fun addComment(comment: EagerPostComment) {
        comments += comment
        comment.post = this
    }
}