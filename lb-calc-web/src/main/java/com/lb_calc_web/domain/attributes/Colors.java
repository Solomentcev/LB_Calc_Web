package com.lb_calc_web.domain.attributes;

/** Цвета корпуса и дверей модулей в независимом от графической библиотеки RGB-представлении. */
public enum Colors {
    Black(0x000000),
    Blue(0x0000FF),
    Cyan(0x00FFFF),
    DarkGray(0x404040),
    Gray(0x808080),
    Green(0x00FF00),
    LightGray(0xC0C0C0),
    Magenta(0xFF00FF),
    Orange(0xFFC800),
    Pink(0xFFAFAF),
    Red(0xFF0000),
    White(0xFFFFFF),
    Yellow(0xFFFF00);

    private final int rgb;

    Colors(int rgb) {
        this.rgb = rgb;
    }

    /** Возвращает RGB-значение цвета без зависимости домена от AWT. */
    public int getRgb() {
        return rgb;
    }

    /** Возвращает имя константы цвета. */
    public String getName() {
        return name();
    }
}
