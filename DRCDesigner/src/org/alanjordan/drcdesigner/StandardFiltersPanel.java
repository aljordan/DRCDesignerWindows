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
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;

public class StandardFiltersPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private Options options = null;
	private JLabel lblSamplingRate = null;
	private JComboBox cboSamplingRate = null;
	private JButton btnGenerateFilters = null;
	private JLabel lblStatus = null;
	private JLabel lblFilterTypes = null;
	private JCheckBox chkERB = null;
	private JCheckBox chkMinimal = null;
	private JCheckBox chkSoft = null;
	private JCheckBox chkNormal = null;
	private JLabel lblSpace1 = null;
	private JLabel lblSpace0 = null;
	private JCheckBox chkStrong = null;
	private JCheckBox chkMicCompensation = null;
	private JLabel lblMicCompensation = null;
	private JFileChooser fcMicComp;
	private JPanel runtimeInstance;
	//private TargetDesigner targetDesigner = null;
	/**
	 * This is the default constructor
	 */
	public StandardFiltersPanel(Options options) {
		super();
		initialize();
		this.options = options;
		initializeSamplingRates();
		initializeMicCompensation();
	}
	
	private void initializeMicCompensation() {
		if (options.isUseMicCompensationFile() && options.getMicCompensationFile() != null) {
			chkMicCompensation.setSelected(true);
			lblMicCompensation.setEnabled(true);
        	lblMicCompensation.setText("Mic calibration file: " + options.getMicCompensationFilePath());
		}
		else {
			chkMicCompensation.setSelected(false);
			if (options.getMicCompensationFile() != null)
	        	lblMicCompensation.setText("Mic calibration file: " + options.getMicCompensationFilePath());				
			else 
				lblMicCompensation.setText("Mic calibration file:");

			lblMicCompensation.setEnabled(false);
		}		
	}
	
	private void runDRC() {
	    DrcProcessor drcp = new DrcProcessor(options, this, cboSamplingRate.getSelectedItem().toString());
	    
	    drcp.start();
	}
	
	public boolean checkSelectedFilterType(DrcProcessor.FilterType type) {
		boolean result = false;
		switch (type) {
		case erb: 
			result = chkERB.isSelected(); 
			break;
		case minimal: 
			result = chkMinimal.isSelected(); 
			break;
		case soft: 
			result = chkSoft.isSelected(); 
			break;
		case normal: 
			result = chkNormal.isSelected(); 
			break;
		case strong: 
			result = chkStrong.isSelected(); 
			break;
		}
		return result;
	}
	
	
    public void setStatus(String status) {
        lblStatus.setText("Status: " + status);
    }
    
    public void enableDisableGenerateFiltersButton(boolean enable) {
    	btnGenerateFilters.setEnabled(enable);
    }

	private void enableDisableGenerateFiltersButton() {
		File leftImpulseResponse = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\LeftSpeakerImpulseResponse" + cboSamplingRate.getSelectedItem() + ".pcm");
		boolean leftExists = leftImpulseResponse.exists(); 
		File rightImpulseResponse = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\RightSpeakerImpulseResponse" + cboSamplingRate.getSelectedItem() + ".pcm");
		boolean rightExists = rightImpulseResponse.exists();
		
		btnGenerateFilters.setEnabled(leftExists && rightExists);
//		pnlCustomFiltersPanel.enable((leftExists && rightExists));
	}
	
	private void initializeSamplingRates() {
		DefaultComboBoxModel rateModel = (DefaultComboBoxModel)cboSamplingRate.getModel();
		rateModel.removeAllElements();
		rateModel.addElement("44100");
		rateModel.addElement("48000");
		rateModel.addElement("88200");
		rateModel.addElement("96000");
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
//		GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
//		gridBagConstraints23.gridx = 1;
//		gridBagConstraints23.gridy = 0;
//		gridBagConstraints23.gridheight = 14;
//		gridBagConstraints23.weightx = 50;
//		gridBagConstraints23.weighty = 1;
//		gridBagConstraints23.fill = GridBagConstraints.BOTH;
		GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
		gridBagConstraints31.gridx = 0;
		gridBagConstraints31.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints31.insets = new Insets(0, 5, 5, 5);
		gridBagConstraints31.gridy = 11;
		lblMicCompensation = new JLabel();
		lblMicCompensation.setText("Mic calibration file:");
		GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
		gridBagConstraints22.gridx = 0;
		gridBagConstraints22.insets = new Insets(5, 5, 0, 5);
		gridBagConstraints22.anchor = GridBagConstraints.SOUTHWEST;
		gridBagConstraints22.gridy = 10;
		GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
		gridBagConstraints91.gridx = 0;
		gridBagConstraints91.anchor = GridBagConstraints.WEST;
		gridBagConstraints91.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints91.gridy = 8;
		GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
		gridBagConstraints21.gridx = 0;
		gridBagConstraints21.anchor = GridBagConstraints.WEST;
		gridBagConstraints21.gridy = 2;
		lblSpace0 = new JLabel();
		lblSpace0.setText("  ");
		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.gridx = 0;
		gridBagConstraints11.anchor = GridBagConstraints.WEST;
		gridBagConstraints11.gridy = 9;
		lblSpace1 = new JLabel();
		lblSpace1.setText("   ");
		GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
		gridBagConstraints10.gridx = 0;
		gridBagConstraints10.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints10.anchor = GridBagConstraints.WEST;
		gridBagConstraints10.gridy = 7;
		GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
		gridBagConstraints9.gridx = 0;
		gridBagConstraints9.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints9.anchor = GridBagConstraints.WEST;
		gridBagConstraints9.gridy = 6;
		GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
		gridBagConstraints8.gridx = 0;
		gridBagConstraints8.anchor = GridBagConstraints.WEST;
		gridBagConstraints8.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints8.gridy = 5;
		GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
		gridBagConstraints7.gridx = 0;
		gridBagConstraints7.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints7.anchor = GridBagConstraints.WEST;
		gridBagConstraints7.gridy = 4;
		GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
		gridBagConstraints6.gridx = 0;
		gridBagConstraints6.anchor = GridBagConstraints.WEST;
		gridBagConstraints6.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints6.gridy = 3;
		lblFilterTypes = new JLabel();
		lblFilterTypes.setText("Filter Types:");
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.anchor = GridBagConstraints.WEST;
		gridBagConstraints3.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints3.gridwidth = 2;
		gridBagConstraints3.gridy = 13;
		lblStatus = new JLabel();
		lblStatus.setText("Status:");
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints2.weightx = 1.0;
		gridBagConstraints2.anchor = GridBagConstraints.WEST;
		gridBagConstraints2.gridy = 12;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.weightx = 1.0;
		gridBagConstraints1.anchor = GridBagConstraints.WEST;
		gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints1.weighty = 0.0;
		gridBagConstraints1.gridx = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.ipady = 1;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.gridy = 0;
		lblSamplingRate = new JLabel();
		lblSamplingRate.setText("Sampling Rate");
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Standard Filter Generation"));
		this.setSize(800, 600);
		this.setLayout(new GridBagLayout());
		this.add(lblSamplingRate, gridBagConstraints);
		this.add(getCboSamplingRate(), gridBagConstraints1);
		this.add(getBtnGenerateFilters(), gridBagConstraints2);
		this.add(lblStatus, gridBagConstraints3);
		this.add(lblFilterTypes, gridBagConstraints6);
		this.add(getChkERB(), gridBagConstraints7);
		this.add(getChkMinimal(), gridBagConstraints8);
		this.add(getChkSoft(), gridBagConstraints9);
		this.add(getChkNormal(), gridBagConstraints10);
		this.add(lblSpace1, gridBagConstraints11);
		this.add(lblSpace0, gridBagConstraints21);
		this.add(getChkStrong(), gridBagConstraints91);
		this.add(getChkMicCompensation(), gridBagConstraints22);
		this.add(lblMicCompensation, gridBagConstraints31);
		//this.add(getTargetDesigner(), gridBagConstraints23);
		runtimeInstance = this; // just used to pass the parent window to the JFileChooser
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
					enableDisableGenerateFiltersButton();
				}
			});
		}
		return cboSamplingRate;
	}

	/**
	 * This method initializes btnGenerateFilters	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnGenerateFilters() {
		if (btnGenerateFilters == null) {
			btnGenerateFilters = new JButton();
			btnGenerateFilters.setText("Generate Selected Filters     ");
			btnGenerateFilters.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					runDRC();
				}
			});
		}
		return btnGenerateFilters;
	}

	/**
	 * This method initializes chkERB	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getChkERB() {
		if (chkERB == null) {
			chkERB = new JCheckBox();
			chkERB.setText("ERB Psycho Acoustic");
			chkERB.setSelected(true);
		}
		return chkERB;
	}

	/**
	 * This method initializes chkMinimal	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getChkMinimal() {
		if (chkMinimal == null) {
			chkMinimal = new JCheckBox();
			chkMinimal.setText("Minimal");
			chkMinimal.setSelected(true);
		}
		return chkMinimal;
	}

	/**
	 * This method initializes chkSoft	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getChkSoft() {
		if (chkSoft == null) {
			chkSoft = new JCheckBox();
			chkSoft.setText("Soft");
			chkSoft.setSelected(true);
		}
		return chkSoft;
	}

	/**
	 * This method initializes chkNormal	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getChkNormal() {
		if (chkNormal == null) {
			chkNormal = new JCheckBox();
			chkNormal.setText("Normal");
			chkNormal.setSelected(true);
		}
		return chkNormal;
	}

	/**
	 * This method initializes chkStrong	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getChkStrong() {
		if (chkStrong == null) {
			chkStrong = new JCheckBox();
			chkStrong.setText("Strong");
			chkStrong.setSelected(true);
		}
		return chkStrong;
	}

	/**
	 * This method initializes chkMicCompensation	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getChkMicCompensation() {
		if (chkMicCompensation == null) {
			chkMicCompensation = new JCheckBox();
			chkMicCompensation.setText("Use Microphone Calibration File");
			chkMicCompensation.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (chkMicCompensation.isSelected()) {
						fcMicComp = new JFileChooser();
						fcMicComp.setFileSelectionMode(JFileChooser.FILES_ONLY);

			            int retval = fcMicComp.showOpenDialog(runtimeInstance);
			            if (retval == JFileChooser.APPROVE_OPTION) {
			            	options.setUseMicCompensationFile(true);
			            	options.setMicCompensationFile(fcMicComp.getSelectedFile());
			            	lblMicCompensation.setEnabled(true);
			            	lblMicCompensation.setText("Mic compensation file: " + options.getMicCompensationFilePath());
			            }
			            else { // file chooser is canceled
			            	// if there is already a microphone compensation file specified we will
		            		// keep showing the path in case user does not want to load it again
			            	if (options.getMicCompensationFile() != null) {
			            		lblMicCompensation.setEnabled(true);
				            	lblMicCompensation.setText("Mic compensation file: " + options.getMicCompensationFilePath());			            		
			            	} 
			            	else {
				            	lblMicCompensation.setText("Mic compensation file:");			            		
				            	lblMicCompensation.setEnabled(false);
				            	chkMicCompensation.setSelected(false);
			            	}
			            }
					} 
					else { // unchecked
						options.setUseMicCompensationFile(false);
						lblMicCompensation.setEnabled(false);
					}
				}
			});
		}
		return chkMicCompensation;
	}

//	/**
//	 * This method initializes targetDesigner	
//	 * 	
//	 * @return org.alanjordan.drcdesigner.TargetDesigner	
//	 */
//	private TargetDesigner getTargetDesigner() {
//		if (targetDesigner == null) {
//			targetDesigner = new TargetDesigner();
//		}
//		return targetDesigner;
//	}

//	/**
//	 * This method initializes pnlCustomFiltersPanel	
//	 * 	
//	 * @return org.alanjordan.drcdesigner.CustomFiltersPanel	
//	 */
//	private CustomFilterPanel getPnlCustomFiltersPanel() {
//		if (pnlCustomFiltersPanel == null) {
//			pnlCustomFiltersPanel = new CustomFilterPanel();
//		}
//		return pnlCustomFiltersPanel;
//	}

}
