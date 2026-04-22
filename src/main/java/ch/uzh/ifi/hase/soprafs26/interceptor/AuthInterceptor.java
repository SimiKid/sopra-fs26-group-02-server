package ch.uzh.ifi.hase.soprafs26.interceptor;

import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Validates the Authorization token on every request matched in
 * WebMvcConfig (protected routes like /games/**, /attacks/**, /logout).
 * Throws 401 via AuthenticationService when the token is missing or
 * unknown; CORS preflight (OPTIONS) is allowed through without auth.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor{
    private final AuthenticationService authenticationService;

    public AuthInterceptor(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object Handler){
        // CORS preflight carries no Authorization header -> let it through
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        authenticationService.authenticateByToken(token);
        return true;
    }
}
