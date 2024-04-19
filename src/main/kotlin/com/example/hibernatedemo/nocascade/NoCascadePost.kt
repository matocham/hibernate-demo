package com.example.hibernatedemo.nocascade

import jakarta.persistence.*

@Entity
@Table(name = "no_cascade_posts")
class NoCascadePost() {

    constructor(title: String): this() {
        this.title = title
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "title", length = 30)
    var title: String? = null

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    var comments: MutableList<NoCascadePostComment> = mutableListOf()

    fun addComment(comment: NoCascadePostComment) {
        comments += comment
        comment.post = this
    }
}