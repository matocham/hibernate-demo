package com.example.hibernatedemo.onetomany.sequenceid

import com.example.hibernatedemo.base.ComparableEntity
import jakarta.persistence.*

@Entity
@Table(name = SequencePost.TABLE_NAME)
class SequencePost(title: String): ComparableEntity() {
    companion object {
        const val TABLE_NAME = "sequence_posts"
        const val SEQUENCE_NAME = "seq_posts"
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_post")
    @SequenceGenerator(
        name = "seq_post",
        allocationSize = 1,
        sequenceName = SEQUENCE_NAME
    )
    override var id: Long? = null

    @Column(name = "title", length = 30)
    var title: String? = title

    // by default hibernate uses bag for lists so they are unordered and list/set behaves in the same way
    // to change it add OrderColumn annotation
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<SequencePostComment> = mutableListOf()

    fun addComment(comment: SequencePostComment) {
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
//https://thorben-janssen.com/ultimate-guide-association-mappings-jpa-hibernate/