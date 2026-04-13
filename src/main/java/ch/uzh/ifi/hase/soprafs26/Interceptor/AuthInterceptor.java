package ch.uzh.ifi.hase.soprafs26.Interceptor;

import ch.uzh.ifi.hase.soprafs26.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor{
    private final AuthenticationService authenticationService;

    public AuthInterceptor(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object Handler){
        String token = request.getHeader("Authorization");
        authenticationService.authenticateByToken(token);
        return true;
    }
}
