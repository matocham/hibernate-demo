package com.example.hibernatedemo.single

import com.example.hibernatedemo.base.ComparableEntity
import jakarta.persistence.*

@Entity
@Table(name = ManualIdPost.TABLE_NAME)
class ManualIdPost(id: Long, title: String): ComparableEntity() {
    companion object {
        const val TABLE_NAME = "manual_posts"
    }

    @Id
    override var id: Long? = id

    @Column(name = "title", length = 30)
    var title: String? = title
}