//package com.ma.dlp.config;
//
//import java.util.Arrays;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import com.ma.dlp.service.CustomUserDetailsService;
//
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(userDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder);
//        return authProvider;
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
//        http
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .csrf(csrf -> csrf.disable()) // Disable CSRF for APIs
//
//                .authenticationProvider(authenticationProvider)
//                .authorizeHttpRequests(authz -> authz
//
//                                .requestMatchers("/ws", "/ws/**").permitAll()
//                                .requestMatchers("/topic/**", "/app/**").permitAll()
//
//                                // Public endpoints
//
//                                // 🔥 ADD PHISHING ENDPOINTS HERE 🔥
//                                .requestMatchers("/api/phishing/**").permitAll() // Add this line
//                                .requestMatchers("/api/auth/**").permitAll()
//                                .requestMatchers("/api/agent/register").permitAll()
//                                .requestMatchers("/api/agent/login").permitAll()
//                                .requestMatchers("/h2-console/**").permitAll()
//                                .requestMatchers("/api/agent/capabilities", "POST").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/agent/heartbeat").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/agent/file-policies/**").permitAll()
//                                .requestMatchers("/api/agent/active-policies", "GET").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/agent/file-events").permitAll()
//                                .requestMatchers("/api/agent/alerts", "POST").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/agent/ocr-violations").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/agent/security-certificate").permitAll()
//                                .requestMatchers("/api/agent/commands/**").permitAll()
//                                .requestMatchers("/api/admin/security-certificates").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/agent/file-browse-response").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/agent/ocr-register").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/agent/*/security-monitor/realtime").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/agent/*/security-monitor/status").permitAll()
//                                .requestMatchers("/api/admin/ocr/summary").permitAll()
//
//                                // Python GET endpoints
//                                .requestMatchers("/api/python-client/**").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/python/devices").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/python/device/**").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/python/stats").permitAll()
//
//                                // OCR ENDPOINTS:
//                                .requestMatchers(HttpMethod.POST, "/api/agent/ocr/status").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/agent/ocr/live").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/agent/ocr/violation").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/agent/ocr/certificate").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/agent/ocr/certificate/*/latest").permitAll()
//                                .requestMatchers("/api/admin/agents/*/ocr-status").permitAll()
//                                .requestMatchers("/api/admin/ocr-live-data").permitAll()
//                                .requestMatchers("/api/admin/ocr-violations").permitAll()
//                                .requestMatchers("/api/admin/ocr-statistics").permitAll()
//                                .requestMatchers("/api/admin/agents/*/ocr-toggle").permitAll()
//                                .requestMatchers("/api/admin/agents/*/ocr-violations").permitAll()
//
//                                // Static resources (HTML, CSS, JS)
//                                .requestMatchers("/*.html", "/*.css", "/*.js", "/favicon.ico", "/").permitAll()
//
//                                // Admin endpoints - require ADMIN role
//                                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
//                                .anyRequest().authenticated()
//                )
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                )
//                .headers(headers -> headers
//                        .frameOptions(frame -> frame.sameOrigin()) // For H2 console
//                );
//
//        return http.build();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // 🔥 CRITICAL: Add Chrome extension origins here 🔥
//        configuration.setAllowedOriginPatterns(Arrays.asList(
//                "http://localhost:8080",
//                "https://elisha-nongeological-anaya.ngrok-free.dev",
//                "chrome-extension://*", // Allow all Chrome extensions
//                "chrome-extension://eiefebcijckhphnonpbdbahjcmdkjfja" // Your specific extension
//        ));
//
//        // OR use setAllowedOrigins for exact origins:
//        // configuration.setAllowedOrigins(Arrays.asList(
//        //     "http://localhost:8080",
//        //     "https://elisha-nongeological-anaya.ngrok-free.dev",
//        //     "chrome-extension://eiefebcijckhphnonpbdbahjcmdkjfja"
//        // ));
//
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type")); // Add exposed headers
//        configuration.setAllowCredentials(true); // Keep true if you need cookies/credentials
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}



