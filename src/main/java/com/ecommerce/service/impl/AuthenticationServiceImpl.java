package com.ecommerce.service.impl;

import com.ecommerce.entity.User;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.requests.AuthenticateRequest;
import com.ecommerce.requests.RegistrationRequest;
import com.ecommerce.security.filters.jwt.JWTService;
import com.ecommerce.service.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final AuthenticationManager manager;

    @Override
    public String registerUser(RegistrationRequest req) {
        req.setPassword(encoder.encode(req.getPassword()));
        User user = UserMapper.registerUserMapper(req);
        User pUser = userRepository.save(user);
        String token = jwtService.generateToken(pUser);
        return "User Registered Successfully - \n token = " + token;
    }

    @Override
    public String authenticateUser(AuthenticateRequest req) {
        Authentication authenticateObj = manager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        if(authenticateObj.isAuthenticated()){
            return jwtService.generateToken(userRepository.findByEmail(req.getEmail()).get());
        }
        return "Invalid credentials";
    }


}