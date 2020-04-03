package fr.gwa.seed.services.impl

import com.google.firebase.auth.FirebaseToken
import fr.gwa.seed.db.MemberRepository
import fr.gwa.seed.model.Member
import fr.gwa.seed.security.Role
import fr.gwa.seed.services.MemberService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class MemberServiceImpl : MemberService {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    override fun save(member: Member): Mono<Member> {
        return memberRepository.save(member)
    }

    override fun findByToken(token: String): Mono<Member> {
        return memberRepository.findById(token)
                .defaultIfEmpty(Member(tokenUid = token))
    }

    /**
     * Return the member object of the current authenticated user
     * Create it if not exist
     */
    override fun findByFirebaseToken(token: FirebaseToken): Mono<Member> {
        return Mono.zip(Mono.just(token), findByToken(token.uid))
                .flatMap { tuple ->
                    val details = tuple.t1
                    val member = tuple.t2
                    var updated = false

                    // Update datas
                    if (member.name != details.name) {
                        member.name = details.name
                        updated = true
                    }
                    if (member.role == null) {
                        member.role = Role.MEMBER.name
                        updated = true
                    }

                    if (updated) {
                        save(member)
                    } else {
                        Mono.just(member)
                    }
                }
    }

    override fun findAll(): Flux<Member> {
        return memberRepository.findAll()
    }

    override fun deleteById(tokenUid: String): Mono<Void> {
        return memberRepository.deleteById(tokenUid)
    }

}
