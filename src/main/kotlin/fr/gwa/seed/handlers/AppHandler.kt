package fr.gwa.seed.handlers

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono


@Component
class AppHandler {

    fun home(request: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().body(Mono.just("Hello Happy World :-) v9"))
    }
}