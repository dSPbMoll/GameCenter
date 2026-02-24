package com.example.gamecenter.juice_dungeon_2;

import java.util.ArrayList;

public class DungeonEntity {
    private final int teamNumber;
    private final CharacterType type;
    private int maxHp;
    private final double MAX_HP_GROW_RATE;
    private int hp;
    private int power;
    private boolean isAlive;

    private final ArrayList<BattleMove> moveSet;

    public DungeonEntity(int teamNumber, CharacterType character, ArrayList<BattleMove> moveSet,
                         double maxHpGrowRate) {
        this.teamNumber = teamNumber;
        this.type = character;
        this.moveSet = moveSet;
        this.MAX_HP_GROW_RATE = maxHpGrowRate;

        this.power = 10;
        this.isAlive = true;
        recalculateStats();
    }

    public DungeonEntity(int teamNumber, CharacterType character, ArrayList<BattleMove> moveSet,
                         double maxHpGrowRate, int basePower) {
        this.teamNumber = teamNumber;
        this.type = character;
        this.moveSet = moveSet;
        this.MAX_HP_GROW_RATE = maxHpGrowRate;
        this.power = basePower;

        this.isAlive = true;
        recalculateStats();
    }

    //======================= GETTERS & SETTERS =======================
    public CharacterType getType() {
        return type;
    }
    public int getMaxHp() {
        return maxHp;
    }
    public int getHp() {
        return hp;
    }
    public void heal(int ammountHealed) {
        this.hp = this.hp + ammountHealed;
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }
    }
    public void damage(int ammountDamaged) {
        this.hp = this.hp - ammountDamaged;
        if (this.hp < 0) {
            this.hp = 0;
            this.isAlive = false;
        }
    }
    public int getPower() {
        return this.power;
    }
    public void setPower(int power) {
        this.power = power;
    }
    public ArrayList<BattleMove> getMoveSet() {
        return moveSet;
    }
    public boolean getIsAlive() {
        return isAlive;
    }

    // ========================= OTHER ==========================
    public void growPower(int growAmmount) {
        this.power = this.power + growAmmount;
        recalculateStats();
    }

    private void recalculateStats() {
        // MaxHp
        int oldMaxHp = this.maxHp;
        this.maxHp = (int) Math.round(this.power * this.MAX_HP_GROW_RATE);
        int maxHpDiff = maxHp - oldMaxHp;

        // Hp
        heal(maxHpDiff);
    }
}
