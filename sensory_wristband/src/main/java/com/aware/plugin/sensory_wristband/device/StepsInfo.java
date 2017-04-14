package com.aware.plugin.sensory_wristband.device;

import java.io.Serializable;

public interface StepsInfo extends Serializable {

    int getSteps();
    int getDistance();
    int getCalories();
    String toString();

    /**
     * Checks if StepsInfo is empty. Steps, distance, calories are equal to zero.
     * @return true if empty | false if one of parameters are different then zero
     */
    default boolean isStepInfoEmpty() {
        return getSteps() == 0 && getDistance() == 0 && getCalories() == 0;
    }

}
