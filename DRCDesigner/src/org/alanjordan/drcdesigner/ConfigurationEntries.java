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


// BCInitWindow set to 131072 for 44.1 and 48 kHz samplings rate 
// or 262144 for 88.2 and 96 kHz sampling rates
// EPWindowExponent set to MPwindowExponent
// RTWindowExponent set to MPwindowExponent
// RTUpperWindow set to MPUpperWindow
// EPUpperWindow set to MPUpperWindow
// Done MPPFFinalWindow set to MPLowerWindow
// Done EPPFFinalWindow set to EPLowerWindow
// Done ISPELowerWindow set to EPLowerWindow / 2
// Done ISPEUpperWindow set to (EPLowerWindow /2 ) * 0.75
// Done RTWindowGap set to MPUpperWindow 
// Done RTOutWindow set to MPLowerWindow
// Done MSFilterDelay set to EPLowerWindow / 2
public class ConfigurationEntries {
	private ArrayList<ConfigurationFileEntry> entries;
    public enum EntryNames {BCInitWindow, EPLowerWindow, EPUpperWindow, EPWindowExponent,
    	MPLowerWindow, MPUpperWindow, MPWindowExponent,
    	RTLowerWindow, RTUpperWindow, RTWindowExponent,
    	MPPFFinalWindow, EPPFFinalWindow, ISPELowerWindow, 
    	ISPEUpperWindow, RTWindowGap, RTOutWindow, 
    	MSFilterDelay, PLMaxGain };


	public ConfigurationEntries() {
		createEntries();
	}
	
	public ConfigurationEntries(DrcProcessor.FilterType type, DrcProcessor.SamplingRate samplingRate) {
		createEntries();
		
		switch (type) {
//		case erb:
//			switch (samplingRate) {
//			case _44100:
//				setValue(EntryNames.BCInitWindow,"131072");
//				setValue(EntryNames.EPLowerWindow,"1836");
//				setValue(EntryNames.EPUpperWindow,"40");
//				setValue(EntryNames.EPWindowExponent,"1.0");
//				setValue(EntryNames.MPLowerWindow,"5734");
//				setValue(EntryNames.MPUpperWindow,"40");
//				setValue(EntryNames.MPWindowExponent,"1.87");
//				setValue(EntryNames.RTLowerWindow,"5734");
//				setValue(EntryNames.RTUpperWindow,"40");
//				setValue(EntryNames.RTWindowExponent,"1.87");
//				setValue(EntryNames.MPPFFinalWindow, "5734");
//				setValue(EntryNames.RTOutWindow, "5734");
//				setValue(EntryNames.EPPFFinalWindow, "1836");
//				setValue(EntryNames.ISPELowerWindow, "918");
//				setValue(EntryNames.MSFilterDelay, "918");
//				setValue(EntryNames.ISPEUpperWindow, "688");
//				setValue(EntryNames.RTWindowGap, "40");
//				break;
//
//			case _48000:
//				setValue(EntryNames.BCInitWindow,"131072");
//				setValue(EntryNames.EPLowerWindow,"2208");
//				setValue(EntryNames.EPUpperWindow,"44");
//				setValue(EntryNames.EPWindowExponent,"1.0");
//				setValue(EntryNames.MPLowerWindow,"6240");
//				setValue(EntryNames.MPUpperWindow,"44");
//				setValue(EntryNames.MPWindowExponent,"1.87");
//				setValue(EntryNames.RTLowerWindow,"6240");
//				setValue(EntryNames.RTUpperWindow,"44");
//				setValue(EntryNames.RTWindowExponent,"1.87");
//				setValue(EntryNames.MPPFFinalWindow, "6240");
//				setValue(EntryNames.RTOutWindow, "6240");
//				setValue(EntryNames.EPPFFinalWindow, "2208");
//				setValue(EntryNames.ISPELowerWindow, "1104");
//				setValue(EntryNames.MSFilterDelay, "1104");
//				setValue(EntryNames.ISPEUpperWindow, "828");
//				setValue(EntryNames.RTWindowGap, "44");
//				break;
//
//			case _88200:
//				setValue(EntryNames.BCInitWindow,"262144");
//				setValue(EntryNames.EPLowerWindow,"3672");
//				setValue(EntryNames.EPUpperWindow,"80");
//				setValue(EntryNames.EPWindowExponent,"1.0");
//				setValue(EntryNames.MPLowerWindow,"11468");
//				setValue(EntryNames.MPUpperWindow,"80");
//				setValue(EntryNames.MPWindowExponent,"1.87");
//				setValue(EntryNames.RTLowerWindow,"11468");
//				setValue(EntryNames.RTUpperWindow,"80");
//				setValue(EntryNames.RTWindowExponent,"1.87");
//				setValue(EntryNames.MPPFFinalWindow, "11468");
//				setValue(EntryNames.RTOutWindow, "11468");
//				setValue(EntryNames.EPPFFinalWindow, "3672");
//				setValue(EntryNames.ISPELowerWindow, "1836");
//				setValue(EntryNames.MSFilterDelay, "1836");
//				setValue(EntryNames.ISPEUpperWindow, "1377");
//				setValue(EntryNames.RTWindowGap, "80");
//				break;
//
//			case _96000:
//				setValue(EntryNames.BCInitWindow,"262144");
//				setValue(EntryNames.EPLowerWindow,"4416");
//				setValue(EntryNames.EPUpperWindow,"88");
//				setValue(EntryNames.EPWindowExponent,"1.0");
//				setValue(EntryNames.MPLowerWindow,"12480");
//				setValue(EntryNames.MPUpperWindow,"88");
//				setValue(EntryNames.MPWindowExponent,"1.87");
//				setValue(EntryNames.RTLowerWindow,"12480");
//				setValue(EntryNames.RTUpperWindow,"88");
//				setValue(EntryNames.RTWindowExponent,"1.87");
//				setValue(EntryNames.MPPFFinalWindow, "12480");
//				setValue(EntryNames.RTOutWindow, "12480");
//				setValue(EntryNames.EPPFFinalWindow, "4416");
//				setValue(EntryNames.ISPELowerWindow, "2208");
//				setValue(EntryNames.MSFilterDelay, "2208");
//				setValue(EntryNames.ISPEUpperWindow, "1656");
//				setValue(EntryNames.RTWindowGap, "88");
//				break;
//			}
//			break;
		case minimal:
			switch (samplingRate) {
			case _44100:
				setValue(EntryNames.BCInitWindow,"131072");
				setValue(EntryNames.EPLowerWindow,"918");
				setValue(EntryNames.EPUpperWindow,"22");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"22050");
				setValue(EntryNames.MPUpperWindow,"22");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"22050");
				setValue(EntryNames.RTUpperWindow,"22");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "22050");
				setValue(EntryNames.RTOutWindow, "22050");
				setValue(EntryNames.EPPFFinalWindow, "918");
				setValue(EntryNames.ISPELowerWindow, "459");
				setValue(EntryNames.MSFilterDelay, "459");
				setValue(EntryNames.ISPEUpperWindow, "344");
				setValue(EntryNames.RTWindowGap, "22");
				setValue(EntryNames.PLMaxGain,"1.8");
				break;
				
