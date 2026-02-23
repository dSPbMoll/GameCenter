package com.example.gamecenter._2048;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

public class GridUtils {

    public static void giveColorToTextView(TextView tv) {
        String valStr = tv.getText().toString();
        if (valStr.isEmpty()) {
            applyStyle(tv, "#CDC1B4"); // Color celda vacía estándar 2048
            return;
        }

        String colorString;
        try {
            int val = Integer.parseInt(valStr);
            switch (val) {
                case 2: colorString = "#E0E0E0"; break;
                case 4: colorString = "#EDE0C8"; break;
                case 8: colorString = "#FFA557"; break;
                case 16: colorString = "#FF591F"; break;
                case 32: colorString = "#FF391F"; break;
                case 64: colorString = "#FF1717"; break;
                case 128: colorString = "#EDCF72"; break;
                case 256: colorString = "#EDCC61"; break;
                case 512: colorString = "#EDC850"; break;
                case 1024: colorString = "#EDC53F"; break;
                case 2048: colorString = "#EDC22E"; break;
                default: colorString = "#3C3A32";
            }
        } catch (NumberFormatException e) {
            colorString = "#CDC1B4";
        }

        applyStyle(tv, colorString);

        // Ajuste de color de texto (blanco para números altos, oscuro para bajos)
        int val = Integer.parseInt(valStr);
        if (val >= 8) tv.setTextColor(Color.WHITE);
        else tv.setTextColor(Color.parseColor("#776E65"));
    }

    private static void applyStyle(TextView tv, String colorHex) {
        // Usamos GradientDrawable para dar bordes redondeados (estética 2048)
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(15); // Radio de esquina
        shape.setColor(Color.parseColor(colorHex));
        tv.setBackground(shape);
    }
}