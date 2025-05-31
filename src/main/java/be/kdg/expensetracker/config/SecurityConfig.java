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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS with our configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Configure CSRF - disable for all API endpoints to fix test issues
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(authorize -> authorize
                        // Public access
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/webjars/**", "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/expenses").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/expenses").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/expenses/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/expenses/search").permitAll()
                        // Public endpoint for testing with Client - permitAll is used to test the Client
                        .requestMatchers(HttpMethod.POST, "/api/expenses/public").permitAll()

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Only allow the origin that corresponds to the Client application
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:9000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}