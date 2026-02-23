package com.example.gamecenter._2048;

import android.widget.TextView;

public class GridBoxDto {
    public TextView initialTv; // Referencia a la vista original (para borrarla)
    public int x; // Índice Columna (0-3)
    public int y; // Índice Fila (0-3)
    public String value; // EL VALOR DEL TEXTO (Ej: "4", "2048")

    public GridBoxDto(TextView initialTv, int x, int y, String value) {
        this.initialTv = initialTv;
        this.x = x;
        this.y = y;
        this.value = value;
    }
}