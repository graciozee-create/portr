package com.sandymandy.pleasurehorizons.util;

public class ScreenUtils {
    public static boolean isMouseOverHere(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
