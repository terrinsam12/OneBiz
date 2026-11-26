package com.clouderp.config;

import com.clouderp.model.User;
import com.clouderp.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            Claims claims = jwtUtil.getClaims(token);
            String email = claims.getSubject();
            Number uid = (Number) claims.get("userId");
            Number cid = (Number) claims.get("companyId");
            String cname = (String) claims.get("companyName");
            String role = (String) claims.get("role");

            if (email != null && uid != null && cid != null) {
                // Verify user still exists and is active in database
                Optional<User> userOpt = userRepository.findById(uid.longValue());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if (!user.isActive()) {
                        // Inactive account: Reject immediately
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"status\":403,\"message\":\"Account has been deactivated\",\"detail\":\"Account has been deactivated. Please contact your company administrator.\"}");
                        return;
                    }
                    role = user.getRole();
                }

                UserPrincipal principal = new UserPrincipal(
                        uid.longValue(),
                        cid.longValue(),
                        cname != null ? cname : "Company",
                        email,
                        "",
                        role != null ? role : "STAFF"
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
