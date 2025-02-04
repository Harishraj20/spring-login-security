package com.login.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.login.JWT.JWTService;
import com.login.Models.User;
import com.login.Repository.UserRepository;

@Service
public class UserService {

    private final UserRepository repository;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTService jWTService;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public boolean addUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return repository.saveUser(user);
    }

    public String loginWithCredentials(User user) {

        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

            if (authentication.isAuthenticated()) {
                String token = jWTService.generateToken(user.getEmail());
                return token;
            }
        }

        catch (BadCredentialsException e) {
            System.out.println("Bad credential exception!....");
            throw new BadCredentialsException("Invalid email or password", e);
        } catch (Exception e) {
            System.out.println("General Exception occured!" + e.getMessage());
            return null;
        }
        return null;

    }

}
