package com.example.hibernatedemo.onetomany.sortcolumn

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = OrderColPost.TABLE_NAME)
class OrderColPost(title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "order_col_posts"
    }
    @Column(name = "title", length = 30)
    var title: String? = title

    // by default hibernate uses bag for lists so they are unordered and list/set behaves in the same way
    // to change it add OrderColumn annotation
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderColumn(name = "additional_order_col")
    var comments: MutableList<OrderColPostComment> = mutableListOf()

    fun addComment(comment: OrderColPostComment) {
        comments.add(comment)
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