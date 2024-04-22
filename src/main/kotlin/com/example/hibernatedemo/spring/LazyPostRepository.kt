package com.example.hibernatedemo.spring

import com.example.hibernatedemo.onetomany.fetchtypes.LazyPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LazyPostRepository: JpaRepository<LazyPost, Long?>