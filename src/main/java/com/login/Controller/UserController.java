package com.login.Controller;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.login.JWT.JWTService;
import com.login.Models.User;
import com.login.Service.UserService;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    private final UserService userService;
    private final JWTService jwtService;

    @Autowired
    public UserController(UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signUpUser(@RequestBody User user) {
        try {
            boolean isUserCreated = userService.addUser(user);
            if (isUserCreated) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of("message", "User registered successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "User already exists."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody User user, HttpServletResponse response) {
        try {
            String val = userService.loginWithCredentials(user);
            

            if (val != null) {
                Cookie cookie = new Cookie("jwtToken", val);
                cookie.setHttpOnly(true);
                cookie.setMaxAge(1000 * 60 * 60);
                cookie.setPath("/");
                response.addCookie(cookie);
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("jwtToken", val));
            }
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("msg", "User Doesnt exists!"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("msg", "Internal Server error..."));

    }

    @GetMapping("/auth")
    public ResponseEntity<?> authenticationCheck(HttpServletRequest request) {
        System.out.println("Object: "+jwtService);
        String token = jwtService.getTokenFromCookie(request);
        System.out.println("Token from cookie: " + token);

        try {
            if (token != null) {
                String user = jwtService.extractUsername(token);
                System.out.println(user);
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("user", user));
             }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Token"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No Token Found"));
     }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("jwtToken", null);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("msg", "User LoggedOut Successfully!!!"));
    }

}
