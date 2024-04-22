package com.example.hibernatedemo.onetomany.orphanremoval

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = PostWithOrphanRemoval.TABLE_NAME)
class PostWithOrphanRemoval(title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "orphan_removal_posts"
    }
    @Column(name = "title", length = 30)
    var title: String? = title

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<OrphanedPostComment> = mutableListOf()

    fun addComment(comment: OrphanedPostComment) {
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