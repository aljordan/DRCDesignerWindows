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

//import java.io.FileWriter;
//import java.io.PrintWriter;

public class SoxProcessor {
    private Options options;

    public SoxProcessor(Options options) {
        this.options = options;
    }
   
    public void createWavFromRawPcm(String leftPcmFilePath, String rightPcmFilePath, String outputWavFilePath, String samplingRate, boolean deletePcmFiles) {
        String soxExecutable = options.getRoomCorrectionRootPath() + "\\sox-14.3.2\\sox.exe";
        
        try {
//            PrintWriter out = new PrintWriter(new FileWriter("runSox.bat", false));
    //            out.println(...);
//            out.close();

            ProcessBuilder pb = new ProcessBuilder(
                soxExecutable,
                "-M",
                "-t", "raw",
                "-b", "32",
                "-c", "1",
                "-f",
                "-r", samplingRate,
                leftPcmFilePath,
                "-t", "raw",
                "-b", "32",
                "-c", "1",
                "-f",
                "-r", samplingRate,
                rightPcmFilePath,
                "-t", "wav",
                "-b", "32",
                "-f",
                "-r", samplingRate,
                outputWavFilePath);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();
            
            if (deletePcmFiles) {
            	File left = new File(leftPcmFilePath);
            	if (left.exists()) 
            		left.delete();
            	File right = new File(rightPcmFilePath);
            	if (right.exists()) 
            		right.delete();
            }
           
        }
        catch(Exception exc){
            exc.printStackTrace();
        }       


    }

    public boolean resampleWavFile(String inputWavFilePath, String outputWavFilePath, String outputSamplingRate) {
        String soxExecutable = options.getRoomCorrectionRootPath() + "\\sox-14.3.2\\sox.exe";

        try {
            ProcessBuilder pb = new ProcessBuilder(
                soxExecutable,
                "-V1",
                inputWavFilePath,
                "-b", "32",
                "-e", "floating-point",
                outputWavFilePath,
                "rate", "-v", "-L", outputSamplingRate);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            int exitCode = p.waitFor();
            return exitCode == 0;
        }
        catch(Exception exc) {
            exc.printStackTrace();
            return false;
        }
    }

}