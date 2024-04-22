package com.example.hibernatedemo.single

import jakarta.persistence.*

@Entity
@Table(name = ManualStringIdPost.TABLE_NAME)
class ManualStringIdPost(id: String, title: String) {
    companion object {
        const val TABLE_NAME = "manual_str_posts"
    }

    @Id
    var id: String? = id

    @Column(name = "title", length = 30)
    var title: String? = title
}