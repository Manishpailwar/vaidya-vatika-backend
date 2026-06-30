package com.vaidyavatika.security;

import com.vaidyavatika.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtil.isValid(token)) {
                String subject = jwtUtil.extractSubject(token);
                boolean isAdmin = jwtUtil.isAdminToken(token);

                var authorities = isAdmin
                        ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        : List.of(new SimpleGrantedAuthority("ROLE_USER"));

                var auth = new UsernamePasswordAuthenticationToken(subject, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

                // FIXED: Store the caller's email as a request attribute so
                // controllers (OrderController, UserController) can verify
                // ownership without re-parsing the token themselves.
                if (!isAdmin) {
                    request.setAttribute("callerEmail", subject);
                    // Store name for review/profile endpoints
                    userRepository.findByEmail(subject)
                            .ifPresent(user -> request.setAttribute("callerName", user.getName()));
                }
            }
        }

        chain.doFilter(request, response);
    }
}