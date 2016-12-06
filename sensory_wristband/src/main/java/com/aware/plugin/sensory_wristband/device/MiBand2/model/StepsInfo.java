package com.aware.plugin.sensory_wristband.device.MiBand2.model;

import java.io.Serializable;

/**
 * Thanks to fcanducci.
 */
public class StepsInfo implements Serializable {

    private int steps;
    private int distance;
    private int calories;

    public StepsInfo() {

    }

    public StepsInfo(int steps, int distance, int calories) {
        this.steps = steps;
        this.distance = distance;
        this.calories = calories;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    @Override
    public String toString() {
        return "StepsInfo=[steps=" + getSteps() + "; distance=" + getDistance() + "; calories=" + getCalories() + "]";
    }

    /**
     * Get StepsInfo from byte data.
     * Length of given data should be equal to 12 bytes (+1 for control data??) = 13 bytes.
     * @param data - bytes from which create StepsInfo
     * @return StepsInfo
     */
    public static StepsInfo fromByteData(byte[] data){
        if (data.length < 13){
            return null;
        }
        StepsInfo stepsInfo = new StepsInfo();
        stepsInfo.steps = ((data[1] & 255) | ((data[2] & 255) << 8));
        stepsInfo.distance = ((((data[5] & 255) | ((data[6] & 255) << 8)) | (data[7] & 16711680)) | ((data[8] & 255) << 24));
        stepsInfo.calories = ((((data[9] & 255) | ((data[10] & 255) << 8)) | (data[11] & 16711680)) | ((data[12] & 255) << 24));
        return stepsInfo;
    }
}
