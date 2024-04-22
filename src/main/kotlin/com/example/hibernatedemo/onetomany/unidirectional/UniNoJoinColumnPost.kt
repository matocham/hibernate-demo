package com.example.hibernatedemo.onetomany.unidirectional

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = UniNoJoinColumnPost.TABLE_NAME)
class UniNoJoinColumnPost(title: String) : BaseEntity() {
    companion object {
        const val TABLE_NAME = "uni_no_join_col_posts"
        const val JOIN_TABLE_NAME = "uni_join_table_name"
    }

    @Column(name = "title", length = 30)
    var title: String? = title

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinTable(
        name = JOIN_TABLE_NAME,
        joinColumns = [JoinColumn(name = "post_id")],
        inverseJoinColumns = [JoinColumn(name = "post_comment_id")]
    ) // custom name for join table and its columns
    var comments: MutableList<UniNoJoinColumnPostComment> = mutableListOf()

    fun addComment(comment: UniNoJoinColumnPostComment) {
        comments += comment
    }

    fun removeComment(comment: UniNoJoinColumnPostComment) {
        comments.remove(comment)
    }
}