			case _48000:
				setValue(EntryNames.BCInitWindow,"131072");
				setValue(EntryNames.EPLowerWindow,"998");
				setValue(EntryNames.EPUpperWindow,"24");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"24000");
				setValue(EntryNames.MPUpperWindow,"24");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"24000");
				setValue(EntryNames.RTUpperWindow,"24");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "24000");
				setValue(EntryNames.RTOutWindow, "24000");
				setValue(EntryNames.EPPFFinalWindow, "998");
				setValue(EntryNames.ISPELowerWindow, "499");
				setValue(EntryNames.MSFilterDelay, "499");
				setValue(EntryNames.ISPEUpperWindow, "374");
				setValue(EntryNames.RTWindowGap, "24");
				setValue(EntryNames.PLMaxGain,"1.8");
				break;

			case _88200:
				setValue(EntryNames.BCInitWindow,"262144");
				setValue(EntryNames.EPLowerWindow,"1836");
				setValue(EntryNames.EPUpperWindow,"44");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"44100");
				setValue(EntryNames.MPUpperWindow,"44");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"44100");
				setValue(EntryNames.RTUpperWindow,"44");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "44100");
				setValue(EntryNames.RTOutWindow, "44100");
				setValue(EntryNames.EPPFFinalWindow, "1836");
				setValue(EntryNames.ISPELowerWindow, "918");
				setValue(EntryNames.MSFilterDelay, "918");
				setValue(EntryNames.ISPEUpperWindow, "688");
				setValue(EntryNames.RTWindowGap, "44");
				setValue(EntryNames.PLMaxGain,"1.8");
				break;

			case _96000:
				setValue(EntryNames.BCInitWindow,"262144");
				setValue(EntryNames.EPLowerWindow,"1996");
				setValue(EntryNames.EPUpperWindow,"48");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"48000");
				setValue(EntryNames.MPUpperWindow,"48");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"48000");
				setValue(EntryNames.RTUpperWindow,"48");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "48000");
				setValue(EntryNames.RTOutWindow, "48000");
				setValue(EntryNames.EPPFFinalWindow, "1996");
				setValue(EntryNames.ISPELowerWindow, "998");
				setValue(EntryNames.MSFilterDelay, "998");
				setValue(EntryNames.ISPEUpperWindow, "748");
				setValue(EntryNames.RTWindowGap, "48");
				setValue(EntryNames.PLMaxGain,"1.8");
				break;
			}
			break;
			
		case soft:
			switch (samplingRate) {
			case _44100:
				setValue(EntryNames.BCInitWindow,"131072");
				setValue(EntryNames.EPLowerWindow,"1366");
				setValue(EntryNames.EPUpperWindow,"32");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"32768");
				setValue(EntryNames.MPUpperWindow,"32");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"32768");
				setValue(EntryNames.RTUpperWindow,"32");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "32768");
				setValue(EntryNames.RTOutWindow, "32768");
				setValue(EntryNames.EPPFFinalWindow, "1366");
				setValue(EntryNames.ISPELowerWindow, "683");
				setValue(EntryNames.MSFilterDelay, "683");
				setValue(EntryNames.ISPEUpperWindow, "512");
				setValue(EntryNames.RTWindowGap, "32");
				setValue(EntryNames.PLMaxGain,"1.8");
				break;
			
			case _48000:
				setValue(EntryNames.BCInitWindow,"131072");
				setValue(EntryNames.EPLowerWindow,"1488");
				setValue(EntryNames.EPUpperWindow,"34");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"35520");
				setValue(EntryNames.MPUpperWindow,"34");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"35520");
				setValue(EntryNames.RTUpperWindow,"34");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "35520");
				setValue(EntryNames.RTOutWindow, "35520");
				setValue(EntryNames.EPPFFinalWindow, "1488");
				setValue(EntryNames.ISPELowerWindow, "744");
				setValue(EntryNames.MSFilterDelay, "744");
				setValue(EntryNames.ISPEUpperWindow, "558");
				setValue(EntryNames.RTWindowGap, "34");
				setValue(EntryNames.PLMaxGain,"1.8");
				break;
	
			case _88200:
				setValue(EntryNames.BCInitWindow,"262144");
				setValue(EntryNames.EPLowerWindow,"2732");
				setValue(EntryNames.EPUpperWindow,"64");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"65536");
				setValue(EntryNames.MPUpperWindow,"64");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"65536");
				setValue(EntryNames.RTUpperWindow,"64");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "65536");
				setValue(EntryNames.RTOutWindow, "65536");
				setValue(EntryNames.EPPFFinalWindow, "2732");
				setValue(EntryNames.ISPELowerWindow, "1366");
				setValue(EntryNames.MSFilterDelay, "1366");
				setValue(EntryNames.ISPEUpperWindow, "1024");
				setValue(EntryNames.RTWindowGap, "64");
				setValue(EntryNames.PLMaxGain,"1.8");
				break;
	
			case _96000:
				setValue(EntryNames.BCInitWindow,"262144");
				setValue(EntryNames.EPLowerWindow,"2976");
				setValue(EntryNames.EPUpperWindow,"68");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"71040");
				setValue(EntryNames.MPUpperWindow,"68");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"71040");
				setValue(EntryNames.RTUpperWindow,"68");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "71040");
				setValue(EntryNames.RTOutWindow, "71040");
				setValue(EntryNames.EPPFFinalWindow, "2976");
				setValue(EntryNames.ISPELowerWindow, "1488");
				setValue(EntryNames.MSFilterDelay, "1488");
				setValue(EntryNames.ISPEUpperWindow, "1116");
				setValue(EntryNames.RTWindowGap, "68");
				setValue(EntryNames.PLMaxGain,"1.8");
				break;
			}
			break;
			
		case normal:
			switch (samplingRate) {
			case _44100:	
				setValue(EntryNames.BCInitWindow,"131072");
				setValue(EntryNames.EPLowerWindow,"1836");
				setValue(EntryNames.EPUpperWindow,"44");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"44100");
				setValue(EntryNames.MPUpperWindow,"44");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"44100");
				setValue(EntryNames.RTUpperWindow,"44");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "44100");
				setValue(EntryNames.RTOutWindow, "44100");
				setValue(EntryNames.EPPFFinalWindow, "1836");
				setValue(EntryNames.ISPELowerWindow, "918");
				setValue(EntryNames.MSFilterDelay, "918");
				setValue(EntryNames.ISPEUpperWindow, "688");
				setValue(EntryNames.RTWindowGap, "44");
				setValue(EntryNames.PLMaxGain,"2.0");
				break;
				
			case _48000:
				setValue(EntryNames.BCInitWindow,"131072");
				setValue(EntryNames.EPLowerWindow,"2044");
				setValue(EntryNames.EPUpperWindow,"48");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"48000");
				setValue(EntryNames.MPUpperWindow,"48");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"48000");
				setValue(EntryNames.RTUpperWindow,"48");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "48000");
				setValue(EntryNames.RTOutWindow, "48000");
				setValue(EntryNames.EPPFFinalWindow, "2044");
				setValue(EntryNames.ISPELowerWindow, "1022");
				setValue(EntryNames.MSFilterDelay, "1022");
				setValue(EntryNames.ISPEUpperWindow, "766");
				setValue(EntryNames.RTWindowGap, "48");
				setValue(EntryNames.PLMaxGain,"2.0");
				break;
	
			case _88200:
				setValue(EntryNames.BCInitWindow,"262144");
				setValue(EntryNames.EPLowerWindow,"3672");
				setValue(EntryNames.EPUpperWindow,"88");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"88200");
				setValue(EntryNames.MPUpperWindow,"88");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"88200");
				setValue(EntryNames.RTUpperWindow,"88");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "88200");
				setValue(EntryNames.RTOutWindow, "88200");
				setValue(EntryNames.EPPFFinalWindow, "3672");
				setValue(EntryNames.ISPELowerWindow, "1836");
				setValue(EntryNames.MSFilterDelay, "1836");
				setValue(EntryNames.ISPEUpperWindow, "1377");
				setValue(EntryNames.RTWindowGap, "88");
				setValue(EntryNames.PLMaxGain,"2.0");
				break;
	
			case _96000:
				setValue(EntryNames.BCInitWindow,"262144");
				setValue(EntryNames.EPLowerWindow,"4088");
				setValue(EntryNames.EPUpperWindow,"96");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"96000");
				setValue(EntryNames.MPUpperWindow,"96");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"96000");
				setValue(EntryNames.RTUpperWindow,"96");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "96000");
				setValue(EntryNames.RTOutWindow, "96000");
				setValue(EntryNames.EPPFFinalWindow, "4088");
				setValue(EntryNames.ISPELowerWindow, "2044");
				setValue(EntryNames.MSFilterDelay, "2044");
				setValue(EntryNames.ISPEUpperWindow, "1533");
				setValue(EntryNames.RTWindowGap, "96");
				setValue(EntryNames.PLMaxGain,"2.0");
				break;
			}
			break;
		
		case strong:
			switch (samplingRate) {
			case _44100:
				setValue(EntryNames.BCInitWindow,"131072");
				setValue(EntryNames.EPLowerWindow,"2000");
				setValue(EntryNames.EPUpperWindow,"48");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"48000");
				setValue(EntryNames.MPUpperWindow,"48");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"48000");
				setValue(EntryNames.RTUpperWindow,"48");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "48000");
				setValue(EntryNames.RTOutWindow, "48000");
				setValue(EntryNames.EPPFFinalWindow, "2000");
				setValue(EntryNames.ISPELowerWindow, "1000");
				setValue(EntryNames.MSFilterDelay, "1000");
				setValue(EntryNames.ISPEUpperWindow, "750");
				setValue(EntryNames.RTWindowGap, "48");
				setValue(EntryNames.PLMaxGain,"2.0");
				break;
	
			case _48000:
				setValue(EntryNames.BCInitWindow,"131072");
				setValue(EntryNames.EPLowerWindow,"2178");
				setValue(EntryNames.EPUpperWindow,"52");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"52224");
				setValue(EntryNames.MPUpperWindow,"52");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"52224");
				setValue(EntryNames.RTUpperWindow,"52");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "52224");
				setValue(EntryNames.RTOutWindow, "52224");
				setValue(EntryNames.EPPFFinalWindow, "2178");
				setValue(EntryNames.ISPELowerWindow, "1089");
				setValue(EntryNames.MSFilterDelay, "1089");
				setValue(EntryNames.ISPEUpperWindow, "817");
				setValue(EntryNames.RTWindowGap, "52");
				setValue(EntryNames.PLMaxGain,"2.0");
				break;
	
			case _88200:
				setValue(EntryNames.BCInitWindow,"262144");
				setValue(EntryNames.EPLowerWindow,"4000");
				setValue(EntryNames.EPUpperWindow,"96");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"96000");
				setValue(EntryNames.MPUpperWindow,"96");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"96000");
				setValue(EntryNames.RTUpperWindow,"96");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "96000");
				setValue(EntryNames.RTOutWindow, "96000");
				setValue(EntryNames.EPPFFinalWindow, "4000");
				setValue(EntryNames.ISPELowerWindow, "2000");
				setValue(EntryNames.MSFilterDelay, "2000");
				setValue(EntryNames.ISPEUpperWindow, "1500");
				setValue(EntryNames.RTWindowGap, "96");
				setValue(EntryNames.PLMaxGain,"2.0");
				break;
	
			case _96000:
				setValue(EntryNames.BCInitWindow,"262144");
				setValue(EntryNames.EPLowerWindow,"4356");
				setValue(EntryNames.EPUpperWindow,"104");
				setValue(EntryNames.EPWindowExponent,"1.0");
				setValue(EntryNames.MPLowerWindow,"104448");
				setValue(EntryNames.MPUpperWindow,"104");
				setValue(EntryNames.MPWindowExponent,"1.0");
				setValue(EntryNames.RTLowerWindow,"104448");
				setValue(EntryNames.RTUpperWindow,"104");
				setValue(EntryNames.RTWindowExponent,"1.0");
				setValue(EntryNames.MPPFFinalWindow, "104448");
				setValue(EntryNames.RTOutWindow, "104448");
				setValue(EntryNames.EPPFFinalWindow, "4356");
				setValue(EntryNames.ISPELowerWindow, "2178");
				setValue(EntryNames.MSFilterDelay, "2178");
				setValue(EntryNames.ISPEUpperWindow, "1634");
				setValue(EntryNames.RTWindowGap, "104");
				setValue(EntryNames.PLMaxGain,"2.0");
				break;
			}
		}
	}
	
	private void createEntries() {
		entries = new ArrayList<ConfigurationFileEntry>();
		entries.add(new ConfigurationFileEntry("MPLowerWindow"));
		entries.add(new ConfigurationFileEntry("MPUpperWindow"));
		entries.add(new ConfigurationFileEntry("MPWindowExponent"));
		entries.add(new ConfigurationFileEntry("EPLowerWindow"));
		entries.add(new ConfigurationFileEntry("EPUpperWindow"));
		entries.add(new ConfigurationFileEntry("EPWindowExponent"));
		entries.add(new ConfigurationFileEntry("RTLowerWindow"));
		entries.add(new ConfigurationFileEntry("RTUpperWindow"));
		entries.add(new ConfigurationFileEntry("RTWindowExponent"));
		entries.add(new ConfigurationFileEntry("BCInitWindow"));
		entries.add(new ConfigurationFileEntry("MPPFFinalWindow"));
		entries.add(new ConfigurationFileEntry("RTOutWindow"));
		entries.add(new ConfigurationFileEntry("EPPFFinalWindow"));
		entries.add(new ConfigurationFileEntry("ISPELowerWindow"));
		entries.add(new ConfigurationFileEntry("MSFilterDelay"));
		entries.add(new ConfigurationFileEntry("ISPEUpperWindow"));
		entries.add(new ConfigurationFileEntry("RTWindowGap"));
		entries.add(new ConfigurationFileEntry("PLMaxGain"));
		entries.trimToSize();
	}

	public void setValue(EntryNames name, String value) {
		for (ConfigurationFileEntry cfe : entries) {
			if (cfe.getName().equalsIgnoreCase(name.toString())) {
				cfe.setValue(value);
				break;
			}
		}
	}
	
	public String getValue(EntryNames name) {
		String result = null;
		for (ConfigurationFileEntry cfe : entries) {
			if (cfe.getName().equalsIgnoreCase(name.toString())) {
				result = cfe.getValue();
				break;
			}
		}
		return result;
	}

}
