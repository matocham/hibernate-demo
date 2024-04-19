package com.example.hibernatedemo.nocascade

import jakarta.persistence.*

@Entity
@Table(name = "no_cascade_post_comments")
class NoCascadePostComment() {
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
    var post: NoCascadePost? = null
}