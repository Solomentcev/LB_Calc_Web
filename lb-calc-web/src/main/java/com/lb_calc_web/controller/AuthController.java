package com.lb_calc_web.controller;

import com.lb_calc_web.dto.JwtResponse;
import com.lb_calc_web.dto.LoginRequest;
import com.lb_calc_web.dto.RegistrationDTO;
import com.lb_calc_web.service.AuthService;
import com.lb_calc_web.security.jwt.AuthTokens;
import com.lb_calc_web.security.jwt.JwtService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** MVC-контроллер регистрации и входа пользователей через веб-интерфейс. */
@Controller
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }
    /** Отображает форму регистрации пользователя. */
    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("employee", new RegistrationDTO());

        return "registration";
    }
    /** Отображает форму регистрации пользователя. */
    @PostMapping("/registration")
    public String registration(@Valid RegistrationDTO registrationDTO,
                                BindingResult bindingResult,
                                Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("employee", registrationDTO);
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        try {
            authService.registration(
                    registrationDTO.getFirstName(),
                    registrationDTO.getLastName(),
                    registrationDTO.getEmail(),
                    registrationDTO.getPassword(),
                    registrationDTO.getConfirmPassword()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Вы успешно зарегистрировались");
            return "redirect:/login";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", "Ошибка регистрации: " + exception.getMessage());
            return "registration";
        }
    }
    /** Отображает страницу входа в систему. */
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest() );
        return "login";
    }
    /** Проверяет учётные данные и создаёт cookie с JWT-токенами при успешном входе. */
    @PostMapping("/login")
    public String logIn( @ModelAttribute @Valid LoginRequest loginRequest,
                         HttpServletResponse response,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "login";
        }
        try {
            AuthTokens tokens = authService.login(
                    loginRequest.getEmail(), loginRequest.getPassword());
            response.addCookie(jwtService.generateAccessTokenCookie(tokens.accessToken()));
            response.addCookie(jwtService.generateRefreshTokenCookie(tokens.refreshToken()));
        } catch (AuthException exception) {
                redirectAttributes.addFlashAttribute("error", "Неверный email или пароль");
                redirectAttributes.addFlashAttribute("loginRequest", loginRequest);
                return "redirect:/login";
        }
        return "redirect:/";
    }

}
