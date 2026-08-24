package com.lb_calc_web.security.config;

import com.lb_calc_web.security.jwt.JwtAuthentificationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {
    private final JwtAuthentificationFilter jwtAuthentificationFilterAuthFilter;

    public SecurityConfiguration(JwtAuthentificationFilter jwtAuthentificationFilterAuthFilter) {

        this.jwtAuthentificationFilterAuthFilter = jwtAuthentificationFilterAuthFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityCookieFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/login/**", "/registration/**","/error")
                            .permitAll();
                    auth.requestMatchers("/api/v1/login","/api/v1/registration","/api/v1/logout","/api/v1/refresh")
                                    .permitAll();

                    auth.requestMatchers("/employees/**").hasAuthority("ROLE_ADMIN").anyRequest().authenticated();
                })
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .loginProcessingUrl("/login")
//                        .successForwardUrl("/profile")
//                        .permitAll()
//                )
                .exceptionHandling(c ->
                        // основная точка входа
                {
                    c.authenticationEntryPoint(
                                    new LoginUrlAuthenticationEntryPoint("/login"));
                            // точка входа для REST API

                })
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .deleteCookies("jwtAccess")
                        .deleteCookies("jwtRefresh")
                        .logoutSuccessUrl("/login?logout")

                )
                .addFilterBefore(jwtAuthentificationFilterAuthFilter, UsernamePasswordAuthenticationFilter.class);
                http.sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                });
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}

