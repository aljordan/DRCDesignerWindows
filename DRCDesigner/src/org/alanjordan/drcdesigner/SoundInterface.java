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

public class SoundInterface {
	private String name = null;
	private int deviceNumber = -1;
	private boolean probeStatusSuccessful = false;
	private int outputChannels = -1;
	private int inputChannels = -1;
	private String[] supportedSampleRates = null;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean handlesSampleRate(String sampleRate) {
		boolean result = false;
		for (int counter = 0; counter < supportedSampleRates.length; counter++) {
			if (supportedSampleRates[counter].equalsIgnoreCase(sampleRate)) {
				result = true;
			}
		}
		return result;
	}
	
	public String getName() {
		return name;
	}
	
	public void setDeviceNumber(int deviceNumber) {
		this.deviceNumber = deviceNumber;
	}
	
	public int getDeviceNumber() {
		return deviceNumber;
	}

	public void setProbeStatusSuccessful(boolean probeStatusSuccessful) {
		this.probeStatusSuccessful = probeStatusSuccessful;
	}

	public boolean isProbeStatusSuccessful() {
		return probeStatusSuccessful;
	}

	public void setOutputChannels(int outputChannels) {
		this.outputChannels = outputChannels;
	}

	public int getOutputChannels() {
		return outputChannels;
	}

	public void setInputChannels(int inputChannels) {
		this.inputChannels = inputChannels;
	}

	public int getInputChannels() {
		return inputChannels;
	}

	public void setSupportedSampleRates(String[] supportedSampleRates) {
		if ((supportedSampleRates[0] != null && supportedSampleRates[0].equalsIgnoreCase("100"))
				&& (supportedSampleRates[1] != null && supportedSampleRates[1].equalsIgnoreCase("200000")))
			this.supportedSampleRates = new String[] {"44100","48000","88200","96000"};
		else { //roundabout way of only ensuring sample rates between 44100 and 96000 get into the drop downs 
			boolean has44100 = false;
			boolean has48000 = false;
			boolean has88200 = false;
			boolean has96000 = false;
			int arrayLength = 0;
			for (int counter = 0; counter < supportedSampleRates.length; counter++) {
				if (supportedSampleRates[counter].equals("44100")) {
					has44100 = true;
					arrayLength = arrayLength + 1;
				}
				if (supportedSampleRates[counter].equals("48000")) {
					has48000 = true;
					arrayLength = arrayLength + 1;
				}
				if (supportedSampleRates[counter].equals("88200")) {
					has88200 = true;
					arrayLength = arrayLength + 1;
				}
				if (supportedSampleRates[counter].equals("96000")) {
					has96000 = true;
					arrayLength = arrayLength + 1;
				}
			}
			String[] allowedSampleRates = new String[arrayLength];
			int currentPosition = 0;
			if (has44100) {
				allowedSampleRates[currentPosition] = "44100";
				currentPosition++;
			}
			if (has48000) {
				allowedSampleRates[currentPosition] = "48000";
				currentPosition++;
			}
			if (has88200) {
				allowedSampleRates[currentPosition] = "88200";
				currentPosition++;
			}
			if (has96000) {
				allowedSampleRates[currentPosition] = "96000";
			}
			this.supportedSampleRates = allowedSampleRates;
			}
	}

	public String[] getSupportedSampleRates() {
		return supportedSampleRates;
	}
	
}
