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
import java.util.logging.Logger;

public class Options implements java.io.Serializable {
    private static final Logger LOGGER = AppLogger.getLogger();

    private File roomCorrectionRoot; //Root folder of RoomCorrectionCustomized folder
    private String roomCorrectionRootPath; //String representation of above variable because can't always serialize File object
    public enum InterfaceDriverType {ASIO, DIRECT_SOUND};
    private InterfaceDriverType driverType; 
    private boolean useMicCompensationFile;
    private File micCompensationFile;
    private String micCompensationFilePath;
    private FrequencyAmplitudePoints points;
    private FrequencyAmplitudePoints leftChannelResponsePoints;
    private FrequencyAmplitudePoints rightChannelResponsePoints;
    private String responseSmoothingPreset;
    private boolean savePcmFiles;
    private boolean upsampleStandardFilters;
    private boolean upsampleCustomFilters;

    private static final String APP_NAME = "DRCDesigner";

    public static File getAppDataDirectory() {
        String appData = System.getenv("APPDATA");
        File optionsDir;

        if (appData != null && appData.trim().length() > 0) {
            optionsDir = new File(appData, APP_NAME);
        }
        else {
            optionsDir = new File(System.getProperty("user.home"), ".drcdesigner");
        }

        if (!optionsDir.exists()) {
            optionsDir.mkdirs();
        }

        return optionsDir;
    }

    private boolean hasExpectedToolLayout(File root) {
        if (root == null || !root.exists() || !root.isDirectory()) {
            return false;
        }

        return new File(root, "Rec_imp.win32").isDirectory()
                && new File(root, "sox-14.3.2").isDirectory()
                && new File(root, "drc-3.2.3").isDirectory();
    }

    private File normalizeRoomCorrectionRoot(File selectedRoot) {
        if (selectedRoot == null) {
            return null;
        }

        File cursor = selectedRoot;
        while (cursor != null) {
            if (hasExpectedToolLayout(cursor)) {
                return cursor;
            }

            File appSubdir = new File(cursor, "app");
            if (hasExpectedToolLayout(appSubdir)) {
                return appSubdir;
            }

            cursor = cursor.getParentFile();
        }

        return selectedRoot;
    }

    private File getOptionsFile() {
        return new File(getAppDataDirectory(), "DRCDesignerOptions.data");
    }
    
    public void initOptions() {
        try {
            File optionsFile = getOptionsFile();
            File legacyFile = new File("DRCDesignerOptions.data");
            File loadFile = optionsFile.exists() ? optionsFile : legacyFile;
            LOGGER.info("Loading options from: " + loadFile.getAbsolutePath());

            try (FileInputStream f_in = new FileInputStream(loadFile);
                 ObjectInputStream obj_in = new ObjectInputStream(f_in)) {
                Object obj = obj_in.readObject();

                if (obj instanceof Options) {
                    Options tempOptions = (Options)obj;


                    if (tempOptions.getRoomCorrectionRootPath() != null) {
                        this.setRoomCorrectionRoot(new File(tempOptions.getRoomCorrectionRootPath()));
                    }
                    else {
                        this.roomCorrectionRoot = null;
                        this.roomCorrectionRootPath = null;
                    }

                    if (tempOptions.isUseMicCompensationFile())
		        	    this.setUseMicCompensationFile(true);
                    else
		        	    this.setUseMicCompensationFile(false);

                    if (tempOptions.getMicCompensationFilePath() != null) {
                        this.micCompensationFile = new File(tempOptions.getMicCompensationFilePath());
                        this.micCompensationFilePath = micCompensationFile.getPath();
                    }
                    else {
                        this.micCompensationFile = null;
                        this.micCompensationFilePath = null;
                    }

                    if (tempOptions.isSavePcmFiles())
                        this.setSavePcmFiles(true);
                    else
                        this.setSavePcmFiles(false);

                    this.setUpsampleStandardFilters(tempOptions.isUpsampleStandardFilters());
                    this.setUpsampleCustomFilters(tempOptions.isUpsampleCustomFilters());

                    if (tempOptions.getDriverType() != null) {
		        	    this.driverType = tempOptions.getDriverType();
                    }
                    else {
		        	    this.driverType = null;
                    }

                    if (tempOptions.getPoints() != null) {
		        	    this.points = tempOptions.getPoints();
                    }
                    else {
		        	    this.points = null;
                    }

                    if (tempOptions.getResponseSmoothingPreset() != null) {
                        this.responseSmoothingPreset = tempOptions.getResponseSmoothingPreset();
                    }
                    else {
                        this.responseSmoothingPreset = ResponseCurveAnalysisUtil.DisplaySmoothingPreset.OCTAVE_12.getId();
                    }
                }
            }
        }
        catch (Exception e) {
            LOGGER.warning("Unable to load options: " + e.getMessage());
            roomCorrectionRoot = null;
            roomCorrectionRootPath = null;
        }

		if (this.responseSmoothingPreset == null) {
			this.responseSmoothingPreset = ResponseCurveAnalysisUtil.DisplaySmoothingPreset.OCTAVE_12.getId();
		}
        
    }

    
    public void saveOptions() {
        //set objects in a manner that will be serializable.  File object at root of drive
        // can't be serialized and unserialized.

        // rely on string instead of File object
        if (roomCorrectionRoot != null) {
            roomCorrectionRootPath = roomCorrectionRoot.getPath();
        }
        
        try {
            File optionsFile = getOptionsFile();
            LOGGER.info("Saving options to: " + optionsFile.getAbsolutePath());
            try (FileOutputStream f_out = new FileOutputStream(optionsFile);
                 ObjectOutputStream obj_out = new ObjectOutputStream(f_out)) {
                obj_out.writeObject(this);
            }
        }
        catch (FileNotFoundException fe) {
            LOGGER.warning("Options file path not found: " + fe.getMessage());
        }
        catch (IOException ioe) {
            LOGGER.warning("Unable to save options: " + ioe.getMessage());
        }
    }
    