// package com.ma.dlp.config;

// import java.util.Arrays;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.HttpMethod;
// import org.springframework.security.authentication.AuthenticationProvider;
// import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.CorsConfigurationSource;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// import com.ma.dlp.service.CustomUserDetailsService;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     @Bean
//     public AuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
//         DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//         authProvider.setUserDetailsService(userDetailsService);
//         authProvider.setPasswordEncoder(passwordEncoder);
//         return authProvider;
//     }

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
//         http
//                 .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                 .csrf(csrf -> csrf.disable())
//                 .authenticationProvider(authenticationProvider)

//                 .authorizeHttpRequests(authz -> authz
//                         // ============= PUBLIC WEBSOCKET =============
//                         .requestMatchers("/ws", "/ws/**").permitAll()
//                         .requestMatchers("/topic/**", "/app/**").permitAll()

//                         // ============= PUBLIC STATIC PAGES =============
//                         .requestMatchers("/index.html", "/", "/favicon.ico").permitAll()
//                         .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // Static resources

//                         // ============= PUBLIC API ENDPOINTS =============
//                         // Auth endpoints
//                         .requestMatchers("/api/auth/**").permitAll()

//                         // Phishing endpoints
//                         .requestMatchers("/api/phishing/**").permitAll()

//                         // Agent registration/login endpoints
//                         .requestMatchers("/api/agent/register").permitAll()
//                         .requestMatchers("/api/agent/login").permitAll()
//                         .requestMatchers("/h2-console/**").permitAll()

//                         // Agent operational endpoints
//                         .requestMatchers(HttpMethod.POST, "/api/agent/capabilities").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/agent/heartbeat").permitAll()
//                         .requestMatchers(HttpMethod.GET, "/api/agent/file-policies/**").permitAll()
//                         .requestMatchers(HttpMethod.GET, "/api/agent/active-policies").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/agent/file-events").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/agent/alerts").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/agent/ocr-violations").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/agent/security-certificate").permitAll()
//                         .requestMatchers("/api/agent/commands/**").permitAll()
//                         .requestMatchers("/api/admin/security-certificates").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/agent/file-browse-response").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/agent/ocr-register").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/agent/*/security-monitor/realtime").permitAll()
//                         .requestMatchers(HttpMethod.GET, "/api/agent/*/security-monitor/status").permitAll()

//                         // Python client endpoints
//                         .requestMatchers("/api/python-client/**").permitAll()
//                         .requestMatchers(HttpMethod.GET, "/api/python/devices").permitAll()
//                         .requestMatchers(HttpMethod.GET, "/api/python/device/**").permitAll()
//                         .requestMatchers(HttpMethod.GET, "/api/python/stats").permitAll()

//                         // OCR endpoints
//                         .requestMatchers(HttpMethod.POST, "/api/agent/ocr/status").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/agent/ocr/live").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/agent/ocr/violation").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/agent/ocr/certificate").permitAll()
//                         .requestMatchers(HttpMethod.POST, "/api/agent/ocr/certificate/*/latest").permitAll()

//                         // ============= ADMIN PAGES - REQUIRES AUTHENTICATION =============
//                         // These are the Thymeleaf pages that need authentication
//                         .requestMatchers("/dashboard.html",
//                                 "/manage-agents.html",
//                                 "/agent-add.html",
//                                 "/agent-edit.html",
//                                 "/agent-view.html",
//                                 "/alerts.html",
//                                 "/alerts-details.html",
//                                 "/assign-policy.html",
//                                 "/audit-logs.html",
//                                 "/data-retention.html",
//                                 "/device-logs.html",
//                                 "/file-policies.html",
//                                 "/general.html",
//                                 "/generate-report.html",
//                                 "/integrations.html",
//                                 "/ocr-dashboard.html",
//                                 "/ocr-policies.html",
//                                 "/permissions.html",
//                                 "/policy-create.html",
//                                 "/policy-edit.html",
//                                 "/policy-view.html",
//                                 "/reports.html",
//                                 "/roles.html",
//                                 "/security.html",
//                                 "/users.html").authenticated()

