package com.example.hibernatedemo.onetomany.version

import com.example.hibernatedemo.base.BaseEntity
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPostComment
import jakarta.persistence.*

@Entity
@Table(name = VersionIdentityPost.TABLE_NAME)
class VersionIdentityPost(title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "ver_identity_posts"
    }

    @Version
    var version: Long? = null

    @Column(name = "title", length = 30)
    var title: String? = title

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var comments: MutableList<VersionIdentityPostComment> = mutableListOf()

    fun addComment(comment: VersionIdentityPostComment) {
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