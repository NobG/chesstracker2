package com.nobg.chesstracker2.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/favicon.svg", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("text/html;charset=UTF-8");
                            response.getWriter().write("""
                                    <!doctype html>
                                    <html lang="de">
                                    <head>
                                        <meta charset="utf-8">
                                        <meta name="viewport" content="width=device-width, initial-scale=1">
                                        <title>Sitzung abgelaufen - chesstracker2</title>
                                        <link rel="icon" type="image/svg+xml" href="/favicon.svg">
                                        <link rel="stylesheet" href="/css/app.css">
                                    </head>
                                    <body>
                                    <main class="shell error-page">
                                        <section class="error-panel">
                                            <p class="eyebrow">Zug blockiert</p>
                                            <h1>Deine Sitzung ist abgelaufen oder die Aktion wurde blockiert.</h1>
                                            <p>Bitte melde dich neu an und versuche es erneut. Bereits eingegebene Trainingsdaten werden auf der heutigen Trainingseite nach Moeglichkeit aus dem Browser-Draft wiederhergestellt.</p>
                                            <div class="button-row">
                                                <a class="primary-button" href="/login">Zur Anmeldung</a>
                                                <a class="secondary-button" href="/today">Zur heutigen Trainingseite</a>
                                            </div>
                                        </section>
                                    </main>
                                    </body>
                                    </html>
                                    """);
                        })
                )
                .build();
    }
}
