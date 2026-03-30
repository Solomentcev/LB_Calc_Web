package com.lb_calc_web.controller.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lb_calc_web.dto.JwtResponse;

import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    public String status;
    public T data;

    public String message;
    public List<String> errors;

    public LocalDate timestamp;

    private ApiResponse() {}

    private static <T> ApiResponse<T> base(String status) {
        ApiResponse<T> r = new ApiResponse<>();
        r.status = status;
        r.timestamp = LocalDate.now();
        return r;
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = base("success");
        r.data = data;
        return r;
    }
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> r = base("success");
        r.message = message;
        r.data = data;
        return r;
    }
    public static <T> ApiResponse<T> success(String message) {
        ApiResponse<T> r = base("success");
        r.message = message;
        return r;
    }
    public static ApiResponse<Void> error(String message) {
        ApiResponse<Void> r = base("error");
        r.message = message;
        return r;
    }
    public static <T> ApiResponse<T> error(T data) {
        ApiResponse<T> r = base("error");
        r.data=data;
        return r;
    }

    public static ApiResponse<Void> error(List<String> errors) {
        ApiResponse<Void> r = base("error");
        r.errors = errors;
        return r;
    }

    public static ApiResponse<Void> error(String message, List<String> errors) {
        ApiResponse<Void> r = error(message);
        r.errors = errors;
        return r;
    }
    public static <T> ApiResponse<T> error(String message, T data) {
        ApiResponse<T> r = base("error");
        r.message = message;
        r.data=data;
        return r;
    }
}