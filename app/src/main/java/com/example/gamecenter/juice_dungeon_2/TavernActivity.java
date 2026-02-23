package com.example.gamecenter.juice_dungeon_2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.gamecenter.R;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;

public class TavernActivity extends AppCompatActivity {
    private FrameLayout tavernScreen;
    private FlexboxLayout characterSpawningArea;
    private LinearLayout teamLL;
    private LinearLayout.LayoutParams teamLLChildParams;
    private ArrayList<CharacterType> team;
    private final int maxTeamSize = 6;
    private int width;
    private int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.jd2_activity_tavern);

        // Configuración de pantalla completa
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.systemBars());

        this.tavernScreen = findViewById(R.id.tavern);
        this.teamLL = findViewById(R.id.teamLL);

        // IMPORTANTE: Llamar a init para inicializar tus variables antes de usarlas
        init(this);

        // ESCUCHADOR DE TAMAÑO
        tavernScreen.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Security verification
                if (isFinishing() || isDestroyed()) return;

                width = tavernScreen.getWidth();
                height = tavernScreen.getHeight();

                if (width > 0 && height > 0) {
                    tavernScreen.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    characterSpawningArea.removeAllViews();
                    updateCharacterSpawningArea(width, height);

                    addCharacterToSpawningArea(CharacterType.MACIA);
                    addCharacterToSpawningArea(CharacterType.WIZARD);
                    addCharacterToSpawningArea(CharacterType.KNIGHT);
                }
            }
        });

        findViewById(R.id.enterDungeonBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterDungeon(v);
            }
        });
    }

    private void init(Context context) {
        this.characterSpawningArea = new FlexboxLayout(context);
        this.teamLLChildParams = initTeamLLChildParams();
        this.team = new ArrayList<>();

        // CONFIGURACIÓN MÁGICA
        this.characterSpawningArea.setFlexDirection(FlexDirection.ROW);
        this.characterSpawningArea.setFlexWrap(FlexWrap.WRAP);
        this.characterSpawningArea.setJustifyContent(JustifyContent.FLEX_START);

        this.tavernScreen.addView(characterSpawningArea);
    }

    private LinearLayout.LayoutParams initTeamLLChildParams() {
        int sizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                70,
                getResources().getDisplayMetrics()
        );
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
        params.setMargins(8, 8, 8, 8);
        return params;
    }

    private void updateCharacterSpawningArea(int currentWidth, int currentHeight) {
        int posX = (int) Math.round(currentWidth / 18.5 * 8);
        int posY = (int) Math.round(currentHeight / 10.5 * 4);
        int sizeX = currentWidth - posX;
        int sizeY = currentHeight - posY;

        if (sizeX < 0) sizeX = 0;
        if (sizeY < 0) sizeY = 0;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizeX, sizeY);
        params.leftMargin = posX;
        params.topMargin = posY;
        characterSpawningArea.setLayoutParams(params);
    }

    private void addCharacterToSpawningArea(CharacterType character) {
        Integer imageResId = getImageResOfCharacter(character);
        if (imageResId == null) return;

        if (imageResId != 0) {
            ImageView characterImg = new ImageView(this);
            characterImg.setImageResource(imageResId);
            characterImg.setId(team.size());
            characterImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addCharacterToTeamLL(character, v.getId());
                }
            });

            //int charH = (int) Math.round(height / 10.5 * 2);
            int charH = (int) Math.round(height /4);

            // IMPORTANTE: Usar FlexboxLayout.LayoutParams
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    charH
            );
            // Márgenes pequeños para que no se peguen
            params.setMargins(5, 5, 5, 5);

            characterImg.setLayoutParams(params);
            characterImg.setAdjustViewBounds(true); // Ajustar borde a la imagen
            characterImg.setScaleType(ImageView.ScaleType.FIT_CENTER);

            characterSpawningArea.addView(characterImg);
        }
    }

    private void addCharacterToTeamLL(CharacterType character, int characterPosition) {
        if (team.size() < maxTeamSize) {
            team.add(character);

            ImageView imageView = new ImageView(this);
            int charRes = getImageResOfCharacterPicture(character);
            imageView.setImageResource(charRes);
            imageView.setLayoutParams(teamLLChildParams);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.remove(character);
                    teamLL.removeView(v);
                }
            });

            teamLL.addView(imageView);
        }
    }

    private Integer getImageResOfCharacter(CharacterType character) {
        switch (character) {
            case MACIA:
                return R.drawable.jd2_all_macia;
            case WIZARD:
                return R.drawable.jd2_all_wizard;
            case KNIGHT:
                return R.drawable.jd2_all_knight;
        }
        return null;
    }

    private Integer getImageResOfCharacterPicture(CharacterType character) {
        switch (character) {
            case MACIA:
                return R.drawable.jd2_tavern_picture_macia;
            case WIZARD:
                return R.drawable.jd2_tavern_picture_wizard;
            case KNIGHT:
                return R.drawable.jd2_tavern_picture_knight;
        }
        return null;
    }

    public void enterDungeon(View view) {
        if (team == null || team.isEmpty()) {
            return;
        }

        Intent intent = new Intent(TavernActivity.this, DungeonActivity.class);
        intent.putExtra("SELECTED_TEAM", team);

        startActivity(intent);
        finish();
    }
}