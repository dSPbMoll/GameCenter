package com.example.gamecenter._2048;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.gamecenter.R;

import java.util.List;

public class GameGrid extends FrameLayout {

    private Context mContext;
    private int rowAmmount = 4;
    private int colAmmount = 4;

    // Capas
    private GGBackgroundLayer backgroundLayer;
    private GGNumberLayer numberLayer;
    private GGAnimationLayer animationLayer;
    private Integer[][] previousGridValues;
    private List<GridBoxDto> previousGridBoxes;

    // Control táctil
    private float lastX, lastY;
    private boolean inSameSwipe;
    private static final int SWIPE_THRESHOLD = 0;

    // Control de estado
    private boolean isAnimating = false; // Bloquea input mientras se mueven fichas
    private GameListener gameListener;

    public GameGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setGameListener(GameListener listener) {
        this.gameListener = listener;
        // Pasamos el listener también a la capa lógica
        if (numberLayer != null) {
            numberLayer.setGameListener(listener);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        backgroundLayer = findViewById(R.id.game_grid_background);
        numberLayer = findViewById(R.id.game_grid_numbers);
        animationLayer = findViewById(R.id.game_grid_animations);

        backgroundLayer.init();
        numberLayer.initGrid();
        numberLayer.setGameGrid(this);

        // Si el listener se configuró antes del inflate, lo asignamos ahora
        if (gameListener != null) numberLayer.setGameListener(gameListener);

        animationLayer.setGameGrid(this);

        // Esperamos a que se dibuje el layout para generar los primeros números
        backgroundLayer.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        generateNewNumberInGrid();
                        generateNewNumberInGrid();
                        previousGridValues = numberLayer.getGridValues();
                        previousGridBoxes = numberLayer.getCurrentStateAsDto();
                        backgroundLayer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(size, size); // Cuadrado perfecto
        int spec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        measureChildren(spec, spec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // --- BLOQUEO DE INPUT ---
        // Si las fichas se están moviendo, ignoramos el dedo para evitar crashes
        if (isAnimating) return true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                inSameSwipe = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (inSameSwipe) return true;

                float dx = event.getX() - lastX;
                float dy = event.getY() - lastY;

                if (Math.abs(dx) > Math.abs(dy)) {
                    if (Math.abs(dx) > SWIPE_THRESHOLD) {
                        if (dx > 0) swipe(Direction.RIGHT);
                        else swipe(Direction.LEFT);
                    }
                } else {
                    if (Math.abs(dy) > SWIPE_THRESHOLD) {
                        if (dy > 0) swipe(Direction.DOWN);
                        else swipe(Direction.UP);
                    }
                }
                lastX = event.getX();
                lastY = event.getY();

                inSameSwipe = true;
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void swipe(Direction dir) {
        // --- FIX STEP 1: Save the state BEFORE the swipe ---
        this.previousGridValues = numberLayer.getGridValues();
        this.previousGridBoxes = numberLayer.getCurrentStateAsDto();

        // 1. Calcular lógica (This will change the numberLayer's internal state)
        SwipeResultsDto swipeResults = numberLayer.swipe(dir);

        if (swipeResults.moved) {
            // 2. Bloquear input
            isAnimating = true;

            // 3. Preparar animación y limpiar originales
            animationLayer.prepareForAnimate(swipeResults.initialPositionLLList);
            numberLayer.removeMovingGridBoxes(swipeResults.initialPositionLLList);

            // 4. Ejecutar animación con Callback
            animationLayer.executeAnimations(swipeResults.animatorDtoList, () -> {

                // --- AL TERMINAR ANIMACIÓN ---
                numberLayer.setNewLLContents(swipeResults.finalPositionLLList);
                animationLayer.removeAnimationResidues();

                // Generar nuevo número
                generateNewNumberInGrid();

                // Comprobar Game Over
                numberLayer.checkGameOver();

                // Desbloquear input
                isAnimating = false;
            });
        }
    }

    public void generateNewNumberInGrid() {
        if (numberLayer != null) {
            numberLayer.generateNewNumberInGrid();
        }
    }

    // --- UTILS DE COORDENADAS ---

    public TextView textViewToGridBox(TextView tv, int x, int y) {
        TextView cell = (TextView) backgroundLayer.getChildAt(xYCordsToGGIndex(x, y));

        int[] bgLocation = new int[2];
        backgroundLayer.getLocationOnScreen(bgLocation);
        int[] numberLayerLocation = new int[2];
        this.getLocationOnScreen(numberLayerLocation);

        float xPos = cell.getX() + (bgLocation[0] - numberLayerLocation[0]);
        float yPos = cell.getY() + (bgLocation[1] - numberLayerLocation[1]);

        tv.setX(xPos);
        tv.setY(yPos);

        LayoutParams lp = new LayoutParams(cell.getWidth(), cell.getHeight());
        lp.setMargins(0, 0, 0, 0); // Corrección visual importante
        tv.setLayoutParams(lp);
        tv.setGravity(Gravity.CENTER);

        return tv;
    }

    public float getPixelXForGridIndex(int col, int row) {
        int index = xYCordsToGGIndex(col, row);
        TextView bgCell = (TextView) backgroundLayer.getChildAt(index);
        int[] bgLocation = new int[2];
        backgroundLayer.getLocationOnScreen(bgLocation);
        int[] numberLayerLocation = new int[2];
        this.getLocationOnScreen(numberLayerLocation);
        return bgCell.getX() + (bgLocation[0] - numberLayerLocation[0]);
    }

    public float getPixelYForGridIndex(int col, int row) {
        int index = xYCordsToGGIndex(col, row);
        TextView bgCell = (TextView) backgroundLayer.getChildAt(index);
        int[] bgLocation = new int[2];
        backgroundLayer.getLocationOnScreen(bgLocation);
        int[] numberLayerLocation = new int[2];
        this.getLocationOnScreen(numberLayerLocation);
        return bgCell.getY() + (bgLocation[1] - numberLayerLocation[1]);
    }

    public int xYCordsToGGIndex(int x, int y) {
        return y * rowAmmount + x;
    }

    public void resetGame() {
        if (numberLayer != null) {
            numberLayer.resetGame();
            previousGridValues = numberLayer.getGridValues();
            previousGridBoxes = numberLayer.getCurrentStateAsDto();
        }
    }

    public void rollbackNumberLayer() {
        numberLayer.rollbackValues(previousGridValues);
        numberLayer.rollbackLLContents(previousGridBoxes);
    }
}