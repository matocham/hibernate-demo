package com.example.hibernatedemo.spring

import com.example.hibernatedemo.onetomany.version.VersionSeqPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VersionSeqPostRepository: JpaRepository<VersionSeqPost, Long?>