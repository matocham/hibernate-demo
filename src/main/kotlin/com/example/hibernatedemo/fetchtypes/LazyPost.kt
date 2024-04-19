package com.example.hibernatedemo.fetchtypes

import jakarta.persistence.*

@Entity
@Table(name = "lazy_posts")
class LazyPost() {

    constructor(title: String): this() {
        this.title = title
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "title", length = 30)
    var title: String? = null

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