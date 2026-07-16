@file:Suppress("SpreadOperator")

package com.openbar.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OpenBarAuthApplication

fun main(args: Array<String>) {
    runApplication<OpenBarAuthApplication>(*args)
}
