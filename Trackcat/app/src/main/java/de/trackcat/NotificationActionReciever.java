package de.trackcat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        /* Receives Message when pause Button in Notification is clicked */
        String msg = intent.getAction();
        if (msg != null) {
            if (msg.equalsIgnoreCase("ACTION_PAUSE")) {
                MainActivity.getInstance().stopTracking();
            } else if (msg.equalsIgnoreCase("ACTION_PLAY")) {
                MainActivity.getInstance().startTracking();
            }
        }
    }
}
