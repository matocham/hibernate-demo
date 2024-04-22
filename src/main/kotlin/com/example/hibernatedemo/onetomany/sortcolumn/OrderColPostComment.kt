package com.example.hibernatedemo.onetomany.sortcolumn

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = OrderColPostComment.TABLE_NAME)
class OrderColPostComment(@Column(name = "comment") var comment: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "order_col_post_comments"
    }
    @ManyToOne
    @JoinColumn(name = "post_id")
    var post: OrderColPost? = null
}