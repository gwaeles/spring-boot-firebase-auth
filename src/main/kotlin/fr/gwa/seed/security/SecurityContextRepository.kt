package fr.gwa.seed.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException

/**
 * @author gwa
 *
 * Parsing of request header to extract token
 */
@Component
class SecurityContextRepository : ServerSecurityContextRepository {

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    override fun save(swe: ServerWebExchange, sc: SecurityContext): Mono<Void> {
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun load(swe: ServerWebExchange): Mono<SecurityContext> {
        // Extract infos from headers
        val request = swe.request
        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        val grantType = request.headers.getFirst("grant_type")
        return if (grantType != null && authHeader != null && authHeader.startsWith("Bearer ")) {
            val authToken = authHeader.substring(7)
            val authGrantType = try {
                GrantType.valueOf(grantType)
            }
            catch (e: IllegalArgumentException) {
                GrantType.unknown
            }

            // Format infos
            val auth: Authentication = AuthenticationToken(authGrantType, authToken, authToken)

            // Processing authentication with the manager
            authenticationManager.authenticate(auth).map { authentication: Authentication? -> SecurityContextImpl(authentication) }

        } else {
            Mono.empty()
        }
    }
}