package com.project.kuru.screen

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.router

@Configuration
class Router () {


    @Bean
    fun uploadRoutes(handler: Handler): RouterFunction<ServerResponse> =

        router {
            GET("/test", handler::test)
            "/uploads".nest {
                POST("/image", handler::uploadImage)
                POST("/video", handler::uploadVideo)
                POST("/ref")
            }
        }

}