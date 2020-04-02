package fr.gwa.seed.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import fr.gwa.seed.model.Member
import fr.gwa.seed.services.MemberService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.stream.Collectors

/**
 * @author gwa
 *
 * Proceed authentication
 */
@Component
class AuthenticationManager : ReactiveAuthenticationManager {

    @Autowired
    private lateinit var memberService: MemberService

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val authToken = authentication.credentials.toString()

        return try {
            when ((authentication as? AuthenticationToken)?.grantType) {
                GrantType.google -> {
                    // Request Firebase to validate token
                    val decodedToken = FirebaseAuth.getInstance().verifyIdToken(authToken)
                    val userId = decodedToken.uid

                    // Request Member Service to retrieve user's role
                    memberService.findByToken(userId)
                            .flatMap { member ->
                                val authorities = getAuthoritiesForToken(member)
                                val auth = UsernamePasswordAuthenticationToken(
                                        userId,
                                        null,
                                        authorities.stream().map { role: String? -> SimpleGrantedAuthority(role) }.collect(Collectors.toList())
                                )
                                auth.details = getDetailsForToken(decodedToken)
                                Mono.just(auth)
                            }
                }
                else -> {
                    Mono.empty()
                }
            }
        } catch (e: FirebaseAuthException) {
            Mono.empty()
        } catch (e: IllegalArgumentException) {
            Mono.empty()
        }
    }

    protected fun getDetailsForToken(token: FirebaseToken): Any {
        return token
    }

    protected fun getAuthoritiesForToken(member: Member?): List<String> {
        return when (member?.role) {
            // Add ROLE_ prefix to manage access by roles instead of authorities
            Role.ROOT.name -> listOf("ROLE_${Role.MEMBER.name}", "ROLE_${Role.ADMIN.name}", "ROLE_${Role.ROOT.name}")
            Role.ADMIN.name -> listOf("ROLE_${Role.MEMBER.name}", "ROLE_${Role.ADMIN.name}")
            else -> listOf("ROLE_${Role.MEMBER.name}")
        }
    }
}