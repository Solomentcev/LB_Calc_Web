package com.lb_calc_web.controller.api;

import com.lb_calc_web.dto.JwtResponse;
import com.lb_calc_web.dto.LoginRequest;
import com.lb_calc_web.security.jwt.JwtService;
import com.lb_calc_web.service.AuthService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthRestController {
    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthRestController.class);

    public AuthRestController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest,
                         HttpServletResponse response) throws AuthException {
        JwtResponse tokens = authService.login(loginRequest);
        return new ResponseEntity<>(tokens, HttpStatus.OK);
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Refresh-Token") String header,
                          HttpServletRequest request) {
        JwtResponse tokens=authService.refresh(header);
        return new ResponseEntity<>(tokens, HttpStatus.OK);
    }

}
