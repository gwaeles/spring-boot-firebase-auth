package fr.gwa.seed.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import fr.gwa.seed.security.AuthenticationManager
import fr.gwa.seed.security.Role
import fr.gwa.seed.security.SecurityContextRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.io.IOException
import java.util.*

/**
 * @author Gwa
 */
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    @Autowired
    private val authenticationManager: AuthenticationManager? = null

    @Autowired
    private val securityContextRepository: SecurityContextRepository? = null

    val properties: Properties by lazy {
        Properties().apply {
            load(ClassPathResource("/application.properties").inputStream)
        }
    }

    /**
     * Init firebase SDK with credentials and database informations
     */
    @Primary
    @Bean
    @Throws(IOException::class)
    fun firebaseInit() {

        if (FirebaseApp.getApps().isEmpty()) {
            println("[GWA] Init Firebase App ...")

            val databaseUrl = properties.getProperty("firebase.databaseUrl")
            var credentialsPath = properties.getProperty("spring.cloud.gcp.firestore.credentials.location")

            if (credentialsPath.startsWith("classpath:")) {
                credentialsPath = credentialsPath.substring(10)
            }

            val options = FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials
                            .fromStream(ClassPathResource(credentialsPath).inputStream))
                    .setDatabaseUrl(databaseUrl)
                    .build()

            FirebaseApp.initializeApp(options)
            println("[GWA] Firebase App initialized")
        }
    }

    /**
     * Define security strategy of different entries points
     */
    @Bean
    fun securitygWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
                .exceptionHandling()
                .authenticationEntryPoint { swe: ServerWebExchange, _: AuthenticationException? -> Mono.fromRunnable { swe.response.statusCode = HttpStatus.UNAUTHORIZED } }.accessDeniedHandler { swe: ServerWebExchange, _: AccessDeniedException? -> Mono.fromRunnable { swe.response.statusCode = HttpStatus.FORBIDDEN } }.and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/").permitAll()
                .pathMatchers("/auth").hasAnyRole(Role.ADMIN.name)
                .anyExchange().authenticated()
                .and().build()
    }
}