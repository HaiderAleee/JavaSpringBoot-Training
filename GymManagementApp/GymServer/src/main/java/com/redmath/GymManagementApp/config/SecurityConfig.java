package com.redmath.GymManagementApp.config;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.redmath.GymManagementApp.member.Member;
import com.redmath.GymManagementApp.member.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final MemberRepository memberRepository; //constructor injection

    public SecurityConfig(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtEncoder jwtEncoder, PasswordEncoder passwordEncoder) throws Exception {
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**")); //ignoring csrf on h2-cosole
        http
                .formLogin(config -> config.successHandler(formLoginSuccessHandler(jwtEncoder)))
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2SuccessHandler(jwtEncoder, passwordEncoder))
                ).headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .oauth2ResourceServer(config -> config.jwt(jwtConfig -> jwtConfig.jwtAuthenticationConverter(jwt -> {
                    String role = jwt.getClaimAsString("role");
                    if (role == null || role.isBlank()) {
                        throw new IllegalArgumentException("Role missing in JWT");
                    }
                    return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(role)));
                })))
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Public routes
                        .requestMatchers("/error", "/swagger-ui/**", "/v3/api-docs/**", "/login", "/actuator/**", "/oauth2/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/news/**").permitAll()

                        // GET Access
                        .requestMatchers(HttpMethod.GET, "/trainers").hasAnyRole("MEMBER", "TRAINER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/members/me").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/trainers/{id}").hasAnyRole("TRAINER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/members").hasAnyRole("TRAINER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/members/by-trainer/**").hasAnyRole("TRAINER", "ADMIN")

                        // PUT Access
                        .requestMatchers(HttpMethod.PUT, "/members/me").hasAnyRole("MEMBER", "ADMIN") // Members edit themselves
                        .requestMatchers(HttpMethod.PUT, "/members/**").hasAnyRole("MEMBER", "ADMIN") // Members edit themselves
                        .requestMatchers(HttpMethod.PUT, "/members/**").hasAnyRole("TRAINER", "ADMIN") // Trainers/Admin edit any member
                        .requestMatchers(HttpMethod.PUT, "/trainers/**").hasRole("ADMIN") // Only admin edits trainers
                        .requestMatchers(HttpMethod.PUT, "/api/v1/news/**").hasRole("ADMIN") // Only admin edits news

                        // POST Access
                        .requestMatchers(HttpMethod.POST, "/members/**").hasAnyRole("TRAINER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/trainers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/news/**").hasRole("ADMIN")

                        // DELETE Access
                        .requestMatchers(HttpMethod.DELETE, "/members/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/trainers/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/news/**").hasRole("ADMIN")

                        // Fallback
                        .requestMatchers("/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                );

        return http.build();
    }


    private AuthenticationSuccessHandler formLoginSuccessHandler(JwtEncoder jwtEncoder) {
        return (request, response, auth) -> {
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
        };
    }

    private AuthenticationSuccessHandler oAuth2SuccessHandler(JwtEncoder jwtEncoder, PasswordEncoder passwordEncoder) {
        return (request, response, authentication) -> {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();

            String email = (String) attributes.get("email");
            String gender = (String) attributes.get("gender");

            boolean isNewUser = !memberRepository.existsByUsername(email);

            if (isNewUser) {
                Member member = new Member();
                member.setUsername(email);
                member.setPassword(passwordEncoder.encode("123456"));
                member.setGender(gender != null ? gender : "UNSPECIFIED");
                member.setJoinDate(LocalDate.now());
                member.setRole("MEMBER"); // Store clean role
                memberRepository.save(member);
            }

            long expirySeconds = 3600;
            var jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
            var claims = JwtClaimsSet.builder()
                    .subject(email)
                    .claim("role", "ROLE_MEMBER")
                    .claim("isNewUser", isNewUser)
                    .expiresAt(Instant.now().plusSeconds(expirySeconds))
                    .build();

            Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims));

            String redirectUrl = "http://localhost:3000/?token=" + jwt.getTokenValue() + "&isNewUser=" + isNewUser;
            response.sendRedirect(redirectUrl);
        };
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