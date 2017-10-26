package com.sanjay.wfcsystem;

/**
 * Created by sanjay on 22/10/17.
 */

public class WFCSystem {
    private boolean pumpOn;
    private boolean autoOn;
    private int waterLevel;

    public WFCSystem() {
    }

    public WFCSystem(boolean pumpOn, boolean autoOn, int waterLevel) {
        this.pumpOn = pumpOn;
        this.autoOn = autoOn;
        this.waterLevel = waterLevel;
    }

    public boolean isPumpOn() {
        return pumpOn;
    }

    public void setPumpOn(boolean pumpOn) {
        this.pumpOn = pumpOn;
    }

    public boolean isAutoOn() {
        return autoOn;
    }

    public void setAutoOn(boolean autoOn) {
        this.autoOn = autoOn;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }
}
