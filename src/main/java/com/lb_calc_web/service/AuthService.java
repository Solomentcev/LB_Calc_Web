package com.lb_calc_web.service;

import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.JwtResponse;
import com.lb_calc_web.dto.LoginRequest;
import com.lb_calc_web.repository.EmployeeRepository;
import com.lb_calc_web.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.Cookie;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final EmployeeService employeeService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(EmployeeService employeeService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.employeeService = employeeService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    public JwtResponse login(LoginRequest loginRequest) throws AuthException {
        logger.info("Login request: {}", loginRequest);
        EmployeeDTO user= null;
        try {
            user = employeeService.loadUserByEmail(loginRequest.getEmail());
        } catch (UsernameNotFoundException e) {
            logger.error("User not found");
            throw new AuthException("User not found");
        }

        String encryptedPassword = passwordEncoder.encode(loginRequest.getPassword());
        logger.debug(loginRequest.getPassword()+" "+encryptedPassword);
        logger.debug(user.getPassword()+" "+user.getEncryptedPassword());

        if (passwordEncoder.matches(loginRequest.getPassword(), user.getEncryptedPassword())) {
            logger.info("Password verified");
            Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getEncryptedPassword(), user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            String access = jwtService.generateAccessToken(user);
            String refresh = jwtService.generateRefreshToken(user);
            return new JwtResponse(access, refresh);

        } else {
            logger.info("Password not verified");
            throw new AuthException("Password not verified");
        }
    }

    public JwtResponse refresh(@NonNull String refreshToken) {
        logger.info("Refreshing token...: {}", refreshToken);
        Claims claims = jwtService.getRefreshClaims(refreshToken);
        if (!jwtService.isRefresh(claims)) {
            logger.info("Invalid token type");
            return new JwtResponse(null, null);
        }
        String email= claims.getSubject();
        EmployeeDTO user = employeeService.loadUserByEmail(email);
        String accessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        logger.info("Refreshing token success: "+newRefreshToken);
        return new JwtResponse(accessToken, newRefreshToken);
    }
    public Cookie generateAccessTokenCookie(String token) {
        return jwtService.generateAccessTokenCookie(token);
    }
    public Cookie generateRefreshTokenCookie(String token) {
        return jwtService.generateRefreshTokenCookie(token);
    }
    public EmployeeDTO getAuthInfo() {
        return (EmployeeDTO) SecurityContextHolder.getContext().getAuthentication();
    }
    public boolean existsByEmail(@NotBlank @Size(max = 50) @Email String email) {
        logger.info("Is exist (%s)...".formatted(email));
        return employeeService.existsByEmail(email);
    }
    public EmployeeDTO registration(EmployeeDTO employeeDTO) {
        logger.info("Регистрация пользователя...");
        return employeeService.save(employeeDTO);
    }
}
