package com.example.hibernatedemo.spring

import com.example.hibernatedemo.single.ManualStringIdPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ManualStringIdPostRepository: JpaRepository<ManualStringIdPost, Long?>