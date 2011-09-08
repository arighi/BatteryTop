/*
 *  BatteryInfo: retrieve detailed battery informations
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Copyright (C) 2011 Andrea Righi <andrea@betterlinux.com>
 */

package com.arighi.batterytop;

import android.os.BatteryManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;
import java.util.Set;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/* Handle all battery change events */
public class BatteryInfo extends BroadcastReceiver {
    /* Mapping from battery status codes to strings */
    private static final HashMap<Integer, String> batteryStatus = new HashMap<Integer, String>();
    static {
        batteryStatus.put(BatteryManager.BATTERY_STATUS_UNKNOWN,
                          "unknown");
        batteryStatus.put(BatteryManager.BATTERY_STATUS_CHARGING,
                          "charging");
        batteryStatus.put(BatteryManager.BATTERY_STATUS_DISCHARGING,
                          "draining");
        batteryStatus.put(BatteryManager.BATTERY_STATUS_NOT_CHARGING,
                          "not charging");
        batteryStatus.put(BatteryManager.BATTERY_STATUS_FULL,
                          "full");
    }

    private static final HashMap<Integer, String> batteryHealth = new HashMap<Integer, String>();
    static {
        batteryHealth.put(BatteryManager.BATTERY_HEALTH_UNKNOWN, "unknown");
        batteryHealth.put(BatteryManager.BATTERY_HEALTH_GOOD, "good");
        batteryHealth.put(BatteryManager.BATTERY_HEALTH_OVERHEAT, "overheat");
        batteryHealth.put(BatteryManager.BATTERY_HEALTH_DEAD, "dead");
        batteryHealth.put(BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE, "over_voltage");
        batteryHealth.put(BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE, "failed");
    }

    private static final HashMap<Integer, String> batteryPlugged = new HashMap<Integer, String>();
    static {
        batteryPlugged.put(0, "unplugged");
        batteryPlugged.put(BatteryManager.BATTERY_PLUGGED_AC, "AC");
        batteryPlugged.put(BatteryManager.BATTERY_PLUGGED_USB, "USB");
    }

    private BatteryView bv = null;

    private float levelSpeed = 0;
    private float startLevel = 0;
    private float lastLevel = 0;
    private long startTime = 0;

    private class BatteryView {
        private TextView tv = null;

        /* Show battery information on screen */
        public void showStat(StringBuilder str) {
            tv.setText(str);
        }

        public BatteryView(TextView _tv) {
            tv = _tv;
        }
    }

    private String getDescription(int val, HashMap<Integer, String> map) {
        String name = map.get(val);
        if (name != null)
            return name;
        return "<" + val + ">";
    }

    public void onReceive(Context context, Intent intent) {
        StringBuilder builder = new StringBuilder("Battery (raw data)\n");

        /* Show raw informations */
        Set<String> keys = intent.getExtras().keySet();
        for (String key : keys) {
            int value = intent.getIntExtra(key, 0);

            if (key.equals("icon-small")) {
                continue;
            } else if (key.equals("status")) {
                builder.append("  " + key + ": " +
                               getDescription(value, batteryStatus) + "\n");
            } else if (key.equals("health")) {
                builder.append("  " + key + ": " +
                               getDescription(value, batteryHealth) + "\n");
            } else if (key.equals("plugged")) {
                builder.append("  " + key + ": " +
                               getDescription(value, batteryPlugged) + "\n");
            } else if (key.equals("voltage")) {
                builder.append("  " + key + ": " +
                               value + "mV\n");
            } else if (key.equals("temperature")) {
                builder.append("  " + key + ": " +
                               (value / 10) + "." + (value % 10) + "°C\n");
            } else {
                builder.append("  " + key + ": " + value + "\n");
            }
        }
        builder.append("\nBattery (additional statistics)\n");

        /* Update voltage level */
        float level = (float)intent.getIntExtra("level", 0) /
                      (float)intent.getIntExtra("scale", 0);
        long time = System.currentTimeMillis();
        int plugged = intent.getIntExtra("plugged", -1);

        /* Evaluate voltage speed */
        if ((startLevel == 0) && (startTime == 0)) {
            startLevel = level;
            lastLevel = level;
            startTime = time;
        }
        if ((level != lastLevel) && (time != startTime)) {
            levelSpeed = (float)((level - startLevel) * 1000) /
                            (time - startTime);
            lastLevel = level;
        }
        builder.append("  charge/discharge speed: " +
                       (levelSpeed * 100) + "%/s\n");

        /* Evaluate discharge time if unplugged */
        if (plugged == 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTimeString = formatter.format(startTime);

            builder.append("  start time: " + startTimeString + "\n");
            builder.append("  start level: " + (startLevel * 100) + "%\n");

            if (levelSpeed < 0) {
                float seconds = level / -levelSpeed;
                builder.append("  time left: " +
                               ((int)seconds / 3600)          + "hours, " +
                               (((int)seconds % 3600) / 60)   + "min, " +
                               ((int)seconds % 60)            + "sec\n");
            } else {
                builder.append("  time left: evaluating...\n");
            }
        }

        bv.showStat(builder);
    }

    public BatteryInfo(TextView tv) {
        bv = new BatteryView(tv);
    }
}
