package com.login.Controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.login.JWT.JWTService;
import com.login.Models.User;
import com.login.Service.UserService;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private User user;
    private String userJson;

    private String jwtToken;

    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception {
        jwtToken = "eyToken";

        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("123");
        user.setName("example");

        ObjectMapper objectMapper = new ObjectMapper();
        userJson = objectMapper.writeValueAsString(user);
    }

    @Test
    public void testSignUpMethod_success() throws Exception {
        when(userService.addUser(Mockito.any(User.class))).thenReturn(true);

        System.out.println("Mock return value: " + userService.addUser(user));

        mockMvc.perform(post("/signup")
                .contentType("application/json")
                .content(userJson))
                .andExpect(status().isCreated());
    }

    @Test
    public void testSignUpMethod_failure() throws Exception {
        when(userService.addUser(Mockito.any(User.class))).thenReturn(false);

        System.out.println("Mock return value: " + userService.addUser(user));

        mockMvc.perform(post("/signup")
                .contentType("application/json")
                .content(userJson))
                .andExpect(status().isConflict());
    }

    @Test
    public void testSignUpMethod_Exception() throws Exception {
        when(userService.addUser(Mockito.any(User.class))).thenThrow(new RuntimeException());

        mockMvc.perform(post("/signup")
                .contentType("application/json")
                .content(userJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testLoginMethod_Success() throws Exception {

        when(userService.loginWithCredentials(Mockito.any(User.class))).thenReturn(jwtToken);

        mockMvc.perform(post("/login")
                .contentType("application/json")
                .content(userJson))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.jwtToken").value(jwtToken))
                .andExpect(cookie().exists("jwtToken")).andExpect(cookie().httpOnly("jwtToken", true))
                .andExpect(cookie().path("jwtToken", "/"))
                .andExpect(cookie().maxAge("jwtToken", 3600000));
    }

    @Test
    public void testLoginMethod_BadCredentialException() throws Exception {
        when(userService.loginWithCredentials(Mockito.any(User.class)))
                .thenThrow(new BadCredentialsException("Exception"));

        mockMvc.perform(post("/login")
                .contentType("application/json")
                .content(userJson))
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void testLoginMethod_null() throws Exception {
        when(userService.loginWithCredentials(Mockito.any(User.class))).thenReturn(null);

        mockMvc.perform(post("/login")
                .contentType("application/json")
                .content(userJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.msg")
                        .value("Internal Server error..."));
    }

    @Test
    public void testLogoutMethod() throws Exception {
        mockMvc.perform(get("/logout")).andExpect(cookie().exists("jwtToken"))
                .andExpect(cookie().maxAge("jwtToken", 0));
    }

    @Test
    public void testAuthenticationCheck_Success() throws Exception {
        Cookie cookie = new Cookie("jwtToken", jwtToken);

        when(jwtService.extractUsername(jwtToken)).thenReturn("testUser");
        when(jwtService.getTokenFromCookie(Mockito.any(HttpServletRequest.class))).thenReturn(jwtToken);

        mockMvc.perform(get("/auth").cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    public void testAuthenticationCheck_null() throws Exception {
        when(jwtService.getTokenFromCookie(Mockito.any(HttpServletRequest.class))).thenReturn(null);

        mockMvc.perform(get("/auth"))
                .andDo(result -> System.out.println("Response Status: " + result.getResponse().getStatus()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAuthenticationCheck_Exception() throws Exception {
       

        when(jwtService.getTokenFromCookie(any(HttpServletRequest.class))).thenReturn(jwtToken);
        when(jwtService.extractUsername(jwtToken)).thenThrow(new RuntimeException());

        mockMvc.perform(get("/auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid Token"));
    }

}
