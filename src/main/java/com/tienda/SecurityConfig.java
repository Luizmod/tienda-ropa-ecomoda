package com.tienda;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

/** Acceso por correo, con CSRF habilitado para los formularios. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var solicitudes = new HttpSessionRequestCache();
        var rutas = PathPatternRequestMatcher.withDefaults();
        solicitudes.setRequestMatcher(new OrRequestMatcher(rutas.matcher(HttpMethod.GET, "/carrito/compra"),
                rutas.matcher(HttpMethod.GET, "/carrito/cupon")));
        solicitudes.setMatchingRequestParameterName(null);
        http.authorizeHttpRequests(autorizacion -> autorizacion
                .requestMatchers("/", "/login", "/registro/nuevo", "/registro/guardar",
                        "/css/**", "/js/**", "/img/**", "/fav/**", "/webjars/**", "/error").permitAll()
                .requestMatchers("/catalogo/**").permitAll()
                .requestMatchers("/carrito/compra", "/carrito/cupon", "/carrito/cupon/**",
                        "/consulta/**", "/direccion/**", "/metodopago/**")
                        .hasRole("CLIENTE")
                .requestMatchers("/carrito/**").hasAnyRole("ANONYMOUS", "CLIENTE")
                .requestMatchers(HttpMethod.GET, "/promocion/listado", "/cupon/listado").permitAll()
                .requestMatchers("/categoria/**", "/prenda/**", "/cupon/**", "/promocion/**", "/admin/**")
                        .hasRole("ADMIN")
                .anyRequest().authenticated())
                .requestCache(cache -> cache.requestCache(solicitudes))
                .formLogin(login -> login.loginPage("/login")
                        .usernameParameter("correo").passwordParameter("contrasena")
                        .defaultSuccessUrl("/perfil").failureUrl("/login?error").permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?salida").permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
