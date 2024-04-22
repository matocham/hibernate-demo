package com.example.hibernatedemo.spring

import com.example.hibernatedemo.single.PrimitiveIdPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PrimitiveIdPostRepository: JpaRepository<PrimitiveIdPost, Long>