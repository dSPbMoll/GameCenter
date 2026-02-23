package com.example.gamecenter._2048;

import java.util.List;

public class SwipeResultsDto {

    public final List<GridBoxDto> initialPositionLLList;
    public final List<GridBoxDto> finalPositionLLList;
    public final List<AnimatorDto> animatorDtoList;
    public final boolean moved;

    public SwipeResultsDto(List<GridBoxDto> initialPositionLLList,
                           List<GridBoxDto> finalPositionLLList,
                           List<AnimatorDto> animatorDtoList,
                           boolean moved) {

        this.initialPositionLLList = initialPositionLLList;
        this.finalPositionLLList = finalPositionLLList;
        this.animatorDtoList = animatorDtoList;
        this.moved = moved;
    }

}
