package com.lb_calc_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



/** Точка входа Spring Boot-приложения расчёта и проектирования автоматизированных камер хранения. */
@SpringBootApplication
public class LbCalcWebApplication {

    /** Запускает Spring Boot-приложение. */
    public static void main(String[] args) {
        SpringApplication.run(LbCalcWebApplication.class, args);
    }
}
