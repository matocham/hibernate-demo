package com.example.hibernatedemo.onetomany.unidirectional

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*


@Entity
@Table(name = UnidirectionalSetPost.TABLE_NAME)
class UnidirectionalSetPost(title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "uni_set_posts"
    }
    @Column(name = "title", length = 30)
    var title: String? = title

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "post_id")
    var comments: MutableSet<UnidirectionalSetPostComment> = mutableSetOf()

    fun addComment(comment: UnidirectionalSetPostComment) {
        comments += comment
    }

    fun removeComment(comment: UnidirectionalSetPostComment) {
        comments.remove(comment)
    }
}