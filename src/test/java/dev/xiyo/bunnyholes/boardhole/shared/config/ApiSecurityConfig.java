package dev.xiyo.bunnyholes.boardhole.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import dev.xiyo.bunnyholes.boardhole.shared.constants.ApiPaths;

@EnableWebSecurity
@EnableMethodSecurity
public class ApiSecurityConfig {
    @Bean
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                ApiPaths.AUTH + ApiPaths.AUTH_SIGNUP,
                                ApiPaths.AUTH + ApiPaths.AUTH_LOGIN,
                                ApiPaths.AUTH + ApiPaths.AUTH_PUBLIC_ACCESS
                        ).permitAll()
                        .requestMatchers(ApiPaths.BOARDS + "/**").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .build();
    }

}
