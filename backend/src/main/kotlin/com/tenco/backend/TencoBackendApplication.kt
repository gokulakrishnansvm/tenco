package com.tenco.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TencoBackendApplication

fun main(args: Array<String>) {
    runApplication<TencoBackendApplication>(*args)
}
