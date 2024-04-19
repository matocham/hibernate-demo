package com.example.hibernatedemo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class HibernateDemoApplication

fun main(args: Array<String>) {
    SpringApplication.run(HibernateDemoApplication::class.java, *args)
}
