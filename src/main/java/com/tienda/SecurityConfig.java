package com.tienda;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Acceso por correo, con CSRF habilitado para los formularios. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(autorizacion -> autorizacion
                .requestMatchers("/", "/login", "/registro/nuevo", "/registro/guardar",
                        "/css/**", "/js/**", "/img/**", "/fav/**", "/webjars/**", "/error").permitAll()
                .requestMatchers("/catalogo/**").permitAll()
                .requestMatchers("/carrito/**").hasRole("CLIENTE")
                .requestMatchers("/categoria/**", "/prenda/**", "/cupon/**", "/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
                .formLogin(login -> login.loginPage("/login")
                        .usernameParameter("correo").passwordParameter("contrasena")
                        .defaultSuccessUrl("/perfil", true).failureUrl("/login?error").permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?salida").permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
