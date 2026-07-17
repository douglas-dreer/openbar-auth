package com.openbar.pm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpenBarPmApplication

fun main(args: Array<String>) {
    runApplication<OpenBarPmApplication>(*args)
}
