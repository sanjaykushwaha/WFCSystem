package com.sanjay.wfcsystem;

/**
 * Callback to get notified when distance changes.
 */

public interface DistanceListener {

    /**
     * @param distanceInCm Distance of the object from ranging module in centimeter.
     */
    void onDistanceChange(double distanceInCm);
}
