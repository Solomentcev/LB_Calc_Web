package com.lb_calc_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.RestController;


@SpringBootApplication
/** Точка входа Spring Boot-приложения расчёта и проектирования автоматизированных камер хранения. */
public class LbCalcWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(LbCalcWebApplication.class, args);
    }


}
