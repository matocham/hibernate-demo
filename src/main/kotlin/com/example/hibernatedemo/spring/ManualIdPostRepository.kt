package com.example.hibernatedemo.spring

import com.example.hibernatedemo.single.ManualIdPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ManualIdPostRepository: JpaRepository<ManualIdPost, Long?>