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

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.content.Context;
import android.widget.TextView;
import java.util.List;

/* Handle all battery change events */
public class SensorInfo {
    private SensorManager sm = null;
    private SensorView sv = null;

    private class SensorView {
        private TextView tv = null;

        /* Show the sensor informations on screen */
        public void showStat(StringBuilder str) {
            tv.setText(str);
        }

        public SensorView(TextView _tv) {
            tv = _tv;
        }
    }

    public SensorInfo(SensorManager sm, TextView tv) {
        sv = new SensorView(tv);

        StringBuilder builder = new StringBuilder("Sensors consumption\n");
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : sensors) {
            builder.append("  " + s.getName() + ": " + s.getPower() + "mA\n");
        }
        sv.showStat(builder);
    }
}
