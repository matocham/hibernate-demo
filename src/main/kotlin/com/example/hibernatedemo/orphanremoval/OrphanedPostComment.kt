package com.example.hibernatedemo.orphanremoval

import jakarta.persistence.*

@Entity
@Table(name = "orphaned_post_comments")
class OrphanedPostComment() {
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
    var post: PostWithOrphanRemoval? = null
}