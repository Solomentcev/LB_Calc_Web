package com.lb_calc_web.domain.attributes;

import java.awt.*;

/** Цвета корпуса и дверей модулей, сопоставленные с цветами {@link java.awt.Color}. */
public enum Colors {
    Black(Color.BLACK),
    Blue(Color.BLUE),
    Cyan(Color.CYAN),
    DarkGray(Color.DARK_GRAY),
    Gray(Color.GRAY),
    Green(Color.GREEN),
    LightGray(Color.lightGray),
    Magenta(Color.MAGENTA),
    Orange(Color.ORANGE),
    Pink(Color.PINK),
    Red(Color.RED),
    White(Color.WHITE),
    Yellow(Color.YELLOW);
    final Color color;
    final String name;
    Colors(Color color){
        this.color=color;
        name= String.valueOf(this);
    }

    /** @return цвет для построения графического представления */
    public Color getColor() {
        return color;
    }

    /** @return имя константы цвета */
    public String getName() {
        return name;
    }
}
