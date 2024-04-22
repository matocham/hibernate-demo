package com.example.hibernatedemo.spring

import com.example.hibernatedemo.onetomany.version.VersionIdentityPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VersionIdentityPostRepository: JpaRepository<VersionIdentityPost, Long?>