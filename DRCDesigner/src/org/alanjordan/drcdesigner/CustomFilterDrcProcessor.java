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
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;


public class CustomFilterDrcProcessor extends Thread {
	private static final Logger LOGGER = AppLogger.getLogger();

	private Options options;
	private CustomizedFilterPanel parentWindow;
	private String samplingRate;
	private String impulseCenter;
	private ConfigurationEntries configEntries;
	private int customFileNumber;

	private File getWorkFile(String fileName) {
		return new File(Options.getAppDataDirectory(), fileName);
	}

	
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

        boolean generationSucceeded;
        boolean upsampleAttempted = false;
        boolean upsampleSucceeded = true;

        Targets t = new Targets(options);
        t.writeTargetPointsFile(Integer.parseInt(samplingRate));

        generationSucceeded = runDrc();

		if (generationSucceeded && parentWindow.shouldUpsampleGeneratedFilters() && supportsPostGenerationUpsample()) {
			upsampleAttempted = true;
			upsampleSucceeded = upsampleCustomWav();
		}
        
		if (!generationSucceeded) {
			parentWindow.setStatus("Finished generating filters with errors");
		}
		else if (upsampleAttempted && !upsampleSucceeded) {
			parentWindow.setStatus("Finished generating filters; high-rate upsample had errors");
		}
		else {
	    		parentWindow.setStatus("Finished generating filters");
		}
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
    
    private boolean runDrc() {
    	calculateCustomFileNumber();
		String drcDir = options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample";
		String convolverDir = options.getRoomCorrectionRootPath() + "\\ConvolverFilters";
		
		parentWindow.setStatus("Generating left channel custom " + samplingRate + " filter");
        try { sleep(1);} catch (InterruptedException ie) {}

		try {
	        String leftOutputPcmName = "LeftSpeaker" + samplingRate + "CUSTOM" + "_" + customFileNumber + ".pcm";
	        List<String> leftCommand = createDrcCommand(
	        		"LeftSpeakerImpulseResponse" + samplingRate + ".pcm",
	        		leftOutputPcmName,
	        		impulseCenter,
	        		false);
	        File resultsFile = getWorkFile("drcOutputLeft" + samplingRate + "custom.txt");
	    		runDrcCommand(leftCommand, new File(drcDir), resultsFile, "DRC custom left " + samplingRate);
	    		moveOutputFile(drcDir, leftOutputPcmName, convolverDir);

    		parentWindow.setStatus("Parsing results to find center");
            try { sleep(1);} catch (InterruptedException ie) {}
	        impulseCenter = parseResultsFileForImpulseCenter(resultsFile.getAbsolutePath());
            
    		parentWindow.setStatus("Generating right channel custom " + samplingRate + " filter");
            try { sleep(1);} catch (InterruptedException ie) {}

	        String rightOutputPcmName = "RightSpeaker" + samplingRate + "CUSTOM" + "_" + customFileNumber + ".pcm";
	        List<String> rightCommand = createDrcCommand(
	        		"RightSpeakerImpulseResponse" + samplingRate + ".pcm",
	        		rightOutputPcmName,
	        		impulseCenter,
	        		true);
	        resultsFile = getWorkFile("drcOutputRight" + samplingRate + "custom.txt");
	    		runDrcCommand(rightCommand, new File(drcDir), resultsFile, "DRC custom right " + samplingRate);
	    		moveOutputFile(drcDir, rightOutputPcmName, convolverDir);
    		
//    		generateConvolverConfigFile();
    		
    		generateWavFile(samplingRate);
		}
    	catch(Exception exc){
			LOGGER.warning("Failed running custom DRC " + samplingRate + ": " + exc.getMessage());
    		exc.printStackTrace();
	    		return false;
    	}		
	    
		return true;
    }

	private boolean supportsPostGenerationUpsample() {
		return "88200".equals(samplingRate) || "96000".equals(samplingRate);
	}

	private boolean upsampleCustomWav() {
		String destinationRate = "88200".equals(samplingRate) ? "176400" : "192000";
		String convolverDir = options.getRoomCorrectionRootPath() + "\\ConvolverFilters\\";
		String inputPath = convolverDir + "Stereo" + samplingRate + "CUSTOM" + "_" + customFileNumber + ".wav";
		String outputPath = convolverDir + "Stereo" + destinationRate + "CUSTOM" + "_" + customFileNumber + ".wav";

		parentWindow.setStatus("Upsampling custom filter to " + destinationRate);
		try { sleep(1);} catch (InterruptedException ie) {}

		SoxProcessor sp = new SoxProcessor(options);
		boolean success = sp.resampleWavFile(inputPath, outputPath, destinationRate);
		if (!success) {
			LOGGER.warning("Failed upsampling custom filter from " + samplingRate + " to " + destinationRate + " for index " + customFileNumber);
		}
		return success;
	}

	private List<String> createDrcCommand(String inputPcmName, String outputPcmName, String impulseCenterValue, boolean includeImpulseCenter) {
		List<String> command = new ArrayList<String>();
		String drcExecutablePath = options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\drc.exe";
		command.add(drcExecutablePath);
		if (options.isUseMicCompensationFile() && options.getMicCompensationFile() != null) {
			command.add("--MCFilterType=M");
			command.add("--MCPointsFile=" + options.getMicCompensationFilePath());
		}
		command.add("--PSPointsFile=" + options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\DRCDesignerCustomizedPoints.txt");
		command.add("--BCInFile=" + inputPcmName);
		command.add("--PSOutFile=" + outputPcmName);
		if (includeImpulseCenter) {
			command.add("--BCImpulseCenterMode=M");
			command.add("--BCImpulseCenter=" + impulseCenterValue);
		}
		for (String parameter : getCommandLineParameters().split(" ")) {
			if (parameter.length() > 0) {
				command.add(parameter);
			}
		}
		command.add("soft" + samplingRate + ".drc");
		return command;
	}

	private void runDrcCommand(List<String> command, File workingDirectory, File outputFile, String operationName) throws IOException, InterruptedException {
		String psPointsFile = getCommandArgumentValue(command, "--PSPointsFile=");
		String templateFile = command.get(command.size() - 1);
		LOGGER.info(operationName + " inputs: PSPointsFile=" + psPointsFile + ", Template=" + templateFile);

		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(workingDirectory);
		int exitCode = ProcessExecutionUtil.runCommandAndCaptureOutput(pb, outputFile, operationName);
		if (exitCode != 0) {
			throw new IOException(operationName + " failed with exit code " + exitCode);
		}
	}

	private String getCommandArgumentValue(List<String> command, String prefix) {
		for (String argument : command) {
			if (argument.startsWith(prefix)) {
				return argument.substring(prefix.length());
			}
		}
		return "";
	}

	private void moveOutputFile(String sourceDirectory, String fileName, String destinationDirectory) throws IOException {
		Path source = new File(sourceDirectory, fileName).toPath();
		Path destination = new File(destinationDirectory, fileName).toPath();
		Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
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
			LOGGER.warning("Failed running custom DRC " + samplingRate + ": " + exc.getMessage());
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
