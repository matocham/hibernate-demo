package com.example.hibernatedemo.fetchtypes

import jakarta.persistence.*

@Entity
@Table(name = "eager_post_comments")
class EagerPostComment() {
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
    var post: EagerPost? = null
}