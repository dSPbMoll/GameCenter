package com.example.gamecenter._2048;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GGAnimationLayer extends FrameLayout {

    private Context mContext;
    private GameGrid gameGrid;
    private AnimatorSet animatorSet = new AnimatorSet();
    private List<TextView> gridBoxesForAnimate = new ArrayList<>();


    // ---------------------------------- CONSTRUCTORS ----------------------------------

    public GGAnimationLayer(Context context) {
        super(context);
        mContext = context;
    }

    public GGAnimationLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    // ---------------------------------- LAYOUT BUILDING ----------------------------------

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setGameGrid(GameGrid gg) {
        gameGrid = gg;
    }

    // En GGAnimationLayer.java

    // En GGAnimationLayer.java

    public void prepareForAnimate(List<GridBoxDto> initialPositionLLList) {
        for (GridBoxDto gbDto : initialPositionLLList) {

            TextView tv = new TextView(mContext);
            tv.setText(gbDto.value);
            GridUtils.giveColorToTextView(tv); // <--- LIMPIO

            // -----------------------------------------------------------------------------

            // Coordenadas corregidas (Tu código ya lo tenía bien)
            float pixelX = gameGrid.getPixelXForGridIndex(gbDto.x, gbDto.y);
            float pixelY = gameGrid.getPixelYForGridIndex(gbDto.x, gbDto.y);

            // Fallback size seguro
            int size = 200;
            if (gbDto.initialTv != null) size = gbDto.initialTv.getWidth();
            // Si sigue siendo 0 (porque no se ha dibujado), intentamos calcularlo
            if (size == 0) size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 79, getResources().getDisplayMetrics());

            LayoutParams lp = new LayoutParams(size, size);
            tv.setLayoutParams(lp);

            tv.setGravity(Gravity.CENTER);
            tv.setX(pixelX);
            tv.setY(pixelY);

            addView(tv);
            gridBoxesForAnimate.add(tv);
        }
    }

    // Añade este método auxiliar al final de la clase GGAnimationLayer

    public void executeAnimations(List<AnimatorDto> animatorDtoList, Runnable onAnimationEndCallback) {
        List<Animator> animatorList = new ArrayList<>();

        for (TextView tv : gridBoxesForAnimate) {
            int animationIndex = indexOfChild(tv);
            if (animationIndex < animatorDtoList.size()) {
                AnimatorDto currentAnimation = animatorDtoList.get(animationIndex);
                ObjectAnimator animator = ObjectAnimator.ofFloat(tv, currentAnimation.animationType, currentAnimation.startPosInQuadrant, currentAnimation.endPosInQuadrant);
                animatorList.add(animator);
            }
        }

        if (animatorList.isEmpty()) {
            // Si no hay animaciones, ejecutamos el final inmediatamente
            if (onAnimationEndCallback != null) onAnimationEndCallback.run();
            return;
        }

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatorList);
        animatorSet.setDuration(200); // 200ms es una buena velocidad

        // AQUÍ ESTÁ LA SOLUCIÓN: El Listener
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // Esto se ejecuta SOLO cuando el tiempo de la animación termina
                if (onAnimationEndCallback != null) {
                    onAnimationEndCallback.run();
                }
            }
        });

        animatorSet.start();
    }

    public void removeAnimationResidues() {
        removeAllViews();
        gridBoxesForAnimate.clear();
    }



}
