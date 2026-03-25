package com.lb_calc_web.controller;

import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.JwtResponse;
import com.lb_calc_web.dto.LoginRequest;
import com.lb_calc_web.dto.RegistrationDTO;
import com.lb_calc_web.model.user.Role;
import com.lb_calc_web.service.AuthService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class AuthController {
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;

    }
    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("employee", new RegistrationDTO());

        return "registration";
    }
    @PostMapping("/registration")
    public String registration(@Valid RegistrationDTO registrationDTO,
                                BindingResult bindingResult,
                                Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("employee", registrationDTO);
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        try {
            authService.registration(registrationDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Вы успешно зарегистрировались");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Ошибка регистрации: " + e.getMessage());
            return "registration";
        }
    }
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest() );
        return "login";
    }
    @PostMapping("/login")
    public String logIn( @ModelAttribute @Valid LoginRequest loginRequest,
                         HttpServletResponse response,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) throws AuthException {
        if (bindingResult.hasErrors()) {
            return "login";
        }
        try {
            JwtResponse tokens =authService.login(loginRequest);
            response.addCookie(authService.generateAccessTokenCookie(tokens.getAccessToken()));
            response.addCookie(authService.generateRefreshTokenCookie(tokens.getRefreshToken()));
        } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                redirectAttributes.addFlashAttribute("loginRequest", loginRequest);
                return "redirect:/login";
        }
        return "redirect:/profile";
    }

//    @GetMapping("/logout")
//    public String logout(HttpServletRequest request, HttpServletResponse response) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        logger.debug("Logged out user: {}-{}-{}-{}", auth.getName(),auth.getPrincipal(), auth.getCredentials(),auth.isAuthenticated());
//        SecurityContextHolder.clearContext();
//
//        Cookie cookie = new Cookie("jwtAccess", null);
//        cookie.setMaxAge(0);
//       // cookie.setPath("/");
//        response.addCookie(cookie);
//        logger.debug("user isAuth:"+auth.isAuthenticated());
//        return "redirect:/";
//    }

}
