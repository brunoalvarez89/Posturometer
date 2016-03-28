package com.genomicalsoftware.posturometer.ui;

public enum SettingsTabView {

    SEEKBAR_FRONT_ANGLE_THRESHOLD(1),
    SEEKBAR_FRONT_VIBRATION(2),
    SEEKBAR_LATERAL_ANGLE_THRESHOLD(3),
    SEEKBAR_LATERAL_VIBRATION(4),
    SEEKBAR_DELAY(5),
    TEXTVIEW_FRONT_ANGLE_THRESHOLD(6),
    TEXTVIEW_FRONT_VIBRATION(7),
    TEXTVIEW_LATERAL_ANGLE_THRESHOLD(8),
    TEXTVIEW_LATERAL_VIBRATION(9),
    TEXTVIEW_DELAY(10);

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
                return SEEKBAR_FRONT_VIBRATION;

            case 3:
                return SEEKBAR_LATERAL_ANGLE_THRESHOLD;

            case 4:
                return SEEKBAR_LATERAL_VIBRATION;

            case 5:
                return SEEKBAR_DELAY;

            case 6:
                return TEXTVIEW_FRONT_ANGLE_THRESHOLD;

            case 7:
                return TEXTVIEW_FRONT_VIBRATION;

            case 8:
                return TEXTVIEW_LATERAL_ANGLE_THRESHOLD;

            case 9:
                return TEXTVIEW_LATERAL_VIBRATION;

            case 10:
                return TEXTVIEW_DELAY;

            default:
                return null;
        }
    }
}

