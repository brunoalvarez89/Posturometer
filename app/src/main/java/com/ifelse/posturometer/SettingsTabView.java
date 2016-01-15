package com.ifelse.posturometer;

public enum SettingsTabView {

    SEEKBAR_FRONT_ANGLE_THRESHOLD(1),
    SEEKBAR_FRONT_ANGLE_VIBRATION(2),
    SEEKBAR_LATERAL_ANGLE_THRESHOLD(3),
    SEEKBAR_LATERAL_ANGLE_VIBRATION(4),
    SEEKBAR_DELAY(5);

    private final int value;

    private SettingsTabView(int value){
        this.value=value;
    }

    public int getValue(){return value;}

    public static SettingsTabView values(int what) {
        switch(what){
            case 1:
                return SEEKBAR_FRONT_ANGLE_THRESHOLD;

            case 2:
                return SEEKBAR_FRONT_ANGLE_VIBRATION;

            case 3:
                return SEEKBAR_LATERAL_ANGLE_THRESHOLD;

            case 4:
                return SEEKBAR_LATERAL_ANGLE_VIBRATION;

            case 5:
                return SEEKBAR_DELAY;

            default:
                return null;
        }
    }
}

