package com.login.Service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import com.login.JWT.JWTService;
import com.login.Models.User;
import com.login.Repository.UserRepository;

public class UserServiceTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private String jwtToken;

    private User user;

    @SuppressWarnings("deprecation")
    @Before
    public void setUp() {

        jwtToken = "eyToken";
        MockitoAnnotations.initMocks(this);
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("example@1");
        user.setName("testUser");

        when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

    }

    @Test
    public void testAddUser() throws Exception {
        String encodedPassword = "encoded";
        when(passwordEncoder.encode(user.getPassword())).thenReturn(encodedPassword);
        when(userRepository.saveUser(Mockito.any(User.class))).thenReturn(true);

        boolean result = userService.addUser(user);
        assertTrue(result);
    }

    @Test
    public void loginWithCredentials_success() throws Exception {
        when(authentication.isAuthenticated()).thenReturn(true);
        System.out.println("Authentication status: " + authentication.isAuthenticated());
        when(jwtService.generateToken(user.getEmail())).thenReturn(jwtToken);

        System.out.println(jwtService.generateToken(user.getEmail()));

        String token = userService.loginWithCredentials(user);
        System.out.println(token);

        assertNotNull(token);
    }

    @Test
    public void loginWithCredentials_NotAuthenticated() throws Exception {
        when(authentication.isAuthenticated()).thenReturn(false);
        when(jwtService.generateToken(user.getEmail())).thenReturn(jwtToken);

        System.out.println(jwtService.generateToken(user.getEmail()));

        String token = userService.loginWithCredentials(user);
        System.out.println(token);

        assertNull(token);
    }

    @Test
    public void loginWithCredentials_BadCredentialsException() throws Exception {

        when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad Credentials"));
        BadCredentialsException thrown = assertThrows(
                BadCredentialsException.class,
                () -> userService.loginWithCredentials(user));
        assertTrue(thrown.getMessage().contains("Invalid email or password"));
    }

    @Test
    public void loginWithCredentials_GeneralException() {
        when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        String result = userService.loginWithCredentials(user);

        assertNull(result);
    }

}
