package com.redmath.GymManagementApp.config;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;

@Configuration
@EnableMethodSecurity
@OpenAPIDefinition(
        info = @Info(title = "Gym Management API", version = "v1"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtEncoder jwtEncoder) throws Exception {
        http
                .formLogin(config -> config.successHandler((request, response, auth) -> {
                    long expirySeconds = 3600;
                    var jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
                    var claims = JwtClaimsSet.builder()
                            .subject(auth.getName())
                            .expiresAt(Instant.now().plusSeconds(expirySeconds))
                            .build();

                    Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims));
                    String tokenJson = "{\"token_type\":\"Bearer\",\"access_token\":\"" + jwt.getTokenValue()
                            + "\",\"expires_in\":" + expirySeconds + "}";
                    response.setContentType("application/json");
                    response.getWriter().print(tokenJson);
                }))
                .oauth2ResourceServer(config -> config.jwt(jwtConfig -> jwtConfig.jwtAuthenticationConverter(jwt -> {
                    var user = userDetailsService.loadUserByUsername(jwt.getSubject());
                    return new JwtAuthenticationToken(jwt, user.getAuthorities());
                })))

                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/swagger-ui/**", "/v3/api-docs/**", "/login").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/news/**").permitAll()

                        // Admin full access
                        .requestMatchers("/**").hasRole("ADMIN")

                        // Trainer limited access
                        .requestMatchers(HttpMethod.GET, "/trainers", "/trainers/{id}").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET, "/members").hasRole("TRAINER")
                        .requestMatchers(HttpMethod.GET, "/members/by-trainer/**").hasRole("TRAINER")

                        // Member access
                        .requestMatchers(HttpMethod.GET, "/members/me").hasRole("MEMBER")
                        .requestMatchers(HttpMethod.GET, "/trainers").hasRole("MEMBER")

                        .anyRequest().authenticated()
                )

                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/login", "/h2-console/**")
                );

        return http.build();
    }

    @Bean
    public JwtEncoder jwtEncoder(@Value("${jwt.signing.key}") byte[] signingKey) {
        SecretKeySpec key = new SecretKeySpec(signingKey, "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.signing.key}") byte[] signingKey) {
        SecretKeySpec key = new SecretKeySpec(signingKey, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
