package com.ifelse.posturometer.ui;

public enum SeekBarType {

    ERROR(-1),
    SEEKBAR_FRONT_ANGLE(1),
    SEEKBAR_FRONT_VIBRATION(2),
    SEEKBAR_LATERAL_ANGLE(3),
    SEEKBAR_LATERAL_VIBRATION(4),
    SEEKBAR_DELAY(5);

    private final int value;

    private SeekBarType(int value){
        this.value=value;
    }

    public int getValue(){return value;}

    public static SeekBarType values(int what) {
        switch(what){
            case 1:
                return SEEKBAR_FRONT_ANGLE;

            case 2:
                return SEEKBAR_FRONT_VIBRATION;

            case 3:
                return SEEKBAR_LATERAL_ANGLE;

            case 4:
                return SEEKBAR_LATERAL_VIBRATION;

            case 5:
                return SEEKBAR_DELAY;

            default:
                return ERROR;
        }
    }
}

