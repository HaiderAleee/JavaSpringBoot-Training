package com.redmath.newsproject.Config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.redmath.newsproject.Model.User;
import com.redmath.newsproject.Repository.UserRepository;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.List;

@OpenAPIDefinition(info = @Info(title = "News API", version = "v1"), security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@EnableMethodSecurity
@Configuration
public class ApiSecurityConfiguration {

    private final UserRepository userRepo;

    public ApiSecurityConfiguration(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userService, JwtEncoder jwtEncoder)
            throws Exception {
        http.formLogin(config -> config.successHandler((request, response, auth) -> {
            long expirySeconds = 3600;
            JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
            JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                    .subject(auth.getName())
                    .expiresAt(Instant.now().plusSeconds(expirySeconds))
                    .build();
            Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet));
            String tokenResponse = "{\"token_type\":\"Bearer\",\"access_token\":\"" + jwt.getTokenValue()
                    + "\",\"expires_in\":" + expirySeconds + "}";
            response.getWriter().print(tokenResponse);
        }));

        http.oauth2ResourceServer(config -> config.jwt(jwtConfig -> jwtConfig.jwtAuthenticationConverter(jwt -> {
            UserDetails user = userService.loadUserByUsername(jwt.getSubject());
            return new JwtAuthenticationToken(jwt, user.getAuthorities());
        })));

        http.sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(config -> config
                .requestMatchers("/error", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/news").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/news/**").permitAll()
                .anyRequest().authenticated());

        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers("/login")
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
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority(user.getRole())
            );

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    authorities
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
