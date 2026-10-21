package io.hexlet.spring.config;

import io.hexlet.spring.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private JwtDecoder jwtDecoder;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private CustomUserDetailsService userService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. Отключаем CSRF, так как API работает без сессий (Stateless)
                .csrf(csrf -> csrf.disable())

                // 2. Настраиваем правила доступа
                .authorizeHttpRequests(auth -> auth
                        // Важно: разрешаем системный путь обработки ошибок
                        .requestMatchers("/error", "/error/**").permitAll()

                        // Статический фронтенд и ресурсы
                        .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/img/**").permitAll()

                        // Публичные эндпоинты API (включая все вложенные пути страниц)
                        .requestMatchers("/api/login", "/api/pages", "/api/pages/**").permitAll()

                        .requestMatchers("/api/users", "/api/users/**").permitAll()

                        // Все остальные запросы строго по JWT-токену
                        .anyRequest().authenticated()
                )

                // 3. Отключаем хранение сессий на сервере
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Проверка JWT-токена в заголовке Authorization: Bearer <токен>
                .oauth2ResourceServer(rs ->
                        rs.jwt(jwt -> jwt.decoder(jwtDecoder))
                )

                // Заголовка .httpBasic() здесь быть НЕ ДОЛЖНО
                .build();
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

    @Bean
    public AuthenticationProvider daoAuthProvider(AuthenticationManagerBuilder auth) {
        var provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
