package com.example.gamecenter._2048;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameGridTest extends GridLayout {

    private Context mContext;
    private float lastX, lastY;

    private LinearLayout[][] gridCells;
    private Integer[][] gridValues;
    private ArrayList<Integer[]> emptyCellIndexes;
    private int cellAmmount;
    private int rowAmmount;
    private int colAmmount;

    private boolean inSameSwipe;
    private enum Direction {UP, DOWN, LEFT, RIGHT};

    private static final int SWIPE_THRESHOLD = 0;


    public GameGridTest(Context context) {
        super(context);
        mContext = context;
    }

    public GameGridTest(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
                        if (dx > 0) {
                            swipe(Direction.RIGHT);
                        }
                        else {
                            swipe(Direction.LEFT);
                        }
                    }
                } else {
                    if (Math.abs(dy) > SWIPE_THRESHOLD) {
                        if (dy > 0) {
                            swipe(Direction.DOWN);
                        }
                        else {
                            swipe(Direction.UP);
                        }
                    }
                }
                lastX = event.getX();
                lastY = event.getY();

                inSameSwipe = true;
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        rowAmmount = getRowCount();
        colAmmount = getColumnCount();
        cellAmmount = rowAmmount * colAmmount;
        gridCells = new LinearLayout[rowAmmount][colAmmount];
        gridValues = new Integer[rowAmmount][colAmmount];
        emptyCellIndexes = new ArrayList<>();

        for (int i = 0; i < cellAmmount; i++) {
            // Fill the gameGrid with void layouts
            LinearLayout cell = new LinearLayout(mContext);
            setLinearLayoutParams(cell);
            addView(cell);

            // Put an empty text view in the layout that has been created
            TextView tv = new TextView(mContext);
            setTextViewParams(tv);
            cell.addView(tv, 0);

            // Calculate row and column using the index
            int row = i / colAmmount;
            int col = i % colAmmount;

            // Kepp the reference in the array
            gridCells[row][col] = cell;

            // Initialize its vale as null
            gridValues[row][col] = null;

            // Keep the indexes in the empty cell index array
            emptyCellIndexes.add(generateNewIndex(row, col));

            // Si quieres acceder al TextView dentro del LinearLayout
            TextView cellText = (TextView) cell.getChildAt(0);

            // Ejemplo: inicializamos el texto
            cellText.setText("");
        }

        generateNewNumberInGrid();
        generateNewNumberInGrid();

    }

    private void setLinearLayoutParams(LinearLayout ll) {
        // Convertir 82dp a px
        int size = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                82,
                ll.getResources().getDisplayMetrics()
        );

        // LayoutParams genérico (si está dentro de otro ViewGroup, se ajustará)
        MarginLayoutParams params = new MarginLayoutParams(size, size);

        // Margen uniforme 5dp
        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                5,
                ll.getResources().getDisplayMetrics()
        );
        params.setMargins(margin, margin, margin, margin);

        ll.setLayoutParams(params);

        // Propiedades del LinearLayout
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        ll.setBackgroundColor(Color.parseColor("#C9C9C9"));
    }

    private void setTextViewParams(TextView tv) {
        // LayoutParams wrap_content
        ViewGroup.LayoutParams params = tv.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        } else {
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        tv.setLayoutParams(params);

        // Texto vacío
        tv.setText("");

        // Centrar dentro del LinearLayout
        tv.setGravity(Gravity.CENTER);

        // Tamaño de texto 45sp
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);
    }

    private void generateNewNumberInGrid() {
        // Decide which value will take the new number
        double chanceOf4 = 0.1; // 10%
        int value = Math.random() < chanceOf4 ? 4 : 2;

        // Decide which cell will contain the chosen value
        int randomEmptyCellIndex = (int) (Math.random() * emptyCellIndexes.size());
        Integer[] cellIndex = emptyCellIndexes.get(randomEmptyCellIndex);
        int cellRow = cellIndex[0];
        int cellCol = cellIndex[1];

        // Get the layout and assign it the new value
        LinearLayout chosenLL = gridCells[cellRow][cellCol];
        TextView chosenTextView = (TextView) chosenLL.getChildAt(0);
        chosenTextView.setText(String.valueOf(value));
        giveColorToLayout(chosenLL);

        // Update the other record arrays with the new info
        gridValues[cellRow][cellCol] = value;
        emptyCellIndexes.remove(randomEmptyCellIndex);

    }

    private Integer[] generateNewIndex(int row, int col) {
        Integer[] index = new Integer[2];
        index[0] = row;
        index[1] = col;
        return index;
    }

    private void giveColorToLayout(LinearLayout linearLayout) {
        TextView tv = (TextView) linearLayout.getChildAt(0);

        if (tv.getText().equals("")) {
            linearLayout.setBackgroundColor(Color.parseColor("#C9C9C9"));
            return;
        }

        String colorString = "#C9C9C9";

        switch (Integer.parseInt(String.valueOf(tv.getText()))) {
            case 2:
                colorString = "#F2F2F2";
                break;
            case 4:
                colorString = "#FFFFB0";
                break;
            case 8:
                colorString = "#FFA426";
                break;
            case 16:
                colorString = "#FF931C";
                break;
            case 32:
                colorString = "#FF5959";
                break;
            case 64:
                colorString = "#FF4617";
                break;
            case 128:
                colorString = "#FFE963";
                break;
            case 256:
                colorString = "#FFE84D";
                break;
            case 512:
                colorString = "#FAE341";
                break;
            case 1024:
                colorString = "#FFE72E";
                break;
            case 2048:
                colorString = "#FFE517";
                break;
            default:
                colorString = "#141414";
        }
        linearLayout.setBackgroundColor(Color.parseColor(colorString));
    }

    private void swipe(Direction dir) {
        boolean moved = false;

        switch (dir) {
            case LEFT:
                moved = swipeLeft();
                break;
            case RIGHT:
                moved = swipeRight();
                break;
            case UP:
                moved = swipeUp();
                break;
            case DOWN:
                moved = swipeDown();
                break;
        }

        if (moved) {
            generateNewNumberInGrid();
        }
    }

    private boolean swipeRight() {
        boolean moved = false;
        AnimatorSet animSet = new AnimatorSet();
        List<Animator> animList = new ArrayList<>();
        List<GridBoxDto> destinyLinearLayouts;


        for (int i = 0; i < rowAmmount; i++) {
            Integer currVal = gridValues[i][colAmmount-1];
            int currValJ = colAmmount-1;

            for (int j = colAmmount -2; j >= 0; j--) {
            // Ignore the right column as it will never move when swiping right

                Integer currVal2 = gridValues[i][j];

                if (currVal2 != null) {
                    moved = true;
                    if (currVal == null) {
                    // If a box is sliding into a void space
                        //Todo SwipeAnimRight(i, currValJ, j);
                        gridValues[i][currValJ] = currVal2;
                        gridValues[i][j] = null;

                        LinearLayout movingBox = gridCells[i][j];
                        LinearLayout destinyBox = gridCells[i][currValJ];
                        TextView mtv = (TextView) gridCells[i][j].getChildAt(0);
                        TextView dtv = (TextView) gridCells[i][currValJ].getChildAt(0);
                        dtv.setText(mtv.getText());
                        mtv.setText("");
                        giveColorToLayout(movingBox);
                        giveColorToLayout(destinyBox);

                        float startX = movingBox.getX();
                        float endX = destinyBox.getX();

                        ObjectAnimator anim = ObjectAnimator.ofFloat(movingBox, "translationX", startX, endX);
                        animList.add(anim);

                        currValJ = currValJ - 1;
                        currVal = gridValues[i][currValJ];
                        emptyCellIndexes.add(generateNewIndex(i, j));
                        emptyCellIndexes.remove(i*4 + currValJ);

                    } else if (Objects.equals(currVal, currVal2)) {
                    // If a box is sliding and fusing into its x2
                        //Todo SwipeAnimRight(i, currValJ, j);
                        //Todo PopFusionAnim(i, currValJ, j);
                        gridValues[i][currValJ] = currVal * 2;
                        gridValues[i][j] = null;

                        LinearLayout movingBox = gridCells[i][j];
                        LinearLayout destinyBox = gridCells[i][currValJ];
                        TextView mtv = (TextView) gridCells[i][j].getChildAt(0);
                        TextView dtv = (TextView) gridCells[i][currValJ].getChildAt(0);
                        mtv.setText("");
                        dtv.setText(String.valueOf(currVal * 2));
                        giveColorToLayout(movingBox);
                        giveColorToLayout(destinyBox);

                        float startX = movingBox.getX();
                        float endX = destinyBox.getX();

                        ObjectAnimator anim = ObjectAnimator.ofFloat(movingBox, "translationX", startX, endX);
                        animList.add(anim);

                        currValJ = currValJ - 1;
                        currVal = gridValues[i][currValJ];
                        emptyCellIndexes.add(generateNewIndex(i, j));

                    } else {
                    // If a box is moving into the side of the currVal one because they can't fuse
                        if (j == currValJ -1) {
                        // If it won't move at all because origin == destiny
                            currVal = gridValues[i][j];
                            currValJ = j;
                        } else {
                            //Todo SwipeAnimRight(i, currValJ -1, j);
                            gridValues[i][currValJ -1] = currVal2;
                            gridValues[i][j] = null;

                            LinearLayout movingBox = gridCells[i][j];
                            LinearLayout destinyBox = gridCells[i][currValJ -1];
                            TextView mtv = (TextView) movingBox.getChildAt(0);
                            TextView dtv = (TextView) destinyBox.getChildAt(0);
                            dtv.setText(mtv.getText());
                            mtv.setText("");
                            giveColorToLayout(movingBox);
                            giveColorToLayout(destinyBox);

                            float startX = movingBox.getX();
                            float endX = destinyBox.getX();

                            ObjectAnimator anim = ObjectAnimator.ofFloat(movingBox, "translationX", startX, endX);
                            animList.add(anim);

                            currValJ = j - 1;
                            currVal = gridValues[i][currValJ];
                            emptyCellIndexes.add(generateNewIndex(i, j-1));
                            emptyCellIndexes.remove(i*4 + currValJ -1);

                        }
                    }
                }
            }
        }

        if (moved) {
            animSet.playTogether(animList);
            animSet.setDuration(750);
            animSet.start();
            return true;
        } else {
            return false;
        }

    }

    private boolean swipeLeft() {
        for (int i = 0; i < colAmmount - 1; i--) {
            // Ignore the left column as it will never move when swiping left
            for (int j = 0; j < rowAmmount; j++) {

            }
        }
        return true;
    }

    private boolean swipeUp() {
        Log.i("Direction", "up");
        return true;
    }

    private boolean swipeDown() {
        Log.i("Direction", "down");
        return true;
    }

}
