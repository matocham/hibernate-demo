package com.example.hibernatedemo.onetomany.unidirectional

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = UniNoJoinColumnPostComment.TABLE_NAME)
class UniNoJoinColumnPostComment(@Column(name = "comment") var comment: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "uni_no_join_column_post_comments"
    }
}