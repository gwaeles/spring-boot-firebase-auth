package fr.gwa.seed.config

import fr.gwa.seed.handlers.AppHandler
import fr.gwa.seed.handlers.MemberHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.router


@Configuration
@EnableWebFlux
class WebFluxConfig: WebFluxConfigurer {

    @Bean
    fun mainRouterFunction(appHandler: AppHandler,
                           memberHandler: MemberHandler) =
            router {
                accept(MediaType.TEXT_PLAIN).nest {
                    GET("/", appHandler::home)
                }
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("/auth", memberHandler::auth)
                    GET("/me", memberHandler::me)
                    GET("/deleteme", memberHandler::deleteme)
                    GET("/members", memberHandler::getAll)
                }
            }
}