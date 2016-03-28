package com.genomicalsoftware.posturometer;

public enum AngleType {

    ERROR(-1),
    FRONT_ANGLE(1),
    LATERAL_ANGLE(2),
    NONE(3);

    private final int value;

    private AngleType(int value){
        this.value=value;
    }

    public int getValue(){return value;}

    public static AngleType values(int what) {
        switch(what){
            case 1:
                return FRONT_ANGLE;

            case 2:
                return LATERAL_ANGLE;

            case 3:
                return NONE;

            default:
                return ERROR;
        }
    }
}

