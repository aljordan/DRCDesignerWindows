/*
  Copyright 2011 Alan Brent Jordan
  This file is part of Digital Room Correction Designer.

  Digital Room Correction Designer is free software: you can redistribute
  it and/or modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, version 3 of the License.

  Digital Room Correction Designer is distributed in the hope that it will
  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
  Public License for more details.

  You should have received a copy of the GNU General Public License along with
  Digital Room Correction Designer.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.alanjordan.drcdesigner;

/**
 * Code-only display cap for response visualizations.
 *
 * Update ACTIVE_CAP_HZ to switch display cap without adding UI controls.
 */
public final class ResponseDisplayFrequencyCap {
    public static final double CAP_22K_HZ = 22050.0;
    public static final double CAP_20K_HZ = 20000.0;
    public static final double CAP_18K_HZ = 18000.0;

    // Current selection requested for response overlays and predicted tab.
    private static final double ACTIVE_CAP_HZ = CAP_22K_HZ;

    private ResponseDisplayFrequencyCap() {
    }

    public static double getActiveCapHz() {
        return ACTIVE_CAP_HZ;
    }

    public static double getEffectiveCapForSampleRate(int sampleRate) {
        return Math.min(getActiveCapHz(), sampleRate * 0.5);
    }
}
