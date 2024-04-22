package com.example.hibernatedemo.onetomany.set

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = SetPost.TABLE_NAME)
class SetPost(title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "set_posts"
    }
    @Column(name = "title", length = 30)
    var title: String? = title

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableSet<SetPostComment> = mutableSetOf()

    fun addComment(comment: SetPostComment) {
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