package com.example.hibernatedemo.fetchtypes

import jakarta.persistence.*

@Entity
@Table(name = "eager_posts")
class EagerPost() {

    constructor(title: String): this() {
        this.title = title
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "title", length = 30)
    var title: String? = null

    @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    var comments: MutableList<EagerPostComment> = mutableListOf()

    fun addComment(comment: EagerPostComment) {
        comments += comment
        comment.post = this
    }
}