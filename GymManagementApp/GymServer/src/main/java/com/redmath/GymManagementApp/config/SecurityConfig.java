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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;



@Configuration
@EnableMethodSecurity
@OpenAPIDefinition(
        info = @Info(title = "Gym Management API", version = "v1"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtEncoder jwtEncoder) throws Exception {
        // Configure CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));


        http
                .formLogin(config -> config.successHandler((request, response, auth) -> {
                    long expirySeconds = 3600;
                    var jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
                    var authority = auth.getAuthorities().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException("No authority found"))
                            .getAuthority();

                    var claims = JwtClaimsSet.builder()
                            .subject(auth.getName())
                            .claim("role", authority)
                            .expiresAt(Instant.now().plusSeconds(expirySeconds))
                            .build();

                    Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims));
                    String tokenJson = "{\"token_type\":\"Bearer\",\"access_token\":\"" + jwt.getTokenValue()
                            + "\",\"expires_in\":" + expirySeconds + "}";
                    response.setContentType("application/json");
                    response.getWriter().print(tokenJson);
                }))

                .csrf(csrf -> csrf.disable())

                .oauth2ResourceServer(config -> config.jwt(jwtConfig -> jwtConfig.jwtAuthenticationConverter(jwt -> {
                    String role = jwt.getClaimAsString("role");
                    if (role == null || role.isBlank()) {
                        throw new IllegalArgumentException("Role missing in JWT");
                    }
                    return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(role)));
                })))

                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                        // Public access
                        .requestMatchers("/error", "/swagger-ui/**", "/v3/api-docs/**", "/login", "/actuator/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow preflight requests
                        .requestMatchers(HttpMethod.GET, "/api/v1/news/**").permitAll()

                        // Member & Admin access
                        .requestMatchers(HttpMethod.GET, "/trainers").hasAnyRole("MEMBER", "TRAINER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/members/me").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/trainers/{id}").hasAnyRole("TRAINER", "ADMIN")

                        // Trainer & Admin access
                        .requestMatchers(HttpMethod.GET, "/members").hasAnyRole("TRAINER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/members/by-trainer/**").hasAnyRole("TRAINER", "ADMIN")

                        // Admin full access (fallback)
                        .requestMatchers("/**").hasRole("ADMIN")

                        // Authenticated for any other requests
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // Your React app URL
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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