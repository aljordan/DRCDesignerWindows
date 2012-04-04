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

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class SoundInterfaceParser {
	private ArrayList<SoundInterface> allSoundcards = null;
	private ArrayList<SoundInterface> playbackSoundcards = null;
	private ArrayList<SoundInterface> recordingSoundcards = null;

	public SoundInterfaceParser(Options options) {
		allSoundcards = new ArrayList<SoundInterface>();
		recordingSoundcards = new ArrayList<SoundInterface>();
		playbackSoundcards = new ArrayList<SoundInterface>();

		//String command = "cmd.exe /c \"" + options.getRoomCorrectionRootPath() + "\\Rec_imp.win32\\rec_imp.exe -l 1>\\users\\c3axj\\recImpOutput.txt 2>&1\"";
		//String command = "cmd.exe /c \"" + options.getRoomCorrectionRootPath() + "\\Rec_imp.win32\\rec_imp.exe -l\"";
		String command;
		if (options.getDriverType() == Options.InterfaceDriverType.ASIO )
			command = "cmd.exe /c \"\"" + options.getRoomCorrectionRootPath() + "\\Rec_imp.win32\\rec_impAsio.exe\" -l 1>recImpOutput.txt 2>&1\"";
		else 
			command = "cmd.exe /c \"\"" + options.getRoomCorrectionRootPath() + "\\Rec_imp.win32\\rec_impDS.exe\" -l 1>recImpOutput.txt 2>&1\"";

		try {
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec(command);
			p.waitFor();
			parseResultsFile();
		}
		catch(Exception exc){
			exc.printStackTrace();
		}
		allSoundcards.trimToSize();
		
		for (SoundInterface i: allSoundcards) {
			if (i.getInputChannels() > 0)
				recordingSoundcards.add(i);
			if (i.getOutputChannels() > 0)
				playbackSoundcards.add(i);
		}
		recordingSoundcards.trimToSize();
		playbackSoundcards.trimToSize();
	}
	
	public ArrayList<SoundInterface> getAllSoundInterfaces() {
		return allSoundcards;
	}

	public ArrayList<SoundInterface> getRecordingInterfaces() {
		return recordingSoundcards;
	}

	public ArrayList<SoundInterface> getPlaybackInterfaces() {
		return playbackSoundcards;
	}

	private void parseResultsFile() {
	        Scanner scanner;
	        String line;
	        try {
	            scanner = new Scanner(new File("recImpOutput.txt"));

	            while (scanner.hasNextLine()) {
	                line = scanner.nextLine();
	                if (line.startsWith("Device:")) {
	                	SoundInterface si = new SoundInterface();
	                	si.setDeviceNumber(Integer.parseInt(line.split("#")[1]));
	                	si.setName(scanner.nextLine().split("=")[1].trim());
	                	if (scanner.nextLine().split("=")[1].trim().equalsIgnoreCase("Unsuccessful")) {
	                		si.setProbeStatusSuccessful(false);
	                	}
	                	else {
	                		si.setProbeStatusSuccessful(true);
	                		si.setOutputChannels(Integer.parseInt(scanner.nextLine().split("=")[1].trim()));
	                		si.setInputChannels(Integer.parseInt(scanner.nextLine().split("=")[1].trim()));
	                		// skip until sample rates line found
	                		String sampleRateLine = "";
	                		do {
	                			sampleRateLine = scanner.nextLine();
	                		} while (!sampleRateLine.startsWith("Supported sample rates"));
	                		si.setSupportedSampleRates(sampleRateLine.split("=")[1].trim().split(" "));
	                	}
	                		
	                	allSoundcards.add(si);
	                }
	                else if (line.trim().equals("")) {
	                	//ignore
	                }
	                else {
	                	//tempStr = line.split(";");
	                	System.out.println(line);
	                }
	            }
	            scanner.close();
	        }
	        catch (FileNotFoundException fnf) {
	            System.out.println("Input file not found");
	        }
	}
}
