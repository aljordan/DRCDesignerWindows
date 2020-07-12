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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;


public class CustomFilterDrcProcessor extends Thread {

	private Options options;
	private CustomizedFilterPanel parentWindow;
	private String samplingRate;
	private String impulseCenter;
	private ConfigurationEntries configEntries;
	private int customFileNumber;

	
    public CustomFilterDrcProcessor(Options options, CustomizedFilterPanel parentWindow, String samplingRate, ConfigurationEntries configEntries) {
        this.options = options;
        this.parentWindow = parentWindow;
        this.samplingRate = samplingRate;
        this.configEntries = configEntries;
        customFileNumber = 1;
    }

    @Override
    public void run() {
		parentWindow.enableDisableGenerateFiltersButton(false);
        try { sleep(1);} catch (InterruptedException ie) {}

        Targets t = new Targets(options);
        t.writeTargetPointsFile(Integer.parseInt(samplingRate));

        runDrc();
        
    	parentWindow.setStatus("Finished generating filters");
        try { sleep(1);} catch (InterruptedException ie) {}
        
		parentWindow.enableDisableGenerateFiltersButton(true);
        
    }

    private String getCommandLineParameters() {
    	return "--" + ConfigurationEntries.EntryNames.BCInitWindow.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.BCInitWindow) + " "
    	+ "--" + ConfigurationEntries.EntryNames.EPLowerWindow.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.EPLowerWindow) + " "
    	+ "--" + ConfigurationEntries.EntryNames.EPPFFinalWindow.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.EPPFFinalWindow) + " "
    	+ "--" + ConfigurationEntries.EntryNames.EPUpperWindow.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.EPUpperWindow) + " "
    	+ "--" + ConfigurationEntries.EntryNames.EPWindowExponent.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.EPWindowExponent) + " "
    	+ "--" + ConfigurationEntries.EntryNames.ISPELowerWindow.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.ISPELowerWindow) + " "
    	+ "--" + ConfigurationEntries.EntryNames.ISPEUpperWindow.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.ISPEUpperWindow) + " "
    	+ "--" + ConfigurationEntries.EntryNames.MPLowerWindow.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.MPLowerWindow) + " "
    	+ "--" + ConfigurationEntries.EntryNames.MPPFFinalWindow.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.MPPFFinalWindow) + " "
    	+ "--" + ConfigurationEntries.EntryNames.MPUpperWindow.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.MPUpperWindow) + " "
    	+ "--" + ConfigurationEntries.EntryNames.MPWindowExponent.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.MPWindowExponent) + " "
    	+ "--" + ConfigurationEntries.EntryNames.MSFilterDelay.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.MSFilterDelay) + " "
    	+ "--" + ConfigurationEntries.EntryNames.PLMaxGain.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.PLMaxGain) + " "
    	+ "--" + ConfigurationEntries.EntryNames.RTLowerWindow.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.RTLowerWindow) + " "
    	+ "--" + ConfigurationEntries.EntryNames.RTOutWindow.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.RTOutWindow) + " "
    	+ "--" + ConfigurationEntries.EntryNames.RTUpperWindow.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.RTUpperWindow) + " "
    	+ "--" + ConfigurationEntries.EntryNames.RTWindowExponent.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.RTWindowExponent) + " "
    	+ "--" + ConfigurationEntries.EntryNames.RTWindowGap.toString() + "=" + configEntries.getValue(ConfigurationEntries.EntryNames.RTWindowGap);		
    }

    private void calculateCustomFileNumber() {
		File outputFile = new File(options.getRoomCorrectionRootPath() + "\\ConvolverFilters\\" + "stereo" + samplingRate + "CUSTOM_" + customFileNumber + ".wav");
		while (outputFile.exists()) {
			customFileNumber++;
			outputFile = new File(options.getRoomCorrectionRootPath() + "\\ConvolverFilters\\" + "stereo" + samplingRate + "CUSTOM_" + customFileNumber + ".wav");
		}
    }
    
    private void runDrc() {
    	calculateCustomFileNumber();
		String drcDir = options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample";
		String convolverDir = options.getRoomCorrectionRootPath() + "\\ConvolverFilters";
		String micCompBehavior = "";
		
		if (options.isUseMicCompensationFile() && options.getMicCompensationFile() != null) {
			micCompBehavior = "--MCFilterType=M --MCPointsFile=\"" + options.getMicCompensationFilePath() + "\" ";
		}
		
		parentWindow.setStatus("Generating left channel custom " + samplingRate + " filter");
        try { sleep(1);} catch (InterruptedException ie) {}

		try {
        	PrintWriter out = new PrintWriter(new FileWriter("drcWrapperRunDRCLeftcustom_" + samplingRate +  ".bat", false));
        	out.println("cd " + drcDir);
        	out.println("drc.exe " + micCompBehavior + "--PSPointsFile=\""+ options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\DRCDesignerCustomizedPoints.txt\" " + "--BCInFile=LeftSpeakerImpulseResponse" + samplingRate + ".pcm --PSOutFile=LeftSpeaker" + samplingRate + "CUSTOM" + "_" + customFileNumber + ".pcm " + getCommandLineParameters() + " soft" + samplingRate + ".drc");
        	out.println("move /y LeftSpeaker" + samplingRate + "CUSTOM" + "_" + customFileNumber + ".pcm \"" + convolverDir + "\"");
        	out.close();
        	
        	String resultsFileName = "drcOutputLeft" + samplingRate + "custom.txt";
    		String command = "cmd.exe /c \"drcWrapperRunDRCLeft" + "custom_" + samplingRate + ".bat 1>" + resultsFileName + " 2>&1\"";
    		Runtime rt = Runtime.getRuntime();
    		Process p = rt.exec(command);
    		p.waitFor();

    		parentWindow.setStatus("Parsing results to find center");
            try { sleep(1);} catch (InterruptedException ie) {}
            impulseCenter = parseResultsFileForImpulseCenter(resultsFileName);
            
    		parentWindow.setStatus("Generating right channel custom " + samplingRate + " filter");
            try { sleep(1);} catch (InterruptedException ie) {}

        	out = new PrintWriter(new FileWriter("drcWrapperRunDRCRightcustom_" + samplingRate +  ".bat", false));
        	out.println("cd " + drcDir);
        	out.println("drc.exe " + micCompBehavior + "--PSPointsFile=\""+ options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\DRCDesignerCustomizedPoints.txt\" " + "--BCInFile=RightSpeakerImpulseResponse" + samplingRate + ".pcm --PSOutFile=RightSpeaker" + samplingRate + "CUSTOM" + "_" + customFileNumber + ".pcm --BCImpulseCenterMode=M --BCImpulseCenter=" + impulseCenter + " " + getCommandLineParameters() + " soft" + samplingRate + ".drc");
        	out.println("move /y RightSpeaker" + samplingRate + "CUSTOM" + "_" + customFileNumber + ".pcm \"" + convolverDir + "\"");
        	out.close();
        	resultsFileName = "drcOutputRight" + samplingRate + "custom.txt";
    		command = "cmd.exe /c \"drcWrapperRunDRCRight" + "custom_" + samplingRate + ".bat 1>" + resultsFileName + " 2>&1\"";
    		rt = Runtime.getRuntime();
    		p = rt.exec(command);
    		p.waitFor();
    		
//    		generateConvolverConfigFile();
    		
    		generateWavFile(samplingRate);
		}
    	catch(Exception exc){
    		exc.printStackTrace();
    	}		
    	
    }
    
    private void generateWavFile(String samplingRate) {
        String convolverDir = options.getRoomCorrectionRootPath() + "\\ConvolverFilters\\";
        String leftPcmFilePath = convolverDir + "LeftSpeaker" + samplingRate + "CUSTOM" + "_" + customFileNumber + ".pcm";
        String rightPcmFilePath = convolverDir + "RightSpeaker" + samplingRate + "CUSTOM" + "_" + customFileNumber + ".pcm";
        String outputWavFilePath = convolverDir + "Stereo" + samplingRate + "CUSTOM" + "_" + customFileNumber + ".wav";
       
        parentWindow.setStatus("Generating custom " + samplingRate + " stereo WAV file");
        try { sleep(1);} catch (InterruptedException ie) {}

        SoxProcessor sp = new SoxProcessor(options);
        sp.createWavFromRawPcm(leftPcmFilePath, rightPcmFilePath, outputWavFilePath, samplingRate, !options.isSavePcmFiles());
    }
    
    private void generateConvolverConfigFile() {
		parentWindow.setStatus("Generating custom " + samplingRate + " convolver configuration");
        try { sleep(1);} catch (InterruptedException ie) {}

        String convolverDir = options.getRoomCorrectionRootPath() + "\\ConvolverFilters\\";
		try {
		      PrintWriter out = new PrintWriter(new FileWriter(convolverDir + "convolverConfigCUSTOM" + samplingRate + "_" + customFileNumber + ".txt", false));
		      out.println(samplingRate + " 2 2 0");
		      out.println("0 0");
		      out.println("0 0");
		      out.println(convolverDir + "LeftSpeaker" + samplingRate + "CUSTOM" + "_" + customFileNumber + ".pcm");
		      out.println("0");
		      out.println("0.0");
		      out.println("0.0");
		      out.println(convolverDir + "RightSpeaker" + samplingRate + "CUSTOM" + "_" + customFileNumber + ".pcm");
		      out.println("0");
		      out.println("1.0");
		      out.println("1.0");
		      out.close();
		}
    	catch(Exception exc){
    		exc.printStackTrace();
    	}		   	
    }
    
	private String parseResultsFileForImpulseCenter(String fileName) {
        Scanner scanner;
        String line;
        int startingChar = 0;
        int endingChar = 0;
        String result = "Impulse Center Not Found";
        
        try {
            scanner = new Scanner(new File(fileName));

            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.startsWith("Impulse center found at sample ")) {
                	startingChar = 31;
                	endingChar = line.indexOf(".");
                	result =  line.substring(startingChar, endingChar);
                }
            }
            scanner.close();
        }
        catch (FileNotFoundException fnf) {
            System.out.println(fileName + " not found");
        }
        return result;
	}

}
