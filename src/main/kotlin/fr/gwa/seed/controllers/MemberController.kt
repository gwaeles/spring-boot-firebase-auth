package fr.gwa.seed.controllers

import com.google.firebase.auth.FirebaseToken
import fr.gwa.seed.model.Member
import fr.gwa.seed.services.MemberService
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@OpenAPIDefinition(
        info = Info(
                title = "Seed",
                version = "1.0",
                description = "Seed application"
        )
)
@RestController
@RequestMapping("/members")
@Tag(name = "members")
class MemberController {

    @Autowired
    lateinit var memberService: MemberService

    @Operation(summary = "Get the list of members.")
    @GetMapping("/", produces = ["application/json"])
    fun getMembers(): Flux<Member> {
        return memberService.findAll()
    }

    @Operation(summary = "Get the member object of the current authenticated user.")
    @GetMapping("/me", produces = ["application/json"])
    fun getMe(): Mono<Member> {
        return getAuthenticationDetails()
                .flatMap { details ->
                    memberService.findByFirebaseToken(details)
                }
                .flatMap { member ->
                    Mono.just(member)
                }
    }

    /**
     * Just for dev
     */
    @Operation(summary = "Get the FirebaseToken object of the current authenticated user.")
    @GetMapping("/auth", produces = ["application/json"])
    fun getAuth(): Mono<FirebaseToken> {
        return getAuthenticationDetails()
                .flatMap { details ->
                    Mono.just(details)
                }
    }

    fun getAuthenticationDetails(): Mono<FirebaseToken> {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap { auth ->
                    Mono.just(auth.authentication.details as FirebaseToken)
                }
    }
}