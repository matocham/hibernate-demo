package com.example.hibernatedemo.onetomany.version

import com.example.hibernatedemo.base.ComparableEntity
import jakarta.persistence.*

@Entity
@Table(name = VersionSeqPost.TABLE_NAME)
class VersionSeqPost(title: String): ComparableEntity() {
    companion object {
        const val TABLE_NAME = "ver_seq_posts"
        const val SEQUENCE_NAME = "ver_seq_posts_sequence"
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ver_seq_post")
    @SequenceGenerator(
        name = "ver_seq_post",
        allocationSize = 1,
        sequenceName = SEQUENCE_NAME
    )
    override var id: Long? = null

    @Version
    var version: Long? = null

    @Column(name = "title", length = 30)
    var title: String? = title

    // by default hibernate uses bag for lists so they are unordered and list/set behaves in the same way
    // to change it add OrderColumn annotation
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<VersionSeqPostComment> = mutableListOf()

    fun addComment(comment: VersionSeqPostComment) {
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