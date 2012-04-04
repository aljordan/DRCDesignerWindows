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

public class FrequencyAmplitudePoint  implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double frequency;
	private double amplitude;
	
	public FrequencyAmplitudePoint() {
		this.setFrequency(0.0);
		this.setAmplitude(0.0);
	}
	
	public FrequencyAmplitudePoint(double frequency, double amplitude) {
		this.frequency = frequency;
		this.amplitude = amplitude;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}

	public double getAmplitude() {
		return amplitude;
	}

}
