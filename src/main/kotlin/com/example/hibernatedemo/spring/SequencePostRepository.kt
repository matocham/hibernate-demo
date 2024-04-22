package com.example.hibernatedemo.spring

import com.example.hibernatedemo.onetomany.sequenceid.SequencePost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SequencePostRepository: JpaRepository<SequencePost, Long?>