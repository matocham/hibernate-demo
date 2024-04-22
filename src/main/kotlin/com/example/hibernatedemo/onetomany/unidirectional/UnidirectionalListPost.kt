package com.example.hibernatedemo.onetomany.unidirectional

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = UnidirectionalListPost.TABLE_NAME)
class UnidirectionalListPost(title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "uni_list_posts"
    }
    @Column(name = "title", length = 30)
    var title: String? = title

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "post_id") // without this a joint table would be created
    var comments: MutableList<UnidirectionalListPostComment> = mutableListOf()

    fun addComment(comment: UnidirectionalListPostComment) {
        comments += comment
    }

    fun removeComment(comment: UnidirectionalListPostComment) {
        comments.remove(comment)
    }
}