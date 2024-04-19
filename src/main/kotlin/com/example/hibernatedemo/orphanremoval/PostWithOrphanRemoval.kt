package com.example.hibernatedemo.orphanremoval

import jakarta.persistence.*

@Entity
@Table(name = "orphan_removal_posts")
class PostWithOrphanRemoval() {
    constructor(title: String): this() {
        this.title = title
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "title", length = 30)
    var title: String? = null

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<OrphanedPostComment> = mutableListOf()

    fun addComment(comment: OrphanedPostComment) {
        comments += comment
        comment.post = this
    }

    fun removeComment(commentId: Long) {
        val comment = comments.find { it.id == commentId }
        comment?.let {
            it.post = null
            comments.remove(comment)
        }
    }
}