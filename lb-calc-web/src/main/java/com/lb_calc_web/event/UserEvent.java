package com.lb_calc_web.event;
/** Событие изменения состояния учётной записи для передачи в сервис уведомлений. */
public class UserEvent {

    private UserEventType type;
    private Long userId;
    private String email;

    public UserEvent() {
    }

    public UserEvent(UserEventType type, Long userId, String email) {
        this.type = type;
        this.userId = userId;
        this.email = email;
    }

    public UserEventType getType() {
        return type;
    }

    public void setType(UserEventType type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}