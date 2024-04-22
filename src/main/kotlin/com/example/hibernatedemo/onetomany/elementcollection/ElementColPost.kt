package com.example.hibernatedemo.onetomany.elementcollection

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = ElementColPost.TABLE_NAME)
class ElementColPost(title: String) : BaseEntity() {
    companion object {
        const val TABLE_NAME: String = "element_col_posts"
        const val CHILD_TABLE_NAME: String = "element_col_post_comments"
        const val TAGS_TABLE_NAME: String = "element_col_post_tags"
    }

    @Column(name = "title", length = 30)
    var title: String? = title

    @ElementCollection // lazy by default
    @Column(nullable = false) // make it not nullable to optimize primary key creation
    @CollectionTable(name = CHILD_TABLE_NAME, joinColumns = [JoinColumn(name = "post_id")])
    var comments: MutableList<ElementColPostComment> = mutableListOf()

    @ElementCollection
    @Column(nullable = false) // make it not nullable to optimize primary key creation
    @CollectionTable(name = TAGS_TABLE_NAME, joinColumns = [JoinColumn(name = "post_id")])
    var tags: MutableList<String> = mutableListOf()
}