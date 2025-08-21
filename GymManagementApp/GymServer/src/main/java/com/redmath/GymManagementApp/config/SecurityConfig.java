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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String MEMBER = "MEMBER";
    private static final String TRAINER = "TRAINER";
    private static final String ADMIN = "ADMIN";

    private final MemberRepository memberRepository;

    @Value("${jwt.expires-seconds}")
    private long expirySeconds;
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;
    @Value("${app.oauth.default-password}")
    private String defaultPassword;



    public SecurityConfig(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtEncoder jwtEncoder, PasswordEncoder passwordEncoder) throws Exception {
        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/h2-console/**")
        );

        http
                .formLogin(cfg -> cfg.successHandler(formLoginSuccessHandler(jwtEncoder)))
                .oauth2Login(cfg -> cfg.successHandler(oAuth2SuccessHandler(jwtEncoder, passwordEncoder)))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .oauth2ResourceServer(cfg -> cfg.jwt(j -> j.jwtAuthenticationConverter(jwt -> {
                    String role = jwt.getClaimAsString("role");
                    if (role == null || role.isBlank()) throw new IllegalArgumentException("Role missing in JWT");
                    return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(role)));
                })))
                .sessionManagement(cfg -> cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/swagger-ui/**", "/v3/api-docs/**", "/login", "/actuator/**", "/oauth2/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/trainers").hasAnyRole(MEMBER, TRAINER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/members/me").hasAnyRole(MEMBER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/trainers/{id}").hasAnyRole(TRAINER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/members").hasAnyRole(TRAINER, ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/members/by-trainer/**").hasAnyRole(TRAINER, ADMIN)

                        .requestMatchers(HttpMethod.PUT, "/api/members/me").hasAnyRole(MEMBER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/members/**").hasAnyRole(TRAINER, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/trainers/**").hasRole(ADMIN)

                        .requestMatchers(HttpMethod.POST, "/api/members/**").hasAnyRole(TRAINER, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/trainers/**").hasRole(ADMIN)

                        .requestMatchers(HttpMethod.DELETE, "/api/members/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/trainers/**").hasRole(ADMIN)

                        .anyRequest().hasRole(ADMIN)
                );
        return http.build();
    }

    private String issueJwt(JwtEncoder encoder, String subject, String prefixedRole, Map<String, Object> extraClaims) {
        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        var builder = JwtClaimsSet.builder()
                .subject(subject)
                .claim("role", prefixedRole)
                .expiresAt(Instant.now().plusSeconds(expirySeconds));
        if (extraClaims != null && !extraClaims.isEmpty()) extraClaims.forEach(builder::claim);
        return encoder.encode(JwtEncoderParameters.from(header, builder.build())).getTokenValue();
    }

    private AuthenticationSuccessHandler formLoginSuccessHandler(JwtEncoder jwtEncoder) {
        return (request, response, auth) -> {
            String role = auth.getAuthorities().stream().findFirst().orElseThrow().getAuthority();
            String token = issueJwt(jwtEncoder, auth.getName(), role, Map.of());
            String body = "{\"token_type\":\"Bearer\",\"access_token\":\"" + token + "\",\"expires_in\":" + expirySeconds + "}";
            response.setContentType("application/json");
            response.getWriter().print(body);
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
                member.setPassword(passwordEncoder.encode(defaultPassword));
                member.setGender(gender != null ? gender : "UNSPECIFIED");
                member.setJoinDate(LocalDate.now());
                member.setRole(MEMBER);
                memberRepository.save(member);
            }

            String token = issueJwt(jwtEncoder, email, "ROLE_" + MEMBER, Map.of("isNewUser", isNewUser));
            String redirectUrl = frontendBaseUrl + "/?token=" + token + "&isNewUser=" + isNewUser;
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
