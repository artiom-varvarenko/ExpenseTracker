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
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
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
                // Different handling for API vs MVC endpoints
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        )
                        // This will redirect to login for non-API endpoints
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new NegatedRequestMatcher(new AntPathRequestMatcher("/api/**"))
                        )
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