//                         // ============= ADMIN API - REQUIRES ADMIN ROLE =============
//                         .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

//                         // ============= ANY OTHER REQUEST =============
//                         .anyRequest().authenticated()
//                 )

//                 .sessionManagement(session -> session
//                         .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                         .maximumSessions(1)
//                         .expiredUrl("/index.html")
//                 )

//                 .headers(headers -> headers
//                         .frameOptions(frame -> frame.sameOrigin()) // For H2 console
//                 );

//         return http.build();
//     }

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration configuration = new CorsConfiguration();

//         configuration.setAllowedOriginPatterns(Arrays.asList(
//                 "http://localhost:8080",
//                 "https://elisha-nongeological-anaya.ngrok-free.dev",
//                 "chrome-extension://*",
//                 "chrome-extension://eiefebcijckhphnonpbdbahjcmdkjfja"
//         ));

//         configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
//         configuration.setAllowedHeaders(Arrays.asList("*"));
//         configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
//         configuration.setAllowCredentials(true);

//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", configuration);
//         return source;
//     }
// }




package com.ma.dlp.config;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ma.dlp.service.CustomUserDetailsService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/dashboard.html");
        // handler.setDefaultTargetUrl("/admin-dashboard.html");
        handler.setAlwaysUseDefaultTargetUrl(true);
        return handler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/**")
                )
                .authenticationProvider(authenticationProvider)

                .authorizeHttpRequests(authz -> authz
                        // ============= PUBLIC WEBSOCKET =============
                        .requestMatchers("/ws", "/ws/**").permitAll()
                        .requestMatchers("/topic/**", "/app/**").permitAll()

                        // ============= PUBLIC STATIC PAGES =============
                        .requestMatchers("/index.html", "/", "/favicon.ico").permitAll()
                        .requestMatchers("/*.html").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // ============= PUBLIC API ENDPOINTS =============
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/phishing/**").permitAll()
                        .requestMatchers("/api/agent/**").permitAll()
                        .requestMatchers("/api/python-client/**").permitAll()
                        .requestMatchers("/api/python/**").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/python/devices").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/python/device/**").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/python/stats").permitAll()
                        // ============= ADMIN PAGES - REQUIRES AUTHENTICATION =============
                        .requestMatchers(
                                "/dashboard.html",
                                "/manage-agents.html",
                                "/agent-add.html",
                                "/agent-edit.html",
                                "/agent-view.html",
                                "/alerts.html",
                                "/alert-details.html",
                                "/assign-policy.html",
                                "/audit-logs.html",
                                "/data-retention.html",
                                "/device-logs.html",
                                "/file-policies.html",
                                "/general.html",
                                "/generate-report.html",
                                "/integrations.html",
                                "/ocr-dashboard.html",
                                "/ocr-policies.html",
                                "/permissions.html",
                                "/policy-create.html",
                                "/policy-edit.html",
                                "/policy-view.html",
                                "/reports.html",
                                "/roles.html",
                                "/security.html",
                                "/users.html"
                        ).authenticated()

                        // ============= ADMIN API - REQUIRES ADMIN ROLE =============
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        // ============= ANY OTHER REQUEST =============
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/index.html")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/index.html?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/index.html?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .expiredUrl("/index.html?expired=true")
                )

                // THIS IS THE KEY PART - Redirect to login page instead of 403
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/index.html"))
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/index.html");
                        })
                )

                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:8080",
                "http://localhost:9090",
                "http://localhost:9090/python_client.html",
                "https://elisha-nongeological-anaya.ngrok-free.dev",
                "chrome-extension://*",
                "chrome-extension://eiefebcijckhphnonpbdbahjcmdkjfja",
                "https://isbn-darwin-observations-button.trycloudflare.com/"
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}