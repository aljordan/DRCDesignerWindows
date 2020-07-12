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

import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;

import javax.swing.JSlider;
import javax.swing.JButton;
import javax.swing.JComboBox;

public class CustomizedFilterPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private Options options = null;
	private JLabel lblLowFrequencyStrength = null;
	private JLabel lblMidFrequencyStrength = null;
	private JLabel lblHighFrequencyStrength = null;
	private JSlider sldLowFrequencyStrength = null;
	private JSlider sldMidFrequencyStrength = null;
	private JSlider sldHighFrequencyStrength = null;
	private JLabel lblLowFrequencyValue = null;
	private JLabel lblMidFrequencyValue = null;
	private JLabel lblHighFrequencyValue = null;
	private JButton btnGenerateCustomFilter = null;  //  @jve:decl-index=0:visual-constraint="440,236"
	private JLabel lblMpUpperWindow = null;
	private JLabel lblMpLowerWindow = null;
	private JLabel lblMpWindowExponent = null;
	private JLabel lblEpLowerWindow = null;
	private JLabel lblEpWindowExponent = null;
	private JLabel lblRtWindowExponent = null;
	private JLabel lblLowFrequencyPreEchoControl = null;
	private JSlider sldLowFrequencyPreEchoControlStrength = null;
	private JLabel lblLowFrequencyPreEchoValue = null;
	private JLabel lblEpUpperWindow = null;
	private ConfigurationEntries configEntries;
	private JComboBox cboSamplingRate = null;
	private JLabel lblSamplingRate = null;
	private TemplateLoader templateLoader = null;
	private JLabel lblMidFrequencyPreEchoControl = null;
	private JSlider sldMidFrequencyPreEchoControlStrength = null;
	private JLabel lblMidFrequencyPreEchoValue = null;
	private JLabel lblMaxCorrectionBoost = null;
	private JSlider sldMaxCorrectionBoost = null;
	private JLabel lblMaxCorrectionBoostValue = null;
	private JLabel lblPeakLimitingMaxGain = null;
	private JLabel lblStatus = null;
	
	/**
	 * This is the default constructor
	 */
	public CustomizedFilterPanel(Options options) {
		super();
		initialize();
		configEntries = new ConfigurationEntries();
		this.options = options;
		initializeSamplingRates();
		sldLowFrequencyStrength.setValue(0);
		sldMidFrequencyStrength.setValue(0);
		sldHighFrequencyStrength.setValue(0);
		sldLowFrequencyPreEchoControlStrength.setValue(0);
		//Todo: crappy trick to get screen painted correctly below.   Fix it.
		cboSamplingRate.setSelectedIndex(1);
		cboSamplingRate.setSelectedIndex(0);
		//cboSamplingRate.setSelectedItem(cboSamplingRate.getItemAt(0));
	}

    public void setStatus(String status) {
        lblStatus.setText("Status: " + status);
    }
	
    public void enableDisableGenerateFiltersButton(boolean enable) {
    	btnGenerateCustomFilter.setEnabled(enable);
    }

	private void runDRC() {
	    CustomFilterDrcProcessor drcp = new CustomFilterDrcProcessor(options, this, cboSamplingRate.getSelectedItem().toString(), configEntries);
	    
	    drcp.start();
	}

	public void loadTemplate(ConfigurationEntries entries) {
		configEntries = entries;
		int value;
		value = calculateSliderValueFromBoundsAndValue(16.0F,32.0F, Float.parseFloat(entries.getValue(ConfigurationEntries.EntryNames.MPLowerWindow)) / Float.parseFloat(entries.getValue(ConfigurationEntries.EntryNames.EPLowerWindow)));
		sldLowFrequencyPreEchoControlStrength.setValue(value);

		value = calculateSliderValueFromBoundsAndValue(16384.0F,104448.0F,Float.parseFloat(entries.getValue(ConfigurationEntries.EntryNames.MPLowerWindow)));
		sldLowFrequencyStrength.setValue(value);
		
		value = calculateSliderValueFromBoundsAndValue(0.5F,2.0F,Float.parseFloat(entries.getValue(ConfigurationEntries.EntryNames.MPWindowExponent)));
		sldMidFrequencyStrength.setValue(value);
		
		value = calculateInverseSliderValueFromBoundsAndValue(0.5F,2.0F,Float.parseFloat(entries.getValue(ConfigurationEntries.EntryNames.EPWindowExponent)));
		sldMidFrequencyPreEchoControlStrength.setValue(value);

		value = calculateSliderValueFromBoundsAndValue(22.0F,122.0F,Float.parseFloat(entries.getValue(ConfigurationEntries.EntryNames.MPUpperWindow)));
		sldHighFrequencyStrength.setValue(value);		
		
		value = calculateSliderValueFromBoundsAndValue(1.0F, 4.0F,Float.parseFloat(entries.getValue(ConfigurationEntries.EntryNames.PLMaxGain)));
		sldMaxCorrectionBoost.setValue(value);
	}
	
	public DrcProcessor.SamplingRate getSamplingRate() {
		
		if (((String)cboSamplingRate.getSelectedItem()).equalsIgnoreCase("44100"))
			return DrcProcessor.SamplingRate._44100;
		else if (((String)cboSamplingRate.getSelectedItem()).equalsIgnoreCase("48000"))
			return DrcProcessor.SamplingRate._48000;
		else if (((String)cboSamplingRate.getSelectedItem()).equalsIgnoreCase("88200"))
			return DrcProcessor.SamplingRate._88200;
		else if (((String)cboSamplingRate.getSelectedItem()).equalsIgnoreCase("96000"))
			return DrcProcessor.SamplingRate._96000;
		else
			return null;
	}
	
	private int calculateMpUpperWindow(int sliderPercentage) {
		float lowerBound = 22.0F;
		float upperBound = 122.0F;
		return Math.round(computeValueFromSlider(sliderPercentage, lowerBound, upperBound));
	}
	
	private void enableDisablePanel() {
		File leftImpulseResponse = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\LeftSpeakerImpulseResponse" + cboSamplingRate.getSelectedItem() + ".pcm");
		boolean leftExists = leftImpulseResponse.exists(); 
		File rightImpulseResponse = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\RightSpeakerImpulseResponse" + cboSamplingRate.getSelectedItem() + ".pcm");
		boolean rightExists = rightImpulseResponse.exists();		
		this.enable((leftExists && rightExists));
		templateLoader.enable((leftExists && rightExists));
	}

	private void initializeSamplingRates() {
		DefaultComboBoxModel rateModel = (DefaultComboBoxModel)cboSamplingRate.getModel();
		rateModel.removeAllElements();
		rateModel.addElement("44100");
		rateModel.addElement("48000");
		rateModel.addElement("88200");
		rateModel.addElement("96000");
	}

	private float calculateMaxCorrectionGain(double plMaxGain) {
		return (float)(20 * Math.log10(plMaxGain));
	}
	
	private int calculateMpLowerWindow (int sliderPercentage) {
		float lowerBound = 16384.0F;
		float upperBound = 104448.0F;
		return Math.round(computeValueFromSlider(sliderPercentage, lowerBound, upperBound));	
	}
	
	private int calculateExcessPhaseLowerWindow(int mpLowSliderPercentage, int epLowSliderPercentage) {
		int mpLowerWindow = calculateMpLowerWindow(mpLowSliderPercentage);
		float lowerBound = 16.0F;
		float upperBound = 32.0F;
		int divisor = Math.round(computeValueFromSlider(epLowSliderPercentage, lowerBound, upperBound));
		float result = mpLowerWindow / divisor;
		return Math.round(result);
	}
	
	private float calculateMpWindowExponent(int sliderPercentage) {
		float lowerBound = 0.50F;
		float upperBound = 2.0F;
		return Math.round(computeValueFromSlider(sliderPercentage, lowerBound, upperBound)*100.0F) / 100.0F;
	}
	
	private float calculateEpWindowExponent(int sliderPercentage) {
		float lowerBound = 0.50F;
		float upperBound = 2.0F;
		return Math.round(computeInverseValueFromSlider(sliderPercentage, lowerBound, upperBound)*100.0F) / 100.0F;
	}
	
	/**
	 * 
	 * @param sliderPercentage = the actual value of the slider
	 * @param lowerBound = lowest number in range of allowable values
	 * @param upperBound = highest number in range of allowable values
	 * @return value of the number in the given range given the input of a slider value from 0 to 100
	 */
	private float computeValueFromSlider(int sliderPercentage, float lowerBound, float upperBound) {
		float range = upperBound - lowerBound;
		float step = range / 100;
		return (sliderPercentage * step) + (lowerBound);		
	}
	
	private float computeInverseValueFromSlider(int sliderPercentage, float lowerBound, float upperBound) {
		int inverseSliderPercentage = 100 - sliderPercentage;
		float range = upperBound - lowerBound;
		float step = range / 100;
		return (inverseSliderPercentage * step) + (lowerBound);	
	}
	
	/**
	 * 
	 * @param lowerBound = lowest number in range of allowable values
	 * @param upperBound = highest number in range of allowable values
	 * @param value = value to be converted to slider percentage 
	 * @return slider control position for slider with a range of 0 to 100
	 */
	private int calculateSliderValueFromBoundsAndValue(float lowerBound, float upperBound, float value) {
        float range = upperBound - lowerBound;
        float rangedValue = value - lowerBound;
        return Math.round((rangedValue / range) * 100);
	}
	
	private int calculateInverseSliderValueFromBoundsAndValue(float lowerBound, float upperBound, float value) {
        float range = upperBound - lowerBound;
        float rangedValue = value - lowerBound;
        return 100 - (Math.round((rangedValue / range) * 100));
	}
	
	public void enable(boolean enable) {
		lblLowFrequencyStrength.setEnabled(enable);
		lblMidFrequencyStrength.setEnabled(enable);
		lblHighFrequencyStrength.setEnabled(enable);
		sldLowFrequencyStrength.setEnabled(enable);
		sldMidFrequencyStrength.setEnabled(enable);
		sldHighFrequencyStrength.setEnabled(enable);
		lblLowFrequencyValue.setEnabled(enable);
		lblMidFrequencyValue.setEnabled(enable);
		lblHighFrequencyValue.setEnabled(enable);
		lblMpLowerWindow.setEnabled(enable);
		lblMpWindowExponent.setEnabled(enable);
		lblMpUpperWindow.setEnabled(enable);
		lblEpUpperWindow.setEnabled(enable);
		lblEpLowerWindow.setEnabled(enable);
		lblEpWindowExponent.setEnabled(enable);
		lblRtWindowExponent.setEnabled(enable);
		lblLowFrequencyPreEchoControl.setEnabled(enable);
		lblLowFrequencyPreEchoValue.setEnabled(enable);
		lblMidFrequencyPreEchoControl.setEnabled(enable);
		lblMidFrequencyPreEchoValue.setEnabled(enable);
		sldLowFrequencyPreEchoControlStrength.setEnabled(enable);
		sldMidFrequencyPreEchoControlStrength.setEnabled(enable);
		lblPeakLimitingMaxGain.setEnabled(enable);
		lblMaxCorrectionBoost.setEnabled(enable);
		lblMaxCorrectionBoostValue.setEnabled(enable);
		sldMaxCorrectionBoost.setEnabled(enable);
		btnGenerateCustomFilter.setEnabled(enable);
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
		gridBagConstraints19.gridx = 0;
		gridBagConstraints19.anchor = GridBagConstraints.WEST;
		gridBagConstraints19.gridwidth = 3;
		gridBagConstraints19.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints19.gridy = 12;
		lblStatus = new JLabel();
		lblStatus.setText("Status:");
		GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
		gridBagConstraints8.gridx = 2;
		gridBagConstraints8.gridy = 7;
		GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
		gridBagConstraints7.gridx = 2;
		gridBagConstraints7.insets = new Insets(0, 10, 10, 10);
		gridBagConstraints7.anchor = GridBagConstraints.NORTH;
		gridBagConstraints7.gridy = 10;
		lblMaxCorrectionBoostValue = new JLabel();
		lblMaxCorrectionBoostValue.setText("dB");
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.fill = GridBagConstraints.NONE;
		gridBagConstraints6.gridy = 9;
		gridBagConstraints6.weightx = 1.0;
		gridBagConstraints6.insets = new Insets(10, 10, 10, 10);
		gridBagConstraints6.gridx = 2;
		GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
		gridBagConstraints51.gridx = 2;
		gridBagConstraints51.insets = new Insets(10, 10, 0, 10);
		gridBagConstraints51.anchor = GridBagConstraints.SOUTH;
		gridBagConstraints51.gridy = 8;
		lblMaxCorrectionBoost = new JLabel();
		lblMaxCorrectionBoost.setText("Maximum Correction Boost");
		GridBagConstraints gridBagConstraints42 = new GridBagConstraints();
		gridBagConstraints42.gridx = 1;
		gridBagConstraints42.insets = new Insets(0, 10, 10, 10);
		gridBagConstraints42.gridy = 10;
		lblMidFrequencyPreEchoValue = new JLabel();
		lblMidFrequencyPreEchoValue.setText("%");
		GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
		gridBagConstraints33.fill = GridBagConstraints.NONE;
		gridBagConstraints33.gridy = 9;
		gridBagConstraints33.weightx = 1.0;
		gridBagConstraints33.anchor = GridBagConstraints.NORTH;
		gridBagConstraints33.insets = new Insets(10, 10, 10, 10);
		gridBagConstraints33.gridx = 1;
		GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
		gridBagConstraints26.gridx = 1;
		gridBagConstraints26.anchor = GridBagConstraints.SOUTH;
		gridBagConstraints26.insets = new Insets(10, 10, 0, 10);
		gridBagConstraints26.gridy = 8;
		lblMidFrequencyPreEchoControl = new JLabel();
		lblMidFrequencyPreEchoControl.setText("Mid Frequency Pre-Echo Control");
		GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
		gridBagConstraints18.gridx = 0;
		gridBagConstraints18.gridwidth = 2;
		gridBagConstraints18.anchor = GridBagConstraints.WEST;
		gridBagConstraints18.gridy = 11;
		GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
		gridBagConstraints25.gridx = 0;
		gridBagConstraints25.anchor = GridBagConstraints.WEST;
		gridBagConstraints25.gridy = 0;
		lblSamplingRate = new JLabel();
		lblSamplingRate.setText("Sampling Rate");
		GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
		gridBagConstraints17.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints17.gridy = 1;
		gridBagConstraints17.weightx = 1.0;
		gridBagConstraints17.anchor = GridBagConstraints.WEST;
		gridBagConstraints17.gridx = 0;
		this.setSize(800, 600);
		this.setLayout(new GridBagLayout());

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.insets = new Insets(10, 10, 0, 10);
		gridBagConstraints.anchor = GridBagConstraints.SOUTH;
		gridBagConstraints.gridy = 2;

		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.insets = new Insets(10, 10, 0, 10);
		gridBagConstraints1.anchor = GridBagConstraints.SOUTH;
		gridBagConstraints1.gridy = 2;

		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 2;
		gridBagConstraints2.insets = new Insets(10, 10, 0, 10);
		gridBagConstraints2.anchor = GridBagConstraints.SOUTH;
		gridBagConstraints2.gridy = 2;

		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.fill = GridBagConstraints.NONE;
		gridBagConstraints3.gridy = 3;
		gridBagConstraints3.weightx = 1.0;
		gridBagConstraints3.insets = new Insets(10, 10, 10, 10);
		gridBagConstraints3.anchor = GridBagConstraints.NORTH;
		gridBagConstraints3.gridx = 0;

		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.fill = GridBagConstraints.NONE;
		gridBagConstraints4.gridy = 3;
		gridBagConstraints4.weightx = 1.0;
		gridBagConstraints4.insets = new Insets(10, 10, 10, 10);
		gridBagConstraints4.anchor = GridBagConstraints.NORTH;
		gridBagConstraints4.gridx = 1;

		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
		gridBagConstraints5.fill = GridBagConstraints.NONE;
		gridBagConstraints5.gridy = 3;
		gridBagConstraints5.weightx = 1.0;
		gridBagConstraints5.insets = new Insets(10, 10, 10, 10);
		gridBagConstraints5.anchor = GridBagConstraints.NORTH;
		gridBagConstraints5.gridx = 2;

		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.gridx = 0;
		gridBagConstraints11.insets = new Insets(0, 10, 10, 10);
		gridBagConstraints11.anchor = GridBagConstraints.NORTH;
		gridBagConstraints11.gridy = 4;

		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		gridBagConstraints12.gridx = 2;
		gridBagConstraints12.anchor = GridBagConstraints.CENTER;
		gridBagConstraints12.gridy = 5;

		GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
		gridBagConstraints13.gridx = 1;
		gridBagConstraints13.anchor = GridBagConstraints.CENTER;
		gridBagConstraints13.gridy = 5;

		GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
		gridBagConstraints14.gridx = 0;
		gridBagConstraints14.anchor = GridBagConstraints.SOUTH;
		gridBagConstraints14.insets = new Insets(10, 10, 0, 10);
		gridBagConstraints14.gridy = 8;

		GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
		gridBagConstraints15.gridx = 0;
		gridBagConstraints15.insets = new Insets(0, 10, 10, 10);
		gridBagConstraints15.gridy = 10;

		GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
		gridBagConstraints16.gridx = 2;
		gridBagConstraints16.gridy = 6;

		GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
		gridBagConstraints21.gridx = 1;
		gridBagConstraints21.insets = new Insets(0, 10, 10, 10);
		gridBagConstraints21.anchor = GridBagConstraints.NORTH;
		gridBagConstraints21.gridy = 4;

		GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
		gridBagConstraints22.gridx = 0;
		gridBagConstraints22.anchor = GridBagConstraints.CENTER;
		gridBagConstraints22.gridy = 5;

		GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
		gridBagConstraints23.gridx = 0;
		gridBagConstraints23.gridy = 6;
		gridBagConstraints23.anchor = GridBagConstraints.CENTER;

		GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
		gridBagConstraints24.fill = GridBagConstraints.NONE;
		gridBagConstraints24.gridy = 9;
		gridBagConstraints24.weightx = 1.0;
		gridBagConstraints24.anchor = GridBagConstraints.NORTH;
		gridBagConstraints24.insets = new Insets(10, 10, 10, 10);
		gridBagConstraints24.gridx = 0;

		GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
		gridBagConstraints31.gridx = 2;
		gridBagConstraints31.insets = new Insets(0, 10, 10, 10);
		gridBagConstraints31.anchor = GridBagConstraints.NORTH;
		gridBagConstraints31.gridy = 4;

		GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
		gridBagConstraints32.gridx = 1;
		gridBagConstraints32.anchor = GridBagConstraints.CENTER;
		gridBagConstraints32.gridy = 6;

		GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
		gridBagConstraints41.gridx = 1;
		gridBagConstraints41.anchor = GridBagConstraints.CENTER;
		gridBagConstraints41.gridy = 7;

		GridBagConstraints btnGenerateCustomFilterConstraint = new GridBagConstraints();
		btnGenerateCustomFilterConstraint.gridx = 2;
		btnGenerateCustomFilterConstraint.insets = new Insets(10, 10, 10, 10);
		btnGenerateCustomFilterConstraint.anchor = GridBagConstraints.CENTER;
		btnGenerateCustomFilterConstraint.gridy = 11;

		lblLowFrequencyPreEchoControl = new JLabel();
		lblLowFrequencyPreEchoControl.setText("Low Frequency Pre-Echo Control");
		lblRtWindowExponent = new JLabel();
		lblRtWindowExponent.setForeground(Color.BLUE);
		lblRtWindowExponent.setText("Ringing truncation window exponent:");
		lblEpWindowExponent = new JLabel();
		lblEpWindowExponent.setForeground(Color.BLUE);
		lblEpWindowExponent.setText("Excess phase window exponent:");
		lblEpLowerWindow = new JLabel();
		lblEpLowerWindow.setForeground(Color.BLUE);
		lblEpLowerWindow.setText("Excess phase lower window:");
		lblMpWindowExponent = new JLabel();
		lblMpWindowExponent.setForeground(Color.BLUE);
		lblMpWindowExponent.setText("Min phase window exponent:");
		lblMpLowerWindow = new JLabel();
		lblMpLowerWindow.setForeground(Color.BLUE);
		lblMpLowerWindow.setText("Min phase lower window:");
		lblMpUpperWindow = new JLabel();
		lblMpUpperWindow.setForeground(Color.BLUE);
		lblMpUpperWindow.setText("Min Phase upper window: ");
		lblLowFrequencyPreEchoValue = new JLabel();
		lblLowFrequencyPreEchoValue.setText("%");
		lblEpUpperWindow = new JLabel();
		lblEpUpperWindow.setForeground(Color.BLUE);
		lblEpUpperWindow.setText("Excess phase upper window:");
		lblPeakLimitingMaxGain = new JLabel();
		lblPeakLimitingMaxGain.setText("Peak limiting max gain:");
		lblPeakLimitingMaxGain.setForeground(Color.BLUE);

		
		lblHighFrequencyValue = new JLabel();
		lblHighFrequencyValue.setText("%");
		lblMidFrequencyValue = new JLabel();
		lblMidFrequencyValue.setText("%");
		lblLowFrequencyValue = new JLabel();
		lblLowFrequencyValue.setText("%");
		lblHighFrequencyStrength = new JLabel();
		lblHighFrequencyStrength.setText("High Frequency Correction Strength");
		lblMidFrequencyStrength = new JLabel();
		lblMidFrequencyStrength.setText("Mid Frequency Correction Strength");
		lblLowFrequencyStrength = new JLabel();
		lblLowFrequencyStrength.setText("Low Frequency Correction Strength");
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Custom Filter Generation"));
		this.add(lblLowFrequencyStrength, gridBagConstraints);
		this.add(lblMidFrequencyStrength, gridBagConstraints1);
		this.add(lblHighFrequencyStrength, gridBagConstraints2);
		this.add(getSldLowFrequencyStrength(), gridBagConstraints3);
		this.add(getSldMidFrequencyStrength(), gridBagConstraints4);
		this.add(getSldHighFrequencyStrength(), gridBagConstraints5);
		this.add(lblLowFrequencyValue, gridBagConstraints11);
		this.add(lblMpUpperWindow, gridBagConstraints12);
		this.add(lblMpWindowExponent, gridBagConstraints13);
		this.add(lblLowFrequencyPreEchoControl, gridBagConstraints14);
		this.add(lblMidFrequencyValue, gridBagConstraints21);
		this.add(lblMpLowerWindow, gridBagConstraints22);
		this.add(lblEpLowerWindow, gridBagConstraints23);
		this.add(getSldLowFrequencyPreEchoControlStrength(), gridBagConstraints24);
		this.add(lblHighFrequencyValue, gridBagConstraints31);
		this.add(lblEpWindowExponent, gridBagConstraints32);
		this.add(lblRtWindowExponent, gridBagConstraints41);
		this.add(getBtnGenerateCustomFilter(), btnGenerateCustomFilterConstraint);
		this.add(lblLowFrequencyPreEchoValue, gridBagConstraints15);
		this.add(lblEpUpperWindow, gridBagConstraints16);
		this.add(getCboSamplingRate(), gridBagConstraints17);
		this.add(lblSamplingRate, gridBagConstraints25);
		this.add(getTemplateLoader(), gridBagConstraints18);
		this.add(lblMidFrequencyPreEchoControl, gridBagConstraints26);
		this.add(getSldMidFrequencyPreEchoControlStrength(), gridBagConstraints33);
		this.add(lblMidFrequencyPreEchoValue, gridBagConstraints42);
		this.add(lblMaxCorrectionBoost, gridBagConstraints51);
		this.add(getSldMaxCorrectionBoost(), gridBagConstraints6);
		this.add(lblMaxCorrectionBoostValue, gridBagConstraints7);
		this.add(lblPeakLimitingMaxGain, gridBagConstraints8);
		this.add(lblStatus, gridBagConstraints19);
	}

	/**
	 * This method initializes sldLowFrequencyStrength	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private JSlider getSldLowFrequencyStrength() {
		if (sldLowFrequencyStrength == null) {
			sldLowFrequencyStrength = new JSlider();
			sldLowFrequencyStrength
					.addChangeListener(new javax.swing.event.ChangeListener() {
						public void stateChanged(javax.swing.event.ChangeEvent e) {
							//if (!sldLowFrequencyStrength.getValueIsAdjusting()) {
								lblLowFrequencyValue.setText(sldLowFrequencyStrength.getValue() + " %");
								int mplw = calculateMpLowerWindow(sldLowFrequencyStrength.getValue());
								int eplw = calculateExcessPhaseLowerWindow(sldLowFrequencyStrength.getValue(), sldLowFrequencyPreEchoControlStrength.getValue()); 
								lblMpLowerWindow.setText("Min phase lower window: "+ Integer.toString(mplw));
								lblEpLowerWindow.setText("Excess phase lower window: " + Integer.toString(eplw));
								configEntries.setValue(ConfigurationEntries.EntryNames.MPLowerWindow, Integer.toString(mplw));
								configEntries.setValue(ConfigurationEntries.EntryNames.MPPFFinalWindow, Integer.toString(mplw));
								configEntries.setValue(ConfigurationEntries.EntryNames.RTOutWindow, Integer.toString(mplw));
								configEntries.setValue(ConfigurationEntries.EntryNames.EPLowerWindow, Integer.toString(eplw));
								configEntries.setValue(ConfigurationEntries.EntryNames.EPPFFinalWindow, Integer.toString(eplw));
								configEntries.setValue(ConfigurationEntries.EntryNames.ISPELowerWindow, Integer.toString(eplw / 2));
								configEntries.setValue(ConfigurationEntries.EntryNames.MSFilterDelay, Integer.toString(eplw / 2));
								configEntries.setValue(ConfigurationEntries.EntryNames.ISPEUpperWindow, Integer.toString(((eplw / 2) * 3) / 4));								
							//}
						}
					});
		}
		return sldLowFrequencyStrength;
	}

	/**
	 * This method initializes sldMidFrequencyStrength	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private JSlider getSldMidFrequencyStrength() {
		if (sldMidFrequencyStrength == null) {
			sldMidFrequencyStrength = new JSlider();
			sldMidFrequencyStrength
			.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
						float midFrequencyWindowExponent = calculateMpWindowExponent(sldMidFrequencyStrength.getValue());
						lblMidFrequencyValue.setText(sldMidFrequencyStrength.getValue() + " %");
						lblMpWindowExponent.setText("Min phase window exponent: "+ Float.toString(midFrequencyWindowExponent));						
						lblRtWindowExponent.setText("Ringing truncation window exponent: " + Float.toString(midFrequencyWindowExponent));
						configEntries.setValue(ConfigurationEntries.EntryNames.MPWindowExponent, Float.toString(midFrequencyWindowExponent));
						configEntries.setValue(ConfigurationEntries.EntryNames.RTWindowExponent, Float.toString(midFrequencyWindowExponent));
				}
			});
		}
		return sldMidFrequencyStrength;
	}

	/**
	 * This method initializes sldHighFrequencyStrength	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private JSlider getSldHighFrequencyStrength() {
		if (sldHighFrequencyStrength == null) {
			sldHighFrequencyStrength = new JSlider();
			sldHighFrequencyStrength
			.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					lblHighFrequencyValue.setText(sldHighFrequencyStrength.getValue() + " %");
					int mpuw = calculateMpUpperWindow(sldHighFrequencyStrength.getValue());
					lblMpUpperWindow.setText("Min phase upper window: "+ Integer.toString(mpuw));
					lblEpUpperWindow.setText("Excess phase upper window: "+ Integer.toString(mpuw));
					configEntries.setValue(ConfigurationEntries.EntryNames.MPUpperWindow, Integer.toString(mpuw));
					configEntries.setValue(ConfigurationEntries.EntryNames.EPUpperWindow, Integer.toString(mpuw));
					configEntries.setValue(ConfigurationEntries.EntryNames.RTUpperWindow, Integer.toString(mpuw));
					configEntries.setValue(ConfigurationEntries.EntryNames.RTWindowGap, Integer.toString(mpuw));
					
				}
			});
		}
		return sldHighFrequencyStrength;
	}

	/**
	 * This method initializes btnGenerateCustomFilter	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnGenerateCustomFilter() {
		if (btnGenerateCustomFilter == null) {
			btnGenerateCustomFilter = new JButton();
			btnGenerateCustomFilter.setText("Generate Custom Filter");
			btnGenerateCustomFilter.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					runDRC();
				}
			});
		}
		return btnGenerateCustomFilter;
	}

	/**
	 * This method initializes sldLowFrequencyPreEchoControlStrength	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private JSlider getSldLowFrequencyPreEchoControlStrength() {
		if (sldLowFrequencyPreEchoControlStrength == null) {
			sldLowFrequencyPreEchoControlStrength = new JSlider();
			sldLowFrequencyPreEchoControlStrength
					.addChangeListener(new javax.swing.event.ChangeListener() {
						public void stateChanged(javax.swing.event.ChangeEvent e) {
							lblLowFrequencyPreEchoValue.setText(sldLowFrequencyPreEchoControlStrength.getValue() + " %");
							int eplw = calculateExcessPhaseLowerWindow(sldLowFrequencyStrength.getValue(), sldLowFrequencyPreEchoControlStrength.getValue());
							lblEpLowerWindow.setText("Excess phase lower window: " + Integer.toString(eplw));
							configEntries.setValue(ConfigurationEntries.EntryNames.EPLowerWindow, Integer.toString(eplw));
							configEntries.setValue(ConfigurationEntries.EntryNames.EPPFFinalWindow, Integer.toString(eplw));
							configEntries.setValue(ConfigurationEntries.EntryNames.ISPELowerWindow, Integer.toString(eplw / 2));
							configEntries.setValue(ConfigurationEntries.EntryNames.MSFilterDelay, Integer.toString(eplw / 2));
							configEntries.setValue(ConfigurationEntries.EntryNames.ISPEUpperWindow, Integer.toString(((eplw / 2) * 3) / 4));
						}
					});
		}
		return sldLowFrequencyPreEchoControlStrength;
	}

	/**
	 * This method initializes cboSamplingRate	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getCboSamplingRate() {
		if (cboSamplingRate == null) {
			cboSamplingRate = new JComboBox();
			cboSamplingRate.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					templateLoader.setSelectedFilterTemplate(DrcProcessor.FilterType.soft);
					enableDisablePanel();
				}
			});
		}
		return cboSamplingRate;
	}

	/**
	 * This method initializes templateLoader	
	 * 	
	 * @return org.alanjordan.drcdesigner.TemplateLoader	
	 */
	private TemplateLoader getTemplateLoader() {
		if (templateLoader == null) {
			templateLoader = new TemplateLoader(this);
		}
		return templateLoader;
	}

	/**
	 * This method initializes sldMidFrequencyPreEchoControlStrength	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private JSlider getSldMidFrequencyPreEchoControlStrength() {
		if (sldMidFrequencyPreEchoControlStrength == null) {
			sldMidFrequencyPreEchoControlStrength = new JSlider();
			sldMidFrequencyPreEchoControlStrength
			.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					lblMidFrequencyPreEchoValue.setText(sldMidFrequencyPreEchoControlStrength.getValue() + " %");
					float epWindowExponent = calculateEpWindowExponent(sldMidFrequencyPreEchoControlStrength.getValue());
					lblEpWindowExponent.setText("Excess phase window exponent: "+ Float.toString(epWindowExponent));
					configEntries.setValue(ConfigurationEntries.EntryNames.EPWindowExponent, Float.toString(epWindowExponent));
				}
			});

		}
		return sldMidFrequencyPreEchoControlStrength;
	}

	/**
	 * This method initializes sldMaxCorrectionBoost	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private JSlider getSldMaxCorrectionBoost() {
		if (sldMaxCorrectionBoost == null) {
			sldMaxCorrectionBoost = new JSlider();
			sldMaxCorrectionBoost.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					float lowerBound = 1.0F;
					float upperBound = 4.0F;
					float plMaxGain = Math.round(computeValueFromSlider(sldMaxCorrectionBoost.getValue(), lowerBound, upperBound)*10.0F) / 10.0F;

					float maxCorrectionGain = calculateMaxCorrectionGain(plMaxGain);
					//DecimalFormat twoDForm = new DecimalFormat("#.##");

					//lblMaxCorrectionBoostValue.setText(Double.toString(Double.valueOf(twoDForm.format(maxCorrectionGain))) + " dB");
					lblMaxCorrectionBoostValue.setText(Float.toString(Math.round(maxCorrectionGain * 100.0F) / 100.0F) + " dB");
					lblPeakLimitingMaxGain.setText("Peak limiting max gain: " + Float.toString(plMaxGain));
					configEntries.setValue(ConfigurationEntries.EntryNames.PLMaxGain, Float.toString(plMaxGain));
				}
			});
		}
		return sldMaxCorrectionBoost;
	}

}
