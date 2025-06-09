
package com.accesshr.emsbackend.EmployeeController.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Disable CSRF protection for stateless JWT authentication
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers("/api/v1/employeeManager/register/{company}", "/api/tenants/{schemaName}",
                                "/api/clientDetails", "/api/v1/employeeManager/login", "/api/v1/employeeManager/add",
                                "/api/v1/employeeManager/change-password", "/api/clientDetails/{id}",
                                "/api/v1/employeeManager/reset-password/{employeeId}/{newPassword}",
                                "/api/payment/create-checkout-session/{price}", "/api/payment/verify")
                        .permitAll()
                        .anyRequest().authenticated() // Require authentication for all other requests
                )
                .httpBasic(Customizer.withDefaults()) // Enable basic HTTP authentication for simplicity
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Set
                                                                                                              // session
                                                                                                              // to
                                                                                                              // stateless
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // .requestMatchers("/api/v1/employeeManager/employees",
    // "api/v1/employeeManager/employees/{employeeId}")
    // .hasRole("employee")
    // .requestMatchers("/api/v1/employeeManager/employees",
    // "api/v1/employeeManager/employees/{employeeId}")
    // .hasRole("admin")

}
