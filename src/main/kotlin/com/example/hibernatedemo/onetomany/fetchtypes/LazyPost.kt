package com.example.hibernatedemo.onetomany.fetchtypes

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = LazyPost.TABLE_NAME)
class LazyPost(title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "lazy_posts"
    }
    @Column(name = "title", length = 30)
    var title: String? = title

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var comments: MutableList<LazyPostComment> = mutableListOf()

    fun addComment(comment: LazyPostComment) {
        comments += comment
        comment.post = this
    }

    fun removeComment(commentId: Long) {
        val comment = comments.find { it.id == commentId }
        comment?.let {
            it.post = null
            comments.remove(comment)
        }
    }
}