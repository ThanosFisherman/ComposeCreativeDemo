package io.github.thanosfisherman.demo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform