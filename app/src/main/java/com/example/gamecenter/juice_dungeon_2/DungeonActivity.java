package com.example.gamecenter.juice_dungeon_2;

import static com.example.gamecenter.juice_dungeon_2.BattleMove.STRIKE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.gamecenter.GameSelectorActivity;
import com.example.gamecenter.R;
import com.example.gamecenter.database.GameCenterOpenHelper;
import com.example.gamecenter.session.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DungeonActivity extends AppCompatActivity implements Runnable {
    private Thread thread;

    private DungeonEntity[] team;
    private ImageView[] teamImages;
    private TextView[] teamHpTVs;
    private HashMap<CharacterType, ArrayList<BattleMove>> defaultMoveSets;
    private LinearLayout teamMovesLL;
    private DungeonEntity enemy;
    private TextView enemyHpTV;
    private ImageView enemyImage;

    private int floor = 1;
    private TextView floorNumberTV;
    private boolean teamHasLost = false;
    private int numOfDungeonEntityWithShift = 1;
    private BattleMove chosenMove;
    private Integer numOfChosenTarget;
    private ArrayList<BattleMove> allyTargetMoves;
    private ArrayList<BattleMove> enemyTargetMoves;
    private ArrayList<Integer> defendedTeamMembers = new ArrayList<>();

    private volatile boolean isTransitioning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jd2_activity_dungeon);

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());

        this.defaultMoveSets = initDefaultMoveSets();
        this.allyTargetMoves = initAllyTargetMoves();
        this.enemyTargetMoves = initEnemyTargetMoves();

        ArrayList<CharacterType> receivedTeam = (ArrayList<CharacterType>) getIntent().getSerializableExtra("SELECTED_TEAM");
        this.team = buildTeam(receivedTeam);
        this.enemy = initEnemy();

        this.teamImages = buildTeamImages();
        this.teamHpTVs = buildTeamHpTVs();
        this.teamMovesLL = findViewById(R.id.teamMovesLL);
        this.enemyHpTV = initEnemyHpTv(); // Ya no dará 0/0 porque 'enemy' ya existe
        this.enemyImage = initEnemyImage();
        this.floorNumberTV = findViewById(R.id.floorNumberTV);

        updateTeamHpTvs();
        updateDungeonEntityHpTv(-1);
        updateFloorLL();
        updateTeamMovesLL();

        this.thread = new Thread(this);
        thread.start();
    }
    private HashMap<CharacterType, ArrayList<BattleMove>> initDefaultMoveSets() {
        HashMap<CharacterType, ArrayList<BattleMove>> defaultMoveSets = new HashMap<>();
        defaultMoveSets.put(CharacterType.MACIA, new ArrayList<>());
        defaultMoveSets.put(CharacterType.WIZARD, new ArrayList<>());
        defaultMoveSets.put(CharacterType.KNIGHT, new ArrayList<>());
        defaultMoveSets.put(CharacterType.GOBLIN, new ArrayList<>());

        //MACIA
        defaultMoveSets.get(CharacterType.MACIA).add(BattleMove.TEACH);

        //WIZARD
        defaultMoveSets.get(CharacterType.WIZARD).add(BattleMove.FIREBALL);
        defaultMoveSets.get(CharacterType.WIZARD).add(BattleMove.UNI_HEAL);
        defaultMoveSets.get(CharacterType.WIZARD).add(BattleMove.MULTI_HEAL);

        //KNIGHT
        defaultMoveSets.get(CharacterType.KNIGHT).add(STRIKE);
        defaultMoveSets.get(CharacterType.KNIGHT).add(BattleMove.DEFEND);

        //GOBLIN
        defaultMoveSets.get(CharacterType.GOBLIN).add(STRIKE);
        defaultMoveSets.get(CharacterType.GOBLIN).add(BattleMove.UNI_HEAL);

        return defaultMoveSets;
    }
    private ArrayList<BattleMove> initAllyTargetMoves() {
        ArrayList<BattleMove> allyTargetMoves = new ArrayList<>();

        allyTargetMoves.add(BattleMove.UNI_HEAL);
        allyTargetMoves.add(BattleMove.MULTI_HEAL);
        allyTargetMoves.add(BattleMove.TEACH);
        allyTargetMoves.add(BattleMove.DEFEND);

        return allyTargetMoves;
    }
    private ArrayList<BattleMove> initEnemyTargetMoves() {
        ArrayList<BattleMove> enemyTargetMoves = new ArrayList<>();

        enemyTargetMoves.add(STRIKE);
        enemyTargetMoves.add(BattleMove.FIREBALL);

        return enemyTargetMoves;
    }
    private DungeonEntity[] buildTeam(ArrayList<CharacterType> rawTeam) {
        int counter = 0;
        DungeonEntity[] team = new DungeonEntity[rawTeam.size()];

        for (CharacterType character : rawTeam) {
            DungeonEntity member = null;
            ArrayList<BattleMove> moveSet = defaultMoveSets.get(character);

            switch (character) {
                case MACIA:
                    member = new DungeonEntity(counter +1, CharacterType.MACIA, moveSet, 1.2);
                    break;
                case WIZARD:
                    member = new DungeonEntity(counter +1, CharacterType.WIZARD, moveSet, 1.5);
                    break;
                case KNIGHT:
                    member = new DungeonEntity(counter +1, CharacterType.KNIGHT, moveSet, 2.2);
                    break;
            }

            team[counter] = member;
            counter++;
        }

        return team;
    }
    private ImageView[] buildTeamImages() {
        int[] imageIds = {
                R.id.team1Image, R.id.team2Image, R.id.team3Image,
                R.id.team4Image, R.id.team5Image, R.id.team6Image
        };

        ImageView[] images = new ImageView[team.length];

        for (int i = 0; i < team.length; i++) {
            images[i] = findViewById(imageIds[i]);
            int resId = getDrawableByCharacterType(team[i].getType());
            images[i].setImageResource(resId);

            final int targetIndex = i +1;

            images[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    numOfChosenTarget = targetIndex;
                }
            });
        }
        return images;
    }
    private int getDrawableByCharacterType(CharacterType type) {
        switch (type) {
            case MACIA: return R.drawable.jd2_all_macia;
            case WIZARD: return R.drawable.jd2_all_wizard;
            case KNIGHT: return R.drawable.jd2_all_knight;
            default: return R.drawable.jd2_all_macia;
        }
    }
    private TextView[] buildTeamHpTVs() {
        int[] HpTvIds = {
                R.id.team1HP, R.id.team2HP, R.id.team3HP,
                R.id.team4HP, R.id.team5HP, R.id.team6HP
        };

        TextView[] hpTvs = new TextView[team.length];

        for (int i=0; i < team.length; i++) {
            hpTvs[i] = findViewById(HpTvIds[i]);
        }

        return hpTvs;
    }
    private void updateTeamHpTvs() {
        for (int i=0; i < team.length; i++) {
            updateDungeonEntityHpTv(i);
        }
    }
    private void updateDungeonEntityHpTv(int entityNum) {
        runOnUiThread(() -> {
            if (entityNum >= 0) { // Ojo aquí, el índice 0 también es parte del equipo
                teamHpTVs[entityNum].setText(team[entityNum].getHp() + "/" + team[entityNum].getMaxHp());
            } else {
                enemyHpTV.setText(enemy.getHp() + "/" + enemy.getMaxHp());
            }
        });
    }
    private DungeonEntity initEnemy() {
        int basePower;

        if (floor < 10) {
            basePower = 10 * floor;
        } else {
            basePower = 20 * floor;
        }
        return new DungeonEntity(-1, CharacterType.GOBLIN,
                defaultMoveSets.get(CharacterType.GOBLIN), 10, basePower);
    }
    private TextView initEnemyHpTv() {
        TextView enemyHpTV = findViewById(R.id.enemyHP);
        enemyHpTV.setText(enemy.getHp() + "/" + enemy.getMaxHp());
        return enemyHpTV;
    }
    private ImageView initEnemyImage() {
        ImageView enemyImage = findViewById(R.id.enemyImage);
        enemyImage.setImageResource(R.drawable.jd2_all_goblin);

        enemyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numOfChosenTarget = -1;
            }
        });

        return enemyImage;
    }
    private void updateFloorLL() {
        runOnUiThread(() -> {
            this.floorNumberTV.setText(String.valueOf(floor));
        });
    }
    private void updateTeamMovesLL() {
        runOnUiThread(() -> {
            this.teamMovesLL.removeAllViews();
            if (numOfDungeonEntityWithShift < 0) return;

            for (BattleMove move : team[numOfDungeonEntityWithShift -1].getMoveSet()) {
                this.teamMovesLL.addView(generateMoveImageView(move));
            }
        });
    }
    private ImageView generateMoveImageView(BattleMove move) {
        ImageView moveView = new ImageView(this);

        // Width & Height
        int sizeInPx = (int) (70 * getResources().getDisplayMetrics().density);
        int marginInPx = (int) (5 * getResources().getDisplayMetrics().density);

        // Margins
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
        params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);

        moveView.setLayoutParams(params);

        moveView.setImageResource(getBattleMoveDrawable(move));
        moveView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        moveView.setTag(move);

        moveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenMove = (BattleMove) v.getTag();
            }
        });

        return moveView;
    }
    private Integer getBattleMoveDrawable(BattleMove move) {
        switch (move) {
            case STRIKE:
                return R.drawable.jd2_dungeon_ic_strike;
            case DEFEND:
                return R.drawable.jd2_dungeon_ic_defend;
            case FIREBALL:
                return R.drawable.jd2_dungeon_ic_fireball;
            case UNI_HEAL:
                return R.drawable.jd2_dungeon_ic_uniheal;
            case MULTI_HEAL:
                return R.drawable.jd2_dungeon_ic_multiheal;
            case TEACH:
                return R.drawable.jd2_dungeon_ic_teach;
        }
        return null;
    }

    // ========================== GAME LOGIC ===============================
    public void run() {
        while (!teamHasLost) {
            executeGameLogic();
            try {
                Thread.sleep(100);
            } catch (Exception ignored) {}
        }
        exitDungeon();
    }

    private void executeGameLogic() {
        if (isTransitioning) return;

        // Si el equipo ha perdido, salimos
        teamHasLost = checkTeamLost();
        if (teamHasLost) return;

        // ---------------- ENEMY TURN ----------------
        if (numOfDungeonEntityWithShift < 0) {
            boolean enemyDidMove = enemyShift();  // ahora devuelve boolean
            // Aunque no haga nada (por mala suerte), pasamos turno igualmente para no bloquear
            numOfDungeonEntityWithShift = 1;
            updateTeamMovesLL();
            chosenMove = null;
            numOfChosenTarget = null;
            defendedTeamMembers.clear();
            return;
        }

        // ---------------- PLAYER TURN ----------------
        // Si el jugador actual está muerto, saltamos automáticamente al siguiente turno vivo
        if (!team[numOfDungeonEntityWithShift - 1].getIsAlive()) {
            passToNextShiftSkippingDead();
            return;
        }

        // Ensure chosenMove is always one of the current attacker's moves
        if (chosenMove == null) {
            chosenMove = team[numOfDungeonEntityWithShift - 1].getMoveSet().get(0);
        }

        // Esperamos target del usuario
        if (numOfChosenTarget == null) return;

        boolean isValidMove;
        if ((numOfChosenTarget > 0 && allyTargetMoves.contains(chosenMove))
                || (numOfChosenTarget < 0 && enemyTargetMoves.contains(chosenMove))) {
            isValidMove = executeMove(numOfDungeonEntityWithShift, numOfChosenTarget, chosenMove);
        } else {
            numOfChosenTarget = null;
            return;
        }

        // Si matamos al enemigo, siguiente piso
        if (enemy.getHp() <= 0) {
            isTransitioning = true;
            numOfChosenTarget = null;
            chosenMove = null;
            passToNextFloor();
            return;
        }

        // Si el movimiento fue válido, pasamos turno
        if (isValidMove) {
            passToNextShiftSkippingDead();
        } else {
            // Movimiento inválido (target muerto, etc). Reseteamos selección para que el usuario elija otra cosa.
            chosenMove = null;
            numOfChosenTarget = null;
        }

        teamHasLost = checkTeamLost();
    }
    private void passToNextShiftSkippingDead() {
        // Avanza al siguiente aliado vivo. Si no hay, pasa a turno enemigo.
        int tries = 0;

        while (tries < team.length) {
            if (numOfDungeonEntityWithShift > 0 && numOfDungeonEntityWithShift < team.length) {
                numOfDungeonEntityWithShift++;
            } else if (numOfDungeonEntityWithShift == team.length) {
                numOfDungeonEntityWithShift = -1; // turno enemigo
                updateTeamMovesLL();
                chosenMove = null;
                numOfChosenTarget = null;
                defendedTeamMembers.clear();
                return;
            }

            // Si el siguiente está vivo, paramos
            if (numOfDungeonEntityWithShift > 0 && team[numOfDungeonEntityWithShift - 1].getIsAlive()) {
                updateTeamMovesLL();
                chosenMove = null;
                numOfChosenTarget = null;
                defendedTeamMembers.clear();
                return;
            }

            tries++;
        }

        // Si llega aquí, no hay nadie vivo
        teamHasLost = true;
    }
    private boolean executeMove(int attackerNum, int targetNum, BattleMove move) {
        DungeonEntity attacker;
        if (attackerNum >= 0) {
            attacker = team[attackerNum -1];
        } else {
            attacker = enemy;
        }

        DungeonEntity target;
        if (targetNum >= 0) {
            target = team[targetNum -1];
        } else {
            target = enemy;
        }

        if (!target.getIsAlive() || !attacker.getIsAlive()) return false;

        double efectivityFactor;
        if (defendedTeamMembers.contains(targetNum)) efectivityFactor = 0.2;
        else efectivityFactor = 1;

        // FIX: Convertimos targetNum (1 al 6) a índice de Array (0 al 5) para la interfaz
        int uiTargetIndex = (targetNum >= 0) ? (targetNum - 1) : -1;

        switch (move) {
            case STRIKE:
                target.damage(((int) (attacker.getPower() * efectivityFactor)));
                updateDungeonEntityHpTv(uiTargetIndex);
                break;
            case FIREBALL:
                target.damage(((int) (attacker.getPower() * 2 * efectivityFactor)));
                updateDungeonEntityHpTv(uiTargetIndex);
                break;
            case UNI_HEAL:
                target.heal(attacker.getPower());
                updateDungeonEntityHpTv(uiTargetIndex);
                break;
            case MULTI_HEAL:
                for (int i=0; i < team.length; i++) {
                    target = team[i];
                    target.heal((int) (attacker.getPower() * 0.5));
                    updateDungeonEntityHpTv(i);
                }
                break;
            case DEFEND:
                defendedTeamMembers.add(targetNum);
                break;
            case TEACH:
                target.growPower(1 + ((int) (Math.floor(attacker.getPower() * 0.1))));
                updateDungeonEntityHpTv(uiTargetIndex);
                break;
        }
        return true;
    }
    private void passToNextShift() {
        if (numOfDungeonEntityWithShift > 0 && numOfDungeonEntityWithShift < team.length) {
            numOfDungeonEntityWithShift++;

        } else if (numOfDungeonEntityWithShift == team.length) {
            numOfDungeonEntityWithShift = -1;
            enemyShift();
            numOfDungeonEntityWithShift = 1;
        }
        updateTeamMovesLL();
        chosenMove = null;
        numOfChosenTarget = null;
        defendedTeamMembers.clear();
    }
    private boolean checkTeamLost() {
        for (DungeonEntity member : team) {
            if (member.getIsAlive()) return false;
        }
        return true;
    }
    private boolean enemyShift() {
        if (enemy == null || !enemy.getIsAlive()) return false;

        // Elegir un movimiento aleatorio
        int moveNum = (int) (Math.random() * defaultMoveSets.get(CharacterType.GOBLIN).size());
        BattleMove move = defaultMoveSets.get(CharacterType.GOBLIN).get(moveNum);

        Integer target;

        if (allyTargetMoves.contains(move)) {
            // El goblin solo tiene UNI_HEAL como allyTargetMove, así que se curará a sí mismo
            target = -1;
            return executeMove(-1, target, move);
        }

        if (enemyTargetMoves.contains(move)) {
            // Elegimos un aliado vivo aleatorio
            ArrayList<Integer> aliveTargets = new ArrayList<>();
            for (int i = 0; i < team.length; i++) {
                if (team[i].getIsAlive()) aliveTargets.add(i + 1); // targets van 1..N
            }

            if (aliveTargets.isEmpty()) {
                teamHasLost = true;
                return false;
            }

            int idx = (int) (Math.random() * aliveTargets.size());
            target = aliveTargets.get(idx);

            return executeMove(-1, target, move);
        }

        return false;
    }
    private void passToNextFloor() {
        runOnUiThread(() -> {
            this.floor++;
            View dungeonScene = findViewById(R.id.blackOverlay);

            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_game);
            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_game);

            dungeonScene.startAnimation(fadeIn);

            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    enemy = initEnemy();
                    updateDungeonEntityHpTv(-1);
                    numOfDungeonEntityWithShift = 1;
                    updateFloorLL();
                    updateTeamMovesLL();

                    dungeonScene.startAnimation(fadeOut);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    isTransitioning = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        });
    }
    private void exitDungeon() {
        saveJuiceDungeon2ScoreToDbIfLoggedIn();

        Intent intent = new Intent(DungeonActivity.this, TavernActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveJuiceDungeon2ScoreToDbIfLoggedIn() {
        int userId = SessionManager.getUserId(this);
        if (userId == -1) return;

        if (floor <= 0) return;

        String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        GameCenterOpenHelper db = new GameCenterOpenHelper(this);
        db.insertScore(userId, datetime, floor, "JUICE_DUNGEON_2");
    }

    public void goBackHome(View view) {
        saveJuiceDungeon2ScoreToDbIfLoggedIn();
        Intent intent = new Intent(this, GameSelectorActivity.class);
        startActivity(intent);
        finish();
    }
}
