package com.example.hibernatedemo.single

import jakarta.persistence.*

@Entity
@Table(name = PrimitiveIdPost.TABLE_NAME)
class PrimitiveIdPost(title: String) {
    companion object {
        const val TABLE_NAME = "primitive_posts"
    }

    @Id
    var id: Long = 0

    @Column(name = "title", length = 30)
    var title: String? = title
}