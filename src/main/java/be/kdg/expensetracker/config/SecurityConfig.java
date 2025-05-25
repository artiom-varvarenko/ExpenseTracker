package be.kdg.expensetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // Enable CSRF with exception for API endpoints
            .authorizeHttpRequests(authorize -> authorize
                // Public access
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/webjars/**", "/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/expenses").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/expenses").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/expenses/**").permitAll()
                
                // Role-specific access
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/reports/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/categories").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/expenses").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/expenses/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/expenses/**").authenticated()
                
                // Fallback
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            // Allow frames for h2-console
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
            
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 