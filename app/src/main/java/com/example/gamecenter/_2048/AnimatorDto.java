package com.example.gamecenter._2048;

public class AnimatorDto {

    public final String animationType;
    public final float startPosInQuadrant;
    public final float endPosInQuadrant;

    public AnimatorDto(String animationType, float startPosInQuadrant, float endPosInQuadrant) {
        this.animationType = animationType;
        this.startPosInQuadrant = startPosInQuadrant;
        this.endPosInQuadrant = endPosInQuadrant;
    }

}
