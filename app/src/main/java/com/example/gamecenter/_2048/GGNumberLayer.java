package com.example.gamecenter._2048;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GGNumberLayer extends FrameLayout {

    private Context mContext;
    private GameGrid gameGrid;
    private TextView[][] gridBoxes;
    private Integer[][] gridValues;
    private ArrayList<Integer[]> emptyCellIndexes;
    private TextView lastNumGenerated;
    private Integer[] lastNumGeneratedIndex;
    private int rowAmmount = 4;
    private int colAmmount = 4;

    // Listener para comunicar puntos a MainActivity
    private GameListener gameListener;

    public GGNumberLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void setGameGrid(GameGrid gg) {
        this.gameGrid = gg;
    }
    public void setGameListener(GameListener listener) {
        this.gameListener = listener;
    }
    public Integer[][] getGridValues() {
        Integer[][] copy = new Integer[rowAmmount][colAmmount];
        for (int i = 0; i < rowAmmount; i++) {
            for (int j = 0; j < colAmmount; j++) {
                copy[i][j] = gridValues[i][j];
            }
        }
        return copy;
    }

    public void initGrid() {
        gridBoxes = new TextView[rowAmmount][colAmmount];
        gridValues = new Integer[rowAmmount][colAmmount];
        emptyCellIndexes = new ArrayList<>();
        for (int i = 0; i < rowAmmount; i++) {
            for (int j = 0; j < colAmmount; j++) {
                emptyCellIndexes.add(new Integer[]{i, j});
            }
        }
    }

    public void checkGameOver() {
        if (!emptyCellIndexes.isEmpty()) return; // Si hay huecos, seguimos jugando

        // Si está lleno, comprobamos si hay fusiones posibles
        for (int i = 0; i < rowAmmount; i++) {
            for (int j = 0; j < colAmmount; j++) {
                Integer current = gridValues[i][j];
                // Mirar derecha
                if (j < colAmmount - 1 && current != null && current.equals(gridValues[i][j + 1])) return;
                // Mirar abajo
                if (i < rowAmmount - 1 && current != null && current.equals(gridValues[i + 1][j])) return;
            }
        }
        // Si llegamos aquí, no hay movimientos posibles
        if (gameListener != null) gameListener.onGameOver();
    }

    private void addScore(int points) {
        if (gameListener != null) {
            // This line tells MainActivity to save the current score before updating it.
            gameListener.updatePreviousStepScore();
            gameListener.onScoreChanged(points);
        }
    }

    public void generateNewNumberInGrid() {
        if (emptyCellIndexes.isEmpty()) {
            recalculateEmptyCells();
            if (emptyCellIndexes.isEmpty()) return;
        }

        double chanceOf4 = 0.1;
        int value = Math.random() < chanceOf4 ? 4 : 2;
        int randomListIndex = (int) (Math.random() * emptyCellIndexes.size());
        Integer[] cellIndex = emptyCellIndexes.get(randomListIndex);
        int y = cellIndex[0];
        int x = cellIndex[1];

        // Seguridad anti-solapamiento
        if (gridValues[y][x] != null || gridBoxes[y][x] != null) {
            emptyCellIndexes.remove(randomListIndex);
            generateNewNumberInGrid();
            return;
        }

        TextView textView = new TextView(mContext);
        textView.setText(String.valueOf(value));

        // Uso de la clase auxiliar para limpiar código
        GridUtils.giveColorToTextView(textView);

        textView = gameGrid.textViewToGridBox(textView, x, y);

        // CRUCIAL: Asignar Tag para que el sistema anti-fantasmas funcione
        textView.setTag("r" + y + "c" + x);

        addView(textView);
        gridBoxes[y][x] = textView;
        gridValues[y][x] = value;
        emptyCellIndexes.remove(randomListIndex);

        this.lastNumGenerated = textView;
        this.lastNumGeneratedIndex = cellIndex;
    }

    public SwipeResultsDto swipe(Direction dir) {
        switch (dir) {
            case LEFT: return swipeLeft();
            case RIGHT: return swipeRight();
            case UP: return swipeUp();
            default: return swipeDown();
        }
    }

    private SwipeResultsDto swipeRight() {
        List<GridBoxDto> initialList = new ArrayList<>();
        List<AnimatorDto> animList = new ArrayList<>();
        boolean moved = false;
        boolean[][] merged = new boolean[rowAmmount][colAmmount];
        boolean[][] toUpdate = new boolean[rowAmmount][colAmmount];

        for (int i = 0; i < rowAmmount; i++) {
            for (int j = colAmmount - 2; j >= 0; j--) {
                Integer val = gridValues[i][j];
                if (val == null) continue;

                int nextX = j + 1;
                while (nextX < colAmmount && gridValues[i][nextX] == null) nextX++;
                int targetX = nextX - 1;
                TextView movingBox = gridBoxes[i][j];

                if (nextX < colAmmount && gridValues[i][nextX].equals(val) && !merged[i][nextX]) {
                    // FUSIÓN
                    moved = true;
                    int newVal = val * 2;
                    addScore(newVal); // Sumar puntos

                    gridValues[i][nextX] = newVal;
                    gridValues[i][j] = null;
                    merged[i][nextX] = true;
                    toUpdate[i][nextX] = true;

                    TextView targetBox = gridBoxes[i][nextX];
                    initialList.add(new GridBoxDto(movingBox, j, i, String.valueOf(val)));
                    animList.add(new AnimatorDto("translationX", movingBox.getX(), gameGrid.getPixelXForGridIndex(nextX, i)));

                    // Protección anti-fantasmas con Tags
                    if (targetBox != null && isCorrectTag(targetBox, i, nextX)) {
                        initialList.add(new GridBoxDto(targetBox, nextX, i, String.valueOf(val)));
                        float endX = gameGrid.getPixelXForGridIndex(nextX, i);
                        animList.add(new AnimatorDto("translationX", endX, endX));
                    }
                    gridBoxes[i][nextX] = null;
                    gridBoxes[i][j] = null;

                } else if (targetX > j) {
                    // MOVER
                    moved = true;
                    gridValues[i][targetX] = val;
                    gridValues[i][j] = null;
                    toUpdate[i][targetX] = true;

                    initialList.add(new GridBoxDto(movingBox, j, i, String.valueOf(val)));
                    animList.add(new AnimatorDto("translationX", movingBox.getX(), gameGrid.getPixelXForGridIndex(targetX, i)));

                    gridBoxes[i][targetX] = movingBox;
                    gridBoxes[i][j] = null;
                }
            }
        }
        return buildResult(initialList, animList, moved, toUpdate);
    }

    private SwipeResultsDto swipeLeft() {
        List<GridBoxDto> initialList = new ArrayList<>();
        List<AnimatorDto> animList = new ArrayList<>();
        boolean moved = false;
        boolean[][] merged = new boolean[rowAmmount][colAmmount];
        boolean[][] toUpdate = new boolean[rowAmmount][colAmmount];

        for (int i = 0; i < rowAmmount; i++) {
            for (int j = 1; j < colAmmount; j++) {
                Integer val = gridValues[i][j];
                if (val == null) continue;

                int nextX = j - 1;
                while (nextX >= 0 && gridValues[i][nextX] == null) nextX--;
                int targetX = nextX + 1;
                TextView movingBox = gridBoxes[i][j];

                if (nextX >= 0 && gridValues[i][nextX].equals(val) && !merged[i][nextX]) {
                    // FUSIÓN
                    moved = true;
                    int newVal = val * 2;
                    addScore(newVal);

                    gridValues[i][nextX] = newVal;
                    gridValues[i][j] = null;
                    merged[i][nextX] = true;
                    toUpdate[i][nextX] = true;

                    TextView targetBox = gridBoxes[i][nextX];
                    initialList.add(new GridBoxDto(movingBox, j, i, String.valueOf(val)));
                    animList.add(new AnimatorDto("translationX", movingBox.getX(), gameGrid.getPixelXForGridIndex(nextX, i)));

                    if (targetBox != null && isCorrectTag(targetBox, i, nextX)) {
                        initialList.add(new GridBoxDto(targetBox, nextX, i, String.valueOf(val)));
                        float endX = gameGrid.getPixelXForGridIndex(nextX, i);
                        animList.add(new AnimatorDto("translationX", endX, endX));
                    }
                    gridBoxes[i][nextX] = null;
                    gridBoxes[i][j] = null;

                } else if (targetX < j) {
                    // MOVER
                    moved = true;
                    gridValues[i][targetX] = val;
                    gridValues[i][j] = null;
                    toUpdate[i][targetX] = true;

                    initialList.add(new GridBoxDto(movingBox, j, i, String.valueOf(val)));
                    animList.add(new AnimatorDto("translationX", movingBox.getX(), gameGrid.getPixelXForGridIndex(targetX, i)));

                    gridBoxes[i][targetX] = movingBox;
                    gridBoxes[i][j] = null;
                }
            }
        }
        return buildResult(initialList, animList, moved, toUpdate);
    }

    private SwipeResultsDto swipeUp() {
        List<GridBoxDto> initialList = new ArrayList<>();
        List<AnimatorDto> animList = new ArrayList<>();
        boolean moved = false;
        boolean[][] merged = new boolean[rowAmmount][colAmmount];
        boolean[][] toUpdate = new boolean[rowAmmount][colAmmount];

        for (int j = 0; j < colAmmount; j++) {
            for (int i = 1; i < rowAmmount; i++) {
                Integer val = gridValues[i][j];
                if (val == null) continue;

                int nextY = i - 1;
                while (nextY >= 0 && gridValues[nextY][j] == null) nextY--;
                int targetY = nextY + 1;
                TextView movingBox = gridBoxes[i][j];

                if (nextY >= 0 && gridValues[nextY][j].equals(val) && !merged[nextY][j]) {
                    // FUSIÓN
                    moved = true;
                    int newVal = val * 2;
                    addScore(newVal);

                    gridValues[nextY][j] = newVal;
                    gridValues[i][j] = null;
                    merged[nextY][j] = true;
                    toUpdate[nextY][j] = true;

                    TextView targetBox = gridBoxes[nextY][j];
                    initialList.add(new GridBoxDto(movingBox, j, i, String.valueOf(val)));
                    animList.add(new AnimatorDto("translationY", movingBox.getY(), gameGrid.getPixelYForGridIndex(j, nextY)));

                    if (targetBox != null && isCorrectTag(targetBox, nextY, j)) {
                        initialList.add(new GridBoxDto(targetBox, j, nextY, String.valueOf(val)));
                        float endY = gameGrid.getPixelYForGridIndex(j, nextY);
                        animList.add(new AnimatorDto("translationY", endY, endY));
                    }
                    gridBoxes[nextY][j] = null;
                    gridBoxes[i][j] = null;

                } else if (targetY < i) {
                    // MOVER
                    moved = true;
                    gridValues[targetY][j] = val;
                    gridValues[i][j] = null;
                    toUpdate[targetY][j] = true;

                    initialList.add(new GridBoxDto(movingBox, j, i, String.valueOf(val)));
                    animList.add(new AnimatorDto("translationY", movingBox.getY(), gameGrid.getPixelYForGridIndex(j, targetY)));

                    gridBoxes[targetY][j] = movingBox;
                    gridBoxes[i][j] = null;
                }
            }
        }
        return buildResult(initialList, animList, moved, toUpdate);
    }

    private SwipeResultsDto swipeDown() {
        List<GridBoxDto> initialList = new ArrayList<>();
        List<AnimatorDto> animList = new ArrayList<>();
        boolean moved = false;
        boolean[][] merged = new boolean[rowAmmount][colAmmount];
        boolean[][] toUpdate = new boolean[rowAmmount][colAmmount];

        for (int j = 0; j < colAmmount; j++) {
            for (int i = rowAmmount - 2; i >= 0; i--) {
                Integer val = gridValues[i][j];
                if (val == null) continue;

                int nextY = i + 1;
                while (nextY < rowAmmount && gridValues[nextY][j] == null) nextY++;
                int targetY = nextY - 1;
                TextView movingBox = gridBoxes[i][j];

                if (nextY < rowAmmount && gridValues[nextY][j].equals(val) && !merged[nextY][j]) {
                    // FUSIÓN
                    moved = true;
                    int newVal = val * 2;
                    addScore(newVal);

                    gridValues[nextY][j] = newVal;
                    gridValues[i][j] = null;
                    merged[nextY][j] = true;
                    toUpdate[nextY][j] = true;

                    TextView targetBox = gridBoxes[nextY][j];
                    initialList.add(new GridBoxDto(movingBox, j, i, String.valueOf(val)));
                    animList.add(new AnimatorDto("translationY", movingBox.getY(), gameGrid.getPixelYForGridIndex(j, nextY)));

                    if (targetBox != null && isCorrectTag(targetBox, nextY, j)) {
                        initialList.add(new GridBoxDto(targetBox, j, nextY, String.valueOf(val)));
                        float endY = gameGrid.getPixelYForGridIndex(j, nextY);
                        animList.add(new AnimatorDto("translationY", endY, endY));
                    }
                    gridBoxes[nextY][j] = null;
                    gridBoxes[i][j] = null;

                } else if (targetY > i) {
                    // MOVER
                    moved = true;
                    gridValues[targetY][j] = val;
                    gridValues[i][j] = null;
                    toUpdate[targetY][j] = true;

                    initialList.add(new GridBoxDto(movingBox, j, i, String.valueOf(val)));
                    animList.add(new AnimatorDto("translationY", movingBox.getY(), gameGrid.getPixelYForGridIndex(j, targetY)));

                    gridBoxes[targetY][j] = movingBox;
                    gridBoxes[i][j] = null;
                }
            }
        }
        return buildResult(initialList, animList, moved, toUpdate);
    }

    // --- UTILS DE LA CLASE ---

    private boolean isCorrectTag(TextView tv, int r, int c) {
        Object tag = tv.getTag();
        return tag != null && tag.equals("r" + r + "c" + c);
    }

    private SwipeResultsDto buildResult(List<GridBoxDto> init, List<AnimatorDto> anim, boolean moved, boolean[][] updateMap) {
        List<GridBoxDto> end = new ArrayList<>();
        for (int i = 0; i < rowAmmount; i++) {
            for (int j = 0; j < colAmmount; j++) {
                if (updateMap[i][j]) {
                    end.add(new GridBoxDto(null, j, i, String.valueOf(gridValues[i][j])));
                }
            }
        }
        recalculateEmptyCells();
        return new SwipeResultsDto(init, end, anim, moved);
    }

    public void setNewLLContents(List<GridBoxDto> finalPositionLLList) {
        for (GridBoxDto gbDto : finalPositionLLList) {
            TextView tv = new TextView(mContext);
            tv.setText(gbDto.value);
            GridUtils.giveColorToTextView(tv);
            tv = gameGrid.textViewToGridBox(tv, gbDto.x, gbDto.y);
            tv.setTag("r" + gbDto.y + "c" + gbDto.x);
            addView(tv);
            gridBoxes[gbDto.y][gbDto.x] = tv;
        }
    }

    public void clearChildren() {
        int childIndex = 0;
        while (this.getChildCount() > 0) {
            this.removeViewAt(childIndex);
            childIndex++;
        }
    }

    public void removeMovingGridBoxes(List<GridBoxDto> gridBoxesForRemove) {
        for (GridBoxDto gbDto : gridBoxesForRemove) {
            if (gbDto.initialTv != null) removeView(gbDto.initialTv);
            if (gridBoxes[gbDto.y][gbDto.x] != null) {
                removeView(gridBoxes[gbDto.y][gbDto.x]);
                gridBoxes[gbDto.y][gbDto.x] = null;
            }
            View viewWithTag = findViewWithTag("r" + gbDto.y + "c" + gbDto.x);
            if (viewWithTag != null) removeView(viewWithTag);
        }
    }

    private void recalculateEmptyCells() {
        emptyCellIndexes.clear();
        for (int i = 0; i < rowAmmount; i++) {
            for (int j = 0; j < colAmmount; j++) {
                if (gridValues[i][j] == null) emptyCellIndexes.add(new Integer[]{i, j});
            }
        }
    }

    public void resetGame() {
        // 1. Limpiar vistas
        removeAllViews();

        // 2. Reiniciar arrays y listas
        initGrid();

        // 3. Generar los dos números iniciales
        generateNewNumberInGrid();
        generateNewNumberInGrid();
    }

    public void rollbackValues(Integer[][] values) {
        this.gridValues = values;
    }

    // Cambia tu método rollbackLLContents por esto:
    public void rollbackLLContents(List<GridBoxDto> previousGridState) {
        // 1. Limpiar completamente las vistas actuales de la pantalla.
        removeAllViews();

        // 2. Reiniciar el array de TextViews. MUY IMPORTANTE.
        this.gridBoxes = new TextView[rowAmmount][colAmmount];

        // 3. Re-crear las vistas y re-poblar gridBoxes usando la lógica que ya tienes.
        // El método setNewLLContents ya añade las vistas y las pone en el array gridBoxes.
        setNewLLContents(previousGridState);

        // 4. Recalcular las celdas vacías basándose en el estado restaurado.
        recalculateEmptyCells();
    }

    public List<GridBoxDto> getCurrentStateAsDto() {
        List<GridBoxDto> currentState = new ArrayList<>();
        for (int i = 0; i < rowAmmount; i++) {
            for (int j = 0; j < colAmmount; j++) {
                if (gridValues[i][j] != null) {
                    // Creamos un DTO con la información de la celda
                    currentState.add(new GridBoxDto(null, j, i, String.valueOf(gridValues[i][j])));
                }
            }
        }
        return currentState;
    }


}