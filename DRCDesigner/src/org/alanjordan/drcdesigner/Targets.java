package org.alanjordan.drcdesigner;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Targets {
    public enum TargetNames {Flat, BK, BK2, BK3, DeepTilt, DeepTilt2};
    
    private FrequencyAmplitudePoints flat;
    private FrequencyAmplitudePoints bk;
    private FrequencyAmplitudePoints bk2;
    private FrequencyAmplitudePoints bk3;
    private FrequencyAmplitudePoints deepTilt;
    private FrequencyAmplitudePoints deepTilt2;
    
    private Options options;
    
    public Targets(Options options) {
    	createTargets();
    	this.options = options;
    }
    
	public void writeTargetPointsFile(int sampleRate) {
		ArrayList<FrequencyAmplitudePoint> faPoints = options.getPoints().getPoints();
		int frequency = 0;
		try {
	    	PrintWriter out = new PrintWriter(new FileWriter(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\DRCDesignerCustomizedPoints.txt", false));
	    	for (int counter = 0; counter < faPoints.size(); counter++) {
	    		if (counter == 0 )
	    			frequency = 0;
	    		else if (counter == faPoints.size() - 2) 
	    			frequency = getSemiFinalRate(sampleRate);
	    		else if (counter == faPoints.size() - 1)
	    			frequency = getFinalRate(sampleRate);
	    		else
	    			frequency = (int)faPoints.get(counter).getFrequency();
	    		out.println(frequency + " " + faPoints.get(counter).getAmplitude());
	    	}
	    	out.close();
		}
    	catch (Exception e) {
    		System.out.println(e.getMessage());
    		System.out.println(e.getStackTrace());
    	}
	}

	private int getSemiFinalRate(int sampleRate) {
		int returnValue = 21000;
		switch (sampleRate) {
			case 44100:
				returnValue = 21000;
				break;
			case 48000:
				returnValue = 22000;
				break;
			case 88200:
				returnValue = 32050;
				break;
			case 96000:
				returnValue = 34000;
				break;
		}
		return returnValue;
	}

	private int getFinalRate(int sampleRate) {
		int returnValue = 22050;
		switch (sampleRate) {
			case 44100:
				returnValue = 22050;
				break;
			case 48000:
				returnValue = 24000;
				break;
			case 88200:
				returnValue = 44100;
				break;
			case 96000:
				returnValue = 48000;
				break;
		}
		return returnValue;
	}

    public FrequencyAmplitudePoints getTarget(TargetNames targetName) {
    	FrequencyAmplitudePoints returnValue = null;
    	switch(targetName) {
    		case Flat:
    			returnValue = flat;
    			break;
    		case BK:
    			returnValue = bk;
    			break;
    		case BK2:
    			returnValue = bk2;
    			break;
    		case BK3:
    			returnValue = bk3;
    			break;
    		case DeepTilt:
    			returnValue = deepTilt;
    			break;
    		case DeepTilt2:
    			returnValue = deepTilt2;
    			break;
    	}
    	return returnValue;
    }

    private void createTargets() {
    	//Flat target
    	flat = new FrequencyAmplitudePoints();
    	flat.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(0, -30.0));
    	flat.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(10, -10.0));
    	flat.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(20, 0.00));
    	flat.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(18000, 0.00));
    	flat.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(20000, 0.00));
    	flat.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(21000, -3.00));
    	flat.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(22050, -30.0));
    	flat.trimToSize();

    	//bk target
    	bk = new FrequencyAmplitudePoints();
    	bk.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(0, -30.0));
    	bk.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(10, -10.0));
    	bk.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(20, 0.00));
    	bk.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(400, 0.00));
    	bk.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(12800, -5.00));
    	bk.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(20000, -6.00));
    	bk.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(21000, -10.0));
    	bk.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(22050, -30.0));
    	bk.trimToSize();

    	//bk2 target
    	bk2 = new FrequencyAmplitudePoints();
    	bk2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(0, -30.0));
    	bk2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(10, -10.0));
    	bk2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(20, 0.00));
    	bk2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(200, 0.00));
    	bk2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(12800, -3.00));
    	bk2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(20000, -3.50));
    	bk2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(21000, -10.0));
    	bk2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(22050, -30.0));
    	bk2.trimToSize();

    	//bk3 target
    	bk3 = new FrequencyAmplitudePoints();
    	bk3.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(0, -30.0));
    	bk3.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(10, -10.0));
    	bk3.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(20, 0.00));
    	bk3.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(100, 0.00));
    	bk3.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(12800, -3.50));
    	bk3.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(20000, -4.00));
    	bk3.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(21000, -10.0));
    	bk3.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(22050, -30.0));
    	bk3.trimToSize();

    	//deepTilt target
    	deepTilt = new FrequencyAmplitudePoints();
    	deepTilt.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(0, -30.0));
    	deepTilt.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(10, -10.0));
    	deepTilt.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(20, 0.00));
    	deepTilt.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(40, 0.00));
    	deepTilt.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(200, -1.00));
    	deepTilt.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(500, -1.00));
    	deepTilt.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(20000, -2.00));
    	deepTilt.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(21000, -10.0));
    	deepTilt.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(22050, -30.0));
    	deepTilt.trimToSize();

    	//deepTilt2 target
    	deepTilt2 = new FrequencyAmplitudePoints();
    	deepTilt2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(0, -30.0));
    	deepTilt2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(10, -10.0));
    	deepTilt2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(20, 0.00));
    	deepTilt2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(40, 0.00));
    	deepTilt2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(200, -2.00));
    	deepTilt2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(500, -2.00));
    	deepTilt2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(20000, -4.00));
    	deepTilt2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(21000, -10.0));
    	deepTilt2.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(22050, -30.0));
    	deepTilt2.trimToSize();
    }
}
