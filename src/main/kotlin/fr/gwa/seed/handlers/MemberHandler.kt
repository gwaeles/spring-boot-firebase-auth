package fr.gwa.seed.handlers

import com.google.firebase.auth.FirebaseToken
import fr.gwa.seed.model.Member
import fr.gwa.seed.security.Role
import fr.gwa.seed.services.MemberService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

/**
 * @author gwa
 *
 * Features around members
 */
@Component
class MemberHandler {

    @Autowired
    private lateinit var memberService: MemberService

    fun getAuthenticationDetails(request: ServerRequest): Mono<FirebaseToken> {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap { auth ->
                    Mono.just(auth.authentication.details as FirebaseToken)
                }
    }

    fun auth(request: ServerRequest): Mono<ServerResponse> {

        return getAuthenticationDetails(request)
                .flatMap { details ->
                    ServerResponse.ok().body(Mono.just(details))
                }
    }

    /**
     * Return the member object of the current authenticated user
     * Create it if not exist
     */
    fun me(request: ServerRequest): Mono<ServerResponse> {

        return getAuthenticationDetails(request)
                .flatMap { details ->
                    memberService.findByFirebaseToken(details)
                }
                .flatMap { member ->
                    ServerResponse.ok().body(Mono.just(member))
                }
    }


    fun deleteme(request: ServerRequest): Mono<ServerResponse> {

        return getAuthenticationDetails(request)
                .flatMapMany { details ->
                    memberService.deleteById(details.uid)
                }
                .collectList()
                .flatMap {
                    ServerResponse.ok().body(Mono.just("OK"))
                }
    }


    fun getAll(request: ServerRequest): Mono<ServerResponse> {

        return memberService.findAll()
                .collectList()
                .flatMap { ServerResponse.ok().body(BodyInserters.fromValue(it)) }
                .onErrorResume {
                    ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(it))
                }
    }
}