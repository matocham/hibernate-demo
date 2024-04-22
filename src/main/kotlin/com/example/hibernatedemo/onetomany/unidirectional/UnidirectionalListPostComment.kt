package com.example.hibernatedemo.onetomany.unidirectional

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = UnidirectionalListPostComment.TABLE_NAME)
class UnidirectionalListPostComment(@Column(name = "comment") var comment: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "uni_list_post_comments"
    }
}