package com.example.hibernatedemo.onetomany.list

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = ListPost.TABLE_NAME)
class ListPost(title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "list_posts"
    }
    @Column(name = "title", length = 30)
    var title: String? = title

    // by default hibernate uses bag for lists so they are unordered and list/set behaves in the same way
    // to change it add OrderColumn annotation
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<ListPostComment> = mutableListOf()

    fun addComment(comment: ListPostComment) {
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
//https://thorben-janssen.com/ultimate-guide-association-mappings-jpa-hibernate/