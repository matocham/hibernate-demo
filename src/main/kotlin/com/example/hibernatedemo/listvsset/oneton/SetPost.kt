package com.example.hibernatedemo.listvsset.oneton

import jakarta.persistence.*

@Entity
@Table(name = "set_posts")
class SetPost() {

    constructor(title: String): this() {
        this.title = title
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "title", length = 30)
    var title: String? = null

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
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