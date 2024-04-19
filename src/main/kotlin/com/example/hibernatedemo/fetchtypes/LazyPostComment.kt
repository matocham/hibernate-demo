package com.example.hibernatedemo.fetchtypes

import jakarta.persistence.*

@Entity
@Table(name = "lazy_post_comments")
class LazyPostComment() {
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
    var post: LazyPost? = null
}