package com.aware.plugin.sensory_wristband.device.MiBand2.model;

import com.aware.plugin.sensory_wristband.device.StepsInfo;

public class StepsInfoMiBand2 implements StepsInfo {

    private int steps;
    private int distance;
    private int calories;

    public StepsInfoMiBand2() {

    }

    public StepsInfoMiBand2(int steps, int distance, int calories) {
        this.steps = steps;
        this.distance = distance;
        this.calories = calories;
    }

    @Override
    public int getSteps() {
        return steps;
    }

    @Override
    public int getDistance() {
        return distance;
    }

    @Override
    public int getCalories() {
        return calories;
    }

    @Override
    public String toString() {
        return "StepsInfoMiBand2=[steps=" + getSteps() + "; distance=" + getDistance() + "; calories=" + getCalories() + "]";
    }

    /**
     * Get StepsInfoMiBand2 from byte data.
     * Length of given data should be equal to 12 bytes (+1 for control data??) = 13 bytes.
     * @param data - bytes from which create StepsInfoMiBand2
     * @return StepsInfoMiBand2
     */
    public static StepsInfoMiBand2 fromByteData(byte[] data){
        if (data.length < 13){
            return null;
        }
        StepsInfoMiBand2 stepsInfo = new StepsInfoMiBand2();
        stepsInfo.steps = ((data[1] & 255) | ((data[2] & 255) << 8));
        stepsInfo.distance = ((((data[5] & 255) | ((data[6] & 255) << 8)) | (data[7] & 16711680)) | ((data[8] & 255) << 24));
        stepsInfo.calories = ((((data[9] & 255) | ((data[10] & 255) << 8)) | (data[11] & 16711680)) | ((data[12] & 255) << 24));
        return stepsInfo;
    }
}
