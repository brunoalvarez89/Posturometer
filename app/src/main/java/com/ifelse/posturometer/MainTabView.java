package com.ifelse.posturometer;

public enum MainTabView {

    ERROR(-1),
    TEXTVIEW_FRONT_ANGLE(1),
    TEXTVIEW_RIGHT_ANGLE(2),
    TEXTVIEW_LEFT_ANGLE(3),
    BUTTON_START(4),
    BUTTON_STOP(5);

    private final int value;

    private MainTabView(int value){
        this.value=value;
    }

    public int getValue(){return value;}

    public static MainTabView values(int what) {
        switch(what){
            case 1:
                return TEXTVIEW_FRONT_ANGLE;

            case 2:
                return TEXTVIEW_RIGHT_ANGLE;

            case 3:
                return TEXTVIEW_LEFT_ANGLE;

            case 4:
                return BUTTON_START;

            case 5:
                return BUTTON_STOP;

            default:
                return ERROR;
        }
    }
}

