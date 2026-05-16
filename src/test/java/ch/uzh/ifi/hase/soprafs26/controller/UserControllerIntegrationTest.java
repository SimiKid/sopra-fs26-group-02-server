package ch.uzh.ifi.hase.soprafs26.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import ch.uzh.ifi.hase.soprafs26.interceptor.AuthInterceptor;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises POST /register end-to-end against the real UserService and an
 * in-memory H2 database. Focus: the username validation rules surface as
 * 400 BAD_REQUEST responses carrying the rule-specific message. The message
 * arrives in the ProblemDetail "detail" field, since GlobalExceptionAdvice
 * renders ResponseStatusException as a problem-detail body.
 */
@SpringBootTest
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    // /register is not a protected route, but the interceptor bean is still
    // wired into the context; mocking it keeps the test independent of auth.
    @MockitoBean
    private AuthInterceptor authInterceptor;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void register_usernameWithSpaces_returnsBadRequestWithMessage() throws Exception {
        mockMvc().perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"abc def\",\"password\":\"pw\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Username cannot contain spaces")));
    }

    @Test
    public void register_usernameTooLong_returnsBadRequestWithMessage() throws Exception {
        mockMvc().perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + "a".repeat(21) + "\",\"password\":\"pw\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Username must be at most 20 characters")));
    }

    @Test
    public void register_passwordTooLong_returnsBadRequestWithMessage() throws Exception {
        mockMvc().perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"validuser\",\"password\":\"" + "a".repeat(51) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Password must be at most 50 characters")));
    }
}
