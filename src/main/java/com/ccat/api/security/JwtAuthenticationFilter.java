package com.ccat.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;  // interface, not impl

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {
        final String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            final String token = header.substring(7);
            final String email = jwtService.extractUsername(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails user = userDetailsService.loadUserByUsername(email);
                boolean accountUsable = user.isAccountNonLocked()
                        && user.isEnabled()
                        && user.isAccountNonExpired()
                        && user.isCredentialsNonExpired();

                if (!accountUsable) {
                    // Account is banned or suspended — reject immediately regardless of token
                    SecurityContextHolder.clearContext();
                    writeAccountStatusResponse(response, user);
                    return;
                } else if (jwtService.isTokenValid(token, user)) {
                    var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                // else: account OK but token structurally invalid → proceed unauthenticated (→ 401)
            }
        } catch (Exception ignored) {
            // invalid/expired token — SecurityContext stays empty
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    private void writeAccountStatusResponse(HttpServletResponse response, UserDetails user) throws IOException {
        String message = !user.isEnabled()
                ? "Your account has been banned"
                : "Your account has been suspended";

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("""
                {"status":403,"message":"%s"}
                """.formatted(message));
    }
}
