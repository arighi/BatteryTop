/*
 *  BatteryTop: show detailed battery informations in a top-like format
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

import android.app.Activity;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;
import java.util.Set;
import java.text.SimpleDateFormat;

public class BatteryTop extends Activity
{
    private BatteryInfo bi = null;

    /* Handle all battery change events */
    private class BatteryInfo extends BroadcastReceiver {
        private BatteryView bv = null;

        private float voltageSpeed = 0;
        private long startVoltage = 0;
        private long startTime = 0;

        private class BatteryView {
            private TextView tv = null;

            /* Show battery information on screen */
            public void showStat(StringBuilder str) {
                tv.setText(str);
            }

            public BatteryView() {
                tv = (TextView)findViewById(R.id.text);
            }
        }

        public void onReceive(Context context, Intent intent) {
            StringBuilder builder = new StringBuilder("Battery (raw data)\n");

            /* Show raw informations */
            Set<String> keys = intent.getExtras().keySet();
            for (String key : keys) {
                builder.append("  " + key + " " +
                               intent.getIntExtra(key, 0) + "\n");
            }
            builder.append("\nBattery (additional statistics)\n");

            /* Update voltage level */
            long voltage = intent.getIntExtra("voltage", 0);
            long time = System.currentTimeMillis();
            int plugged = intent.getIntExtra("plugged", -1);

            /* Evaluate voltage speed */
            if ((startVoltage == 0) && (startTime == 0)) {
                startVoltage = voltage;
                startTime = time;
            }
            if (((voltage - startVoltage) != 0) && ((time - startTime) != 0)) {
                voltageSpeed = (float)((voltage - startVoltage) * 1000) /
                                (time - startTime);
            }
            builder.append("  voltage/s " + voltageSpeed + "\n");

            /* Evaluate discharge time if unplugged */
            if (plugged == 0) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String startTimeString = formatter.format(startTime);

                builder.append("  start time: " + startTimeString + "\n");
                builder.append("  start voltage: " + startVoltage + "\n");

                if (voltageSpeed < 0) {
                    float seconds = voltage / -voltageSpeed;
                    builder.append("  depletion time: " +
                                   ((int)seconds / 3600)          + "hours, " +
                                   (((int)seconds % 3600) / 60)   + "min, " +
                                   ((int)seconds % 60)            + "sec\n");
                } else {
                    builder.append("  depletion time: unknown\n");
                }
            }

            bv.showStat(builder);
        }

        public BatteryInfo() {
            bv = new BatteryView();

            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(this, mIntentFilter);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        bi = new BatteryInfo();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onStop() {
        super.onStop();
    }
}
