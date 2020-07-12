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

public class Options implements java.io.Serializable {

    private File roomCorrectionRoot; //Root folder of RoomCorrectionCustomized folder
    private String roomCorrectionRootPath; //String representation of above variable because can't always serialize File object
    public enum InterfaceDriverType {ASIO, DIRECT_SOUND};
    private InterfaceDriverType driverType; 
    private boolean useMicCompensationFile;
    private File micCompensationFile;
    private String micCompensationFilePath;
    private FrequencyAmplitudePoints points;
    private boolean savePcmFiles;
    
    public void initOptions() {
        try {
            FileInputStream f_in = new FileInputStream("DRCDesignerOptions.data");
            ObjectInputStream obj_in = new ObjectInputStream (f_in);
            Object obj = obj_in.readObject();

            if (obj instanceof Options) {
                Options tempOptions = (Options)obj;


                if (tempOptions.getRoomCorrectionRootPath() != null) {
                    this.roomCorrectionRoot = new File(tempOptions.getRoomCorrectionRootPath()); 
                    this.roomCorrectionRootPath = roomCorrectionRoot.getPath();
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
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            roomCorrectionRoot = null;
            roomCorrectionRootPath = null;
        }
        
    }

    
    public void saveOptions() {
        //set objects in a manner that will be serializable.  File object at root of drive
        // can't be serialized and unserialized.

        // rely on string instead of File object
        try {
            roomCorrectionRootPath = roomCorrectionRoot.getPath();
            roomCorrectionRoot = null;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
            roomCorrectionRootPath = null;
            roomCorrectionRoot = null;
        }
        
        try {
            FileOutputStream f_out = new FileOutputStream("DRCDesignerOptions.data");
            ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
            obj_out.writeObject(this);
        }
        catch (FileNotFoundException fe) {
            System.out.println(fe.getMessage());
            System.out.println(fe.getStackTrace());
        }
        catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            System.out.println(ioe.getStackTrace());
        }
    }
    
    public String getRoomCorrectionRootPath() {
        return roomCorrectionRootPath;
    }

    public void setRoomCorrectionRoot(File roomCorrectionRoot) {
        this.roomCorrectionRoot = roomCorrectionRoot;
        this.roomCorrectionRootPath = roomCorrectionRoot.getPath();
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


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
