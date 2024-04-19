package com.example.hibernatedemo.unidirectional

import jakarta.persistence.*


@Entity
@Table(name = "uni_set_posts")
class UnidirectionalSetPost() {

    constructor(title: String) : this() {
        this.title = title
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "title", length = 30)
    var title: String? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "post_id")
    var comments: MutableSet<UnidirectionalSetPostComment> = mutableSetOf()

    fun addComment(comment: UnidirectionalSetPostComment) {
        comments += comment
    }

    fun removeComment(comment: UnidirectionalSetPostComment) {
        comments.remove(comment)
    }
}