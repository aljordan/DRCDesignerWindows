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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class FrequencyAmplitudePoints implements java.io.Serializable {

		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		private ArrayList<FrequencyAmplitudePoint> faPoints;
		private double lowestAmplitude;
		private double highestAmplitude;
		private double lowestFrequency;
		private double highestFrequency;
		private boolean initialized;

		public FrequencyAmplitudePoints() {
			this.initialized = false;
			this.faPoints = new ArrayList<FrequencyAmplitudePoint>();
		}
		
		private void sort() {
	        Collections.sort(faPoints, new Comparator<FrequencyAmplitudePoint>(){
	            public int compare(FrequencyAmplitudePoint p1, FrequencyAmplitudePoint p2) {
	               return (int)p1.getFrequency() - (int)p2.getFrequency();
	            }
	        });
		}
		
		public void trimToSize() {
			faPoints.trimToSize();
		}
		
		// returns the index of a point close enough to be considered a matching point
		// if no point is found -1 is returned.
		// amplitude has to match
		public int findSimilarPoint(double frequency, double amplitude) {
			int returnValue = -1;
			int searchRange = 1;
			if (frequency >= 0 && frequency <= 100)
				searchRange = 4;
			else if (frequency > 100 && frequency <= 400)
				searchRange = 16;
			else if (frequency > 400 && frequency <= 1000)
				searchRange = 20;
			else if (frequency > 1000 && frequency <= 6000)
				searchRange = 200;
			else if (frequency > 6000 && frequency <= 20000)
				searchRange = 600;
			else if (frequency == 21000 || frequency == 22050)
				searchRange = 1;
				
			for (int counter = 0; counter < faPoints.size(); counter++)
			{
				FrequencyAmplitudePoint fap = faPoints.get(counter);
				// if freq = 21000 or 22050 only match freq and ignore amplitude
				if (frequency == 21000 || frequency == 22050) {
					if (frequency > (fap.getFrequency() - searchRange) && (frequency < (fap.getFrequency() + searchRange)))
						return counter;					
				}
				else if (fap.getAmplitude() == amplitude) {
					if (frequency > (fap.getFrequency() - searchRange) && (frequency < (fap.getFrequency() + searchRange))) {
						return counter;
					}
				}
			}
				
			return returnValue;
		}
		
		public void removePoint(int index) {
			faPoints.remove(index);
			this.trimToSize();
		}
		
		public int getNumberOfFrequencyDataPoints() {
			return faPoints.size();
		}
		
		public FrequencyAmplitudePoint getFrequencyAmplitudePoint(int index) {
			return faPoints.get(index - 1);
		}

		
		public FrequencyAmplitudePoints getDataRange(double lowFrequency, double highFrequency) {
			FrequencyAmplitudePoints returnValue = new FrequencyAmplitudePoints();
			Iterator<FrequencyAmplitudePoint> i = faPoints.iterator();
			while (i.hasNext()) {
				FrequencyAmplitudePoint p = i.next();
				if (p.getFrequency() >= lowFrequency && p.getFrequency() <= highFrequency)
					returnValue.addFrequencyAmplitudePoint(p);
			}
			returnValue.trim();
			return returnValue;
		}
		
		public void addFrequencyAmplitudePoint(FrequencyAmplitudePoint point) {
			faPoints.add(point);
			if (!initialized) {
				this.lowestAmplitude = point.getAmplitude();
				this.highestAmplitude = point.getAmplitude();
				this.lowestFrequency = point.getFrequency();
				this.highestFrequency = point.getFrequency();
				initialized = true;
			} else {
	    		if (point.getFrequency() < this.lowestFrequency)
	    			this.lowestFrequency = point.getFrequency();
	    		if (point.getFrequency() > this.highestFrequency)
	    			this.highestFrequency = point.getFrequency();
	    		if (point.getAmplitude() < this.lowestAmplitude)
	    			this.lowestAmplitude = point.getAmplitude();
	    		if (point.getAmplitude() > this.highestAmplitude)
	    			this.highestAmplitude = point.getAmplitude();
			}
			this.sort();
		}
		
		public ArrayList<FrequencyAmplitudePoint> getPoints() {
			return this.faPoints;
		}
		
		public void trim() {
			this.faPoints.trimToSize();
		}

		public double getLowestAmplitude() {
			return lowestAmplitude;
		}

		public double getHighestAmplitude() {
			return highestAmplitude;
		}

		public double getLowestFrequency() {
			return lowestFrequency;
		}

		public double getHighestFrequency() {
			return highestFrequency;
		}

}
