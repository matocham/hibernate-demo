package com.example.hibernatedemo.listvsset.oneton

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "set_post_comments")
class SetPostComment() {
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
    var post: SetPost? = null

    /**
     * Return false when
     * - if objects are equal by reference then return true
     * - other object is null
     * - other object is not the same class
     * - id is null on the current object (this way two transient entities are not equal)
     * - ids don't match
     */
    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        }
        if(other == null || other !is SetPostComment || id == null) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}