    public String getRoomCorrectionRootPath() {
        return roomCorrectionRootPath;
    }

    public void setRoomCorrectionRoot(File roomCorrectionRoot) {
        File normalized = normalizeRoomCorrectionRoot(roomCorrectionRoot);
        this.roomCorrectionRoot = normalized;
        this.roomCorrectionRootPath = normalized.getPath();
    }
    
    public File getRoomCorrectionRoot() {
        return roomCorrectionRoot;
    }


	public void setDriverType(InterfaceDriverType driverType) {
		this.driverType = driverType;
	}


	public InterfaceDriverType getDriverType() {
		return driverType;
	}


	public void setUseMicCompensationFile(boolean useMicCompensationFile) {
		this.useMicCompensationFile = useMicCompensationFile;
	}


    public void setSavePcmFiles(boolean savePcmFiles) {
        this.savePcmFiles = savePcmFiles;
    }


    public boolean isUseMicCompensationFile() {
		return useMicCompensationFile;
	}


	public void setMicCompensationFile(File micCompensationFile) {
		this.micCompensationFile = micCompensationFile;
        this.micCompensationFilePath = micCompensationFile.getPath();
	}


    public boolean isSavePcmFiles() {
        return savePcmFiles;
    }

        public void setUpsampleStandardFilters(boolean upsampleStandardFilters) {
		this.upsampleStandardFilters = upsampleStandardFilters;
	}

        public boolean isUpsampleStandardFilters() {
		return upsampleStandardFilters;
	}

        public void setUpsampleCustomFilters(boolean upsampleCustomFilters) {
		this.upsampleCustomFilters = upsampleCustomFilters;
	}

        public boolean isUpsampleCustomFilters() {
		return upsampleCustomFilters;
	}



    public File getMicCompensationFile() {
		return micCompensationFile;
	}

    public String getMicCompensationFilePath() {
        return micCompensationFilePath;
    }


	public void setPoints(FrequencyAmplitudePoints points) {
		this.points = points;
	}


	public FrequencyAmplitudePoints getPoints() {
		return points;
	}

    public void setLeftChannelResponsePoints(FrequencyAmplitudePoints leftChannelResponsePoints) {
        this.leftChannelResponsePoints = leftChannelResponsePoints;
    }

    public FrequencyAmplitudePoints getLeftChannelResponsePoints() {
        return leftChannelResponsePoints;
    }

    public void setRightChannelResponsePoints(FrequencyAmplitudePoints rightChannelResponsePoints) {
        this.rightChannelResponsePoints = rightChannelResponsePoints;
    }

    public FrequencyAmplitudePoints getRightChannelResponsePoints() {
        return rightChannelResponsePoints;
    }

    public void setResponseSmoothingPreset(String responseSmoothingPreset) {
        this.responseSmoothingPreset = responseSmoothingPreset;
    }

    public String getResponseSmoothingPreset() {
        if (responseSmoothingPreset == null) {
            responseSmoothingPreset = ResponseCurveAnalysisUtil.DisplaySmoothingPreset.OCTAVE_12.getId();
        }
        return responseSmoothingPreset;
    }


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
