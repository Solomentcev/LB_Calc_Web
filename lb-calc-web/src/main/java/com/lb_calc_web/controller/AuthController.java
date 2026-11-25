package com.lb_calc_web.controller;

import com.lb_calc_web.dto.JwtResponse;
import com.lb_calc_web.dto.LoginRequest;
import com.lb_calc_web.dto.RegistrationDTO;
import com.lb_calc_web.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
/** MVC-контроллер регистрации и входа пользователей через веб-интерфейс. */
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;

    }
    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("employee", new RegistrationDTO());

        return "registration";
    }
    @PostMapping("/registration")
    /** Отображает форму регистрации пользователя. */
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
    /** Отображает страницу входа в систему. */
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest() );
        return "login";
    }
    @PostMapping("/login")
    public String logIn( @ModelAttribute @Valid LoginRequest loginRequest,
                         HttpServletResponse response,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
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
        return "redirect:/";
    }

}
