package com.example.hibernatedemo.listvsset.oneton

import jakarta.persistence.*

@Entity
@Table(name = "list_posts")
class ListPost() {

    constructor(title: String): this() {
        this.title = title
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "title", length = 30)
    var title: String? = null

    // by default hibernate uses bag for lists so they are unordered and list/set behaves in the same way
    // to change it add OrderColumn annotation
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
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