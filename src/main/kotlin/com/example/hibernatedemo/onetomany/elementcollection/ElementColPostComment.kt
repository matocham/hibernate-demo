package com.example.hibernatedemo.onetomany.elementcollection

import jakarta.persistence.*
import org.hibernate.Hibernate
import java.util.*

@Embeddable
class ElementColPostComment(
    @Column(nullable = false)
    var comment: String,
    @Column(nullable = false)
    var author: String) { // not nullable columns may be created to create primary key

    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        }
        if(other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false
        }
        other as ElementColPostComment
        return Objects.equals(comment, other.comment) && Objects.equals(author, other.author)
    }
    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}