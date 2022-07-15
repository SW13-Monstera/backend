package com.csbroker.apiserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class ApiServerApplication

fun main(args: Array<String>) {
    runApplication<ApiServerApplication>(*args)
}
