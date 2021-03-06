package de.trackcat.Statistics;

import android.location.Location;

/* Class counts the travelled Distance, gets the current Locations */
public class mCounter {
    private float mAmount;
    private Location oldLocation;


    /* Add Distance between old and new Location to amount */
    public void addKm(Location newLocation) {
        if (oldLocation == null) {
            oldLocation = newLocation;
        } else {
            float newDisctance = oldLocation.distanceTo(newLocation);
            mAmount = mAmount + newDisctance;
            oldLocation = newLocation;
        }
    }

    /* Return amount of Distance in meter */
    public double getAmount() {
        return mAmount;
    }
}
