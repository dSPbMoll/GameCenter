package com.example.gamecenter._2048;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.GridLayout;
import android.widget.TextView;

public class GGBackgroundLayer extends GridLayout {

    private Context mContext;
    private int rowAmmount = 4;
    private int colAmmount = 4;

    // ---------------------------------- CONSTRUCTORS ----------------------------------

    public GGBackgroundLayer(Context context) {
        super(context);
        mContext = context;
    }

    public GGBackgroundLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    // ---------------------------------- LAYOUT BUILDING ----------------------------------

    public void init() {
        setRowCount(rowAmmount);
        setColumnCount(colAmmount);

        int cellAmmount = rowAmmount * colAmmount;

        for (int i = 0; i < cellAmmount; i++) {
            TextView gridBox = new TextView(mContext);
            setTextViewParams(gridBox);
            addView(gridBox);
        }
    }

    private void setTextViewParams(TextView tv) {
        // 1. Tamaño (79dp -> px)
        int size = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 79, tv.getResources().getDisplayMetrics());

        MarginLayoutParams params = new MarginLayoutParams(size, size);

        // 2. Margen (5dp)
        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 5, tv.getResources().getDisplayMetrics());
        params.setMargins(margin, margin, margin, margin);

        tv.setLayoutParams(params);

        // 3. --- CAMBIO: FONDO REDONDEADO ---
        // En lugar de setBackgroundColor, usamos un Drawable con radio
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(15); // Radio de 15px (igual que en GridUtils)
        shape.setColor(Color.parseColor("#C2C2C2")); // Color gris del fondo del 2048

        tv.setBackground(shape);
    }

}
