package com.project.kuru.screen

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.router

@Configuration
class Router (private val controller: Controller) {

    @Bean
    fun uploadRoutes(): RouterFunction<ServerResponse> =

        router {
            GET("/test", controller::test)
            "/uploads".nest {
                POST("/image", controller::uploadImage)
            }
        }

}