package com.quiz.security;

import com.quiz.model.User;
import com.quiz.repository.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            logger.info("Extracted JWT Token: " + jwt);

            if (StringUtils.hasText(jwt) && validateToken(jwt)) {
                logger.info("JWT Token is valid");
                Jws<Claims> claims = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                        .build().parseClaimsJws(jwt);
                String username = getUsernameFromJWT(claims);
                logger.info("Extracted username: " + username);

                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                logger.info("User found in database: " + user.getUsername());

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        new org.springframework.security.core.userdetails.User(
                            user.getUsername(), 
                            "", 
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        ), 
                        null, 
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }

    private boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            logger.warn("Exception while validating JWT token", ex);
            return false;
        }
    }

    private String getUsernameFromJWT(Jws<Claims> claims) {
        return claims.getBody().getSubject();
    }
} 