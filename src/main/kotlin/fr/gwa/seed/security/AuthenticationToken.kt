package fr.gwa.seed.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

/**
 * @author gwa
 *
 * Custom AuthenticationToken to add grant type info
 */
class AuthenticationToken(val grantType: GrantType, principal: Any, credentials: Any): UsernamePasswordAuthenticationToken(principal, credentials)