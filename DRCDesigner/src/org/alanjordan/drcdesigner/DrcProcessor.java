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

//import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
//import java.io.FileReader;
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

public class DrcProcessor extends Thread {
	private static final Logger LOGGER = AppLogger.getLogger();

    public enum FilterType {erb, minimal, soft, normal, strong};
    public enum SamplingRate {_44100, _48000, _88200, _96000};
	private Options options;
	private StandardFiltersPanel parentWindow;
	private String samplingRate;
	private String impulseCenter;

	private File getWorkFile(String fileName) {
		return new File(Options.getAppDataDirectory(), fileName);
	}
	
    public DrcProcessor(Options options, StandardFiltersPanel parentWindow, String samplingRate) {
        this.options = options;
        this.parentWindow = parentWindow;
        this.samplingRate = samplingRate;
    }

    @Override
    public void run() {
		parentWindow.enableDisableGenerateFiltersButton(false);
        try { sleep(1);} catch (InterruptedException ie) {}

        boolean allSucceeded = true;
        boolean upsampleAttempted = false;
        boolean upsampleSucceeded = true;
        
        Targets t = new Targets(options);
        t.writeTargetPointsFile(Integer.parseInt(samplingRate));
		
        if (parentWindow.checkSelectedFilterType(FilterType.erb))
	        	allSucceeded = runDrc(FilterType.erb) && allSucceeded;
        if (parentWindow.checkSelectedFilterType(FilterType.minimal))
	        	allSucceeded = runDrc(FilterType.minimal) && allSucceeded;
        if (parentWindow.checkSelectedFilterType(FilterType.soft))
	        	allSucceeded = runDrc(FilterType.soft) && allSucceeded;
        if (parentWindow.checkSelectedFilterType(FilterType.normal))
	        	allSucceeded = runDrc(FilterType.normal) && allSucceeded;
        if (parentWindow.checkSelectedFilterType(FilterType.strong))
	        	allSucceeded = runDrc(FilterType.strong) && allSucceeded;

		if (allSucceeded && parentWindow.shouldUpsampleGeneratedFilters() && supportsPostGenerationUpsample()) {
			upsampleAttempted = true;
			upsampleSucceeded = upsampleGeneratedFiltersForSelectedTypes();
		}
        
		if (!allSucceeded) {
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



    private boolean runDrc(FilterType fType) {
		String drcDir = options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample";
		String convolverDir = options.getRoomCorrectionRootPath() + "\\ConvolverFilters";
		
		parentWindow.setStatus("Generating left channel " + fType.toString() + " " + samplingRate + " filter");
        try { sleep(1);} catch (InterruptedException ie) {}

		try {
	        String leftOutputPcmName = "LeftSpeaker" + samplingRate + fType.toString().toUpperCase() + ".pcm";
	        List<String> leftCommand = createDrcCommand(
	        		"LeftSpeakerImpulseResponse" + samplingRate + ".pcm",
	        		leftOutputPcmName,
	        		fType.toString() + samplingRate + ".drc",
	        		null);
	        File resultsFile = getWorkFile("drcOutputLeft" + samplingRate + fType.toString() + ".txt");
	    		runDrcCommand(leftCommand, new File(drcDir), resultsFile, "DRC left " + fType + " " + samplingRate);
	    		moveOutputFile(drcDir, leftOutputPcmName, convolverDir);

    		parentWindow.setStatus("Parsing results to find center");
            try { sleep(1);} catch (InterruptedException ie) {}
	        impulseCenter = parseResultsFileForImpulseCenter(resultsFile.getAbsolutePath());

    		parentWindow.setStatus("Generating right channel " + fType.toString() + " configuration file");
            try { sleep(1);} catch (InterruptedException ie) {}
            
//            generateRightChannelConfigurationFile(drcDir + "\\" + fType.toString() + samplingRate + "RightChannelTemplate.drc", drcDir + "\\" + fType.toString() + samplingRate + "RightChannel.drc");

    		parentWindow.setStatus("Generating right channel " + fType.toString() + " " + samplingRate + " filter");
            try { sleep(1);} catch (InterruptedException ie) {}

	        String rightOutputPcmName = "RightSpeaker" + samplingRate + fType.toString().toUpperCase() + ".pcm";
	        List<String> rightCommand = createDrcCommand(
	        		"RightSpeakerImpulseResponse" + samplingRate + ".pcm",
	        		rightOutputPcmName,
	        		fType.toString() + samplingRate + ".drc",
	        		impulseCenter);
	        resultsFile = getWorkFile("drcOutputRight" + samplingRate + fType.toString() + ".txt");
	    		runDrcCommand(rightCommand, new File(drcDir), resultsFile, "DRC right " + fType + " " + samplingRate);
	    		moveOutputFile(drcDir, rightOutputPcmName, convolverDir);
    		
    //		generateConvolverConfigFile(fType);
    		
    		generateWavFile(samplingRate, fType);

    	}
    	catch(Exception exc){
			LOGGER.warning("Failed running DRC " + fType + " " + samplingRate + ": " + exc.getMessage());
    		exc.printStackTrace();
	    		return false;
    	}		
	    
		return true;
    }

	private boolean supportsPostGenerationUpsample() {
		return "88200".equals(samplingRate) || "96000".equals(samplingRate);
	}

	private boolean upsampleGeneratedFiltersForSelectedTypes() {
		String destinationRate = "88200".equals(samplingRate) ? "176400" : "192000";
		boolean success = true;

		if (parentWindow.checkSelectedFilterType(FilterType.erb)) {
			success = upsampleFilterType(FilterType.erb, destinationRate) && success;
		}
		if (parentWindow.checkSelectedFilterType(FilterType.minimal)) {
			success = upsampleFilterType(FilterType.minimal, destinationRate) && success;
		}
		if (parentWindow.checkSelectedFilterType(FilterType.soft)) {
			success = upsampleFilterType(FilterType.soft, destinationRate) && success;
		}
		if (parentWindow.checkSelectedFilterType(FilterType.normal)) {
			success = upsampleFilterType(FilterType.normal, destinationRate) && success;
		}
		if (parentWindow.checkSelectedFilterType(FilterType.strong)) {
			success = upsampleFilterType(FilterType.strong, destinationRate) && success;
		}

		return success;
	}

	private boolean upsampleFilterType(FilterType fType, String destinationRate) {
		String convolverDir = options.getRoomCorrectionRootPath() + "\\ConvolverFilters\\";
		String typeSuffix = fType.toString().toUpperCase();
		String inputPath = convolverDir + "Stereo" + samplingRate + typeSuffix + ".wav";
		String outputPath = convolverDir + "Stereo" + destinationRate + typeSuffix + ".wav";

		parentWindow.setStatus("Upsampling " + typeSuffix + " filter to " + destinationRate);
		try { sleep(1);} catch (InterruptedException ie) {}

		SoxProcessor sp = new SoxProcessor(options);
		boolean success = sp.resampleWavFile(inputPath, outputPath, destinationRate);
		if (!success) {
			LOGGER.warning("Failed upsampling standard filter " + typeSuffix + " from " + samplingRate + " to " + destinationRate);
		}
		return success;
	}

	private List<String> createDrcCommand(String inputPcmName, String outputPcmName, String templateFileName, String impulseCenterValue) {
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
		if (impulseCenterValue != null) {
			command.add("--BCImpulseCenterMode=M");
			command.add("--BCImpulseCenter=" + impulseCenterValue);
		}
		command.add(templateFileName);
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
    
    private void generateWavFile(String samplingRate, FilterType fType) {
        String convolverDir = options.getRoomCorrectionRootPath() + "\\ConvolverFilters\\";
        String leftPcmFilePath = convolverDir + "LeftSpeaker" + samplingRate + fType.toString().toUpperCase() + ".pcm";
        String rightPcmFilePath = convolverDir + "RightSpeaker" + samplingRate + fType.toString().toUpperCase() + ".pcm";
        String outputWavFilePath = convolverDir + "Stereo" + samplingRate + fType.toString().toUpperCase() + ".wav";
       
        parentWindow.setStatus("Generating custom " + samplingRate + " stereo WAV file");
        try { sleep(1);} catch (InterruptedException ie) {}

        SoxProcessor sp = new SoxProcessor(options);
        sp.createWavFromRawPcm(leftPcmFilePath, rightPcmFilePath, outputWavFilePath, samplingRate, !options.isSavePcmFiles());
    }

    
    private void generateConvolverConfigFile(FilterType fType) {
		parentWindow.setStatus("Generating " + fType.toString() + " " + samplingRate + " convolver configuration");
        try { sleep(1);} catch (InterruptedException ie) {}

        String convolverDir = options.getRoomCorrectionRootPath() + "\\ConvolverFilters\\";
		try {
		      PrintWriter out = new PrintWriter(new FileWriter(convolverDir + "convolverConfig" + fType.toString().toUpperCase() + samplingRate + ".txt", false));
		      out.println(samplingRate + " 2 2 0");
		      out.println("0 0");
		      out.println("0 0");
		      out.println(convolverDir + "LeftSpeaker" + samplingRate + fType.toString().toUpperCase() + ".pcm");
		      out.println("0");
		      out.println("0.0");
		      out.println("0.0");
		      out.println(convolverDir + "RightSpeaker" + samplingRate + fType.toString().toUpperCase() + ".pcm");
		      out.println("0");
		      out.println("1.0");
		      out.println("1.0");
		      out.close();
		}
    	catch(Exception exc){
			LOGGER.warning("Failed running DRC " + fType + " " + samplingRate + ": " + exc.getMessage());
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

    
    
//    private void generateRightChannelConfigurationFile(String templateFileName, String outputFileName) {
//            try {
//           
//                // Create FileReader Object
//                FileReader inputFileReader   = new FileReader(templateFileName);
//                FileWriter outputFileReader  = new FileWriter(outputFileName, false);
//
//                // Create Buffered/PrintWriter Objects
//                BufferedReader inputStream   = new BufferedReader(inputFileReader);
//                PrintWriter    outputStream  = new PrintWriter(outputFileReader);
//
//                String inLine = null;
//
//                while ((inLine = inputStream.readLine()) != null) {
//                    if (inLine.startsWith("BCImpulseCenter =")) {
//                        outputStream.println("BCImpulseCenter = " + impulseCenter);
//                    }
//                    else {
//                        outputStream.println(inLine);
//                    }
//                }
//                outputStream.close();
//                inputStream.close();
//                
//            } catch (IOException e) {
//
//                System.out.println(e.getMessage());
//                e.printStackTrace();
//            }
//    }

    
	// Working version below for generating right channel DRC configuration file
//  private void runDrc(FilterType fType) {
//		String drcDir = options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample";
//		String convolverDir = options.getRoomCorrectionRootPath() + "\\ConvolverFilters";
//		
//		parentWindow.setStatus("Generating left channel " + fType.toString() + " " + samplingRate + " filter");
//      try { sleep(1);} catch (InterruptedException ie) {}
//
//		try {
//      	PrintWriter out = new PrintWriter(new FileWriter("drcWrapperRunDRCLeft" + fType + "_" + samplingRate +  ".bat", false));
//      	out.println("cd " + drcDir);
//      	out.println("drc.exe --BCInFile=LeftSpeakerImpulseResponse" + samplingRate + ".pcm --PSOutFile=LeftSpeaker" + samplingRate + fType.toString().toUpperCase() + ".pcm " + fType.toString() + samplingRate + ".drc");
//      	out.println("move /y LeftSpeaker" + samplingRate + fType.toString().toUpperCase() + ".pcm " + convolverDir);
//      	out.close();
//      	
//      	String resultsFileName = "drcOutputLeft" + samplingRate + fType.toString() + ".txt";
//  		String command = "cmd.exe /c \"drcWrapperRunDRCLeft" + fType.toString() + "_" + samplingRate + ".bat 1>" + resultsFileName + " 2>&1\"";
//  		Runtime rt = Runtime.getRuntime();
//  		Process p = rt.exec(command);
//  		p.waitFor();
//
//  		parentWindow.setStatus("Parsing results to find center");
//          try { sleep(1);} catch (InterruptedException ie) {}
//          impulseCenter = parseResultsFileForImpulseCenter(resultsFileName);
//
//  		parentWindow.setStatus("Generating right channel " + fType.toString() + " configuration file");
//          try { sleep(1);} catch (InterruptedException ie) {}
//          
//          generateRightChannelConfigurationFile(drcDir + "\\" + fType.toString() + samplingRate + "RightChannelTemplate.drc", drcDir + "\\" + fType.toString() + samplingRate + "RightChannel.drc");
//
//  		parentWindow.setStatus("Generating right channel " + fType.toString() + " " + samplingRate + " filter");
//          try { sleep(1);} catch (InterruptedException ie) {}
//
//      	out = new PrintWriter(new FileWriter("drcWrapperRunDRCRight" + fType + "_" + samplingRate +  ".bat", false));
//      	out.println("cd " + drcDir);
//      	out.println("drc.exe --BCInFile=RightSpeakerImpulseResponse" + samplingRate + ".pcm --PSOutFile=RightSpeaker" + samplingRate + fType.toString().toUpperCase() + ".pcm " + fType.toString() + samplingRate + "RightChannel.drc");
//      	out.println("move /y RightSpeaker" + samplingRate + fType.toString().toUpperCase() + ".pcm " + convolverDir);
//      	out.close();
//      	resultsFileName = "drcOutputRight" + samplingRate + fType.toString() + ".txt";
//  		command = "cmd.exe /c \"drcWrapperRunDRCRight" + fType + "_" + samplingRate + ".bat 1>" + resultsFileName + " 2>&1\"";
//  		rt = Runtime.getRuntime();
//  		p = rt.exec(command);
//  		p.waitFor();
//  		
//  		generateConvolverConfigFile(fType);
//  	}
//  	catch(Exception exc){
//  		exc.printStackTrace();
//  	}		
//  	
//  }

}
