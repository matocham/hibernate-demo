package com.example.hibernatedemo.onetomany.nocascade

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = NoCascadePost.TABLE_NAME)
class NoCascadePost(title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "no_cascade_posts"
    }
    @Column(name = "title", length = 30)
    var title: String? = title

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    var comments: MutableList<NoCascadePostComment> = mutableListOf()

    fun addComment(comment: NoCascadePostComment) {
        comments += comment
        comment.post = this
    }
}