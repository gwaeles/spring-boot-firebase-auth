package fr.gwa.seed.services

import com.google.firebase.auth.FirebaseToken
import fr.gwa.seed.model.Member
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MemberService {

    fun save(member: Member): Mono<Member>

    fun findByToken(token: String): Mono<Member>

    fun findByFirebaseToken(token: FirebaseToken): Mono<Member>

    fun findAll(): Flux<Member>

    fun deleteById(tokenUid: String): Mono<Void>
}