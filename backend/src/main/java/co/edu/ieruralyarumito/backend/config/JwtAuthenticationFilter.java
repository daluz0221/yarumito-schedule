package co.edu.ieruralyarumito.backend.config;

import co.edu.ieruralyarumito.backend.entity.Usuario;
import co.edu.ieruralyarumito.backend.repository.UsuarioRepository;
import co.edu.ieruralyarumito.backend.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// Lee el Bearer token y deja al usuario autenticado en el contexto de Spring.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();

        try {
            Claims claims = jwtService.leerToken(token);
            String correo = claims.getSubject();

            Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);

            if (usuario != null && usuario.isActivo()) {
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        usuario.getCorreo(),
                        null,
                        List.of(new SimpleGrantedAuthority(
                                "ROLE_" + usuario.getRol().name()))
                );
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            }
        } catch (JwtException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"mensaje\":\"Token inválido o expirado\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
