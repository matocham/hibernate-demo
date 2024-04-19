package com.example.hibernatedemo.unidirectional

import jakarta.persistence.*

@Entity
@Table(name = "uni_list_post_comments")
class UnidirectionalListPostComment() {
    constructor(comment: String): this() {
        this.comment = comment
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name="comment")
    lateinit var comment: String

    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        }
        if(other == null || other !is UnidirectionalListPostComment || id == null) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}