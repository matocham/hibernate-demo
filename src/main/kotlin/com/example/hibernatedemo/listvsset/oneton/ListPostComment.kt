package com.example.hibernatedemo.listvsset.oneton

import jakarta.persistence.*

@Entity
@Table(name = "list_post_comments")
class ListPostComment() {
    constructor(comment: String): this() {
        this.comment = comment
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name="comment")
    lateinit var comment: String

    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: ListPost? = null
}