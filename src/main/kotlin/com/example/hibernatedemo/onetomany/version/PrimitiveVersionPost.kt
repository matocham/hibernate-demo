package com.example.hibernatedemo.onetomany.version

import com.example.hibernatedemo.base.BaseEntity
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPostComment
import jakarta.persistence.*

@Entity
@Table(name = PrimitiveVersionPost.TABLE_NAME)
class PrimitiveVersionPost(title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "primitive_ver_posts"
    }

    // this is created as primitive in JVM
    @Version
    var version: Long = 0

    @Column(name = "title", length = 30)
    var title: String? = title

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var comments: MutableList<PrimitiveVersionPostComment> = mutableListOf()

    fun addComment(comment: PrimitiveVersionPostComment) {
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