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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class RecordSweepPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JPanel pnlTopSpace;
	private JPanel pnlBottomSpace;
	private JButton btnInterfaces = null;
	private JButton btnRecord = null;
	private JLabel lblPlaybackInterfaces = null;
	private JLabel lblInputChannel = null;
	private JLabel lblLeftOutputChannel = null;
	private JLabel lblRightOutputChannel = null;
	private JLabel lblSamplingRate = null;
	private JList lstInterfaces = null;
	private JComboBox cboLeftOutputChannel = null;
	private JComboBox cboRightOutputChannel = null;
	private JComboBox cboInputChannel = null;
	private JComboBox cboSamplingRate = null;
	private Options options;
	private ArrayList<SoundInterface> playbackInterfaces;
	private ArrayList<SoundInterface> recordingInterfaces;
	private JList lstRecordingInterfaces = null;
	private JLabel lblRecordingInterfaces = null;
	private JLabel lblLeftChannelPeak = null;
	private JLabel lblRightChannelPeak = null;
	/**
	 * This is the default constructor
	 */
	public RecordSweepPanel(Options options) {
		super();
		initialize();
		this.options = options;
		btnRecord.setEnabled(false);
	}
	
	
	private void enableDisableRecordButton() {
		btnRecord.setEnabled(lstInterfaces.getSelectedIndex() > -1
				&& lstRecordingInterfaces.getSelectedIndex() > -1
				&& cboLeftOutputChannel.getSelectedIndex() > -1
				&& cboRightOutputChannel.getSelectedIndex() > -1
				&& cboInputChannel.getSelectedIndex() > -1
				&& cboSamplingRate.getSelectedIndex() > -1);
	}
	
	
	private void parseSweepResultsFile() {
        Scanner scanner;
        String line;
        int startingChar = 0;
        int endingChar = 0;
        String recordedPeakLevel;
        boolean leftChannelDone = false;
        
        try {
            scanner = new Scanner(new File("recordSweepOutput.txt"));

            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (line.startsWith("Warning: Probable clipping occurred on microphone input")) {
                	if (leftChannelDone == false) {
                		lblLeftChannelPeak.setForeground(Color.RED);
                		lblLeftChannelPeak.setText("Left channel clip: lower input level");
                		leftChannelDone = true;
                	}                	
                	else {
                		lblRightChannelPeak.setForeground(Color.RED);
                		lblRightChannelPeak.setText("Right channel clip: lower input level");                		
                	}
                }
                if (line.startsWith("Peak Mic level")) {
                	startingChar = line.indexOf("(");
                	endingChar = line.indexOf(")");
                	recordedPeakLevel =  line.substring(startingChar + 2, endingChar - 2);
                	if (recordedPeakLevel.length() > 5)
                		recordedPeakLevel = recordedPeakLevel.substring(0,5);
                	
                	double dblRecordedPeak = Double.parseDouble(recordedPeakLevel);

                	if (leftChannelDone == false) {
                		if (dblRecordedPeak >= -3.0)                 			
                			lblLeftChannelPeak.setForeground(Color.ORANGE);
                		if (dblRecordedPeak > -20 && dblRecordedPeak < -3.0)                 			
                			lblLeftChannelPeak.setForeground(((Color.GREEN).darker()).darker());
                		else if (dblRecordedPeak <= -20.0 && dblRecordedPeak > -35.0)
                			lblLeftChannelPeak.setForeground(Color.ORANGE);
                		else 
                			lblLeftChannelPeak.setForeground(Color.RED);
                			
                		lblLeftChannelPeak.setText("Left channel peak: " + recordedPeakLevel + " db");
                		leftChannelDone = true;
                	}
                	else {
                		if (dblRecordedPeak >= -3.0)                 			
                			lblRightChannelPeak.setForeground(Color.ORANGE);
                		if (dblRecordedPeak > -20 && dblRecordedPeak < -3.0)                 			
                			lblRightChannelPeak.setForeground(((Color.GREEN).darker()).darker());
                		else if (dblRecordedPeak <= -20.0 && dblRecordedPeak > -35.0)
                			lblRightChannelPeak.setForeground(Color.ORANGE);
                		else 
                			lblRightChannelPeak.setForeground(Color.RED);

                		lblRightChannelPeak.setText("Right channel peak: " + recordedPeakLevel + " db");                		
                	}
                }
               	System.out.println(line);
            }
            scanner.close();
        }
        catch (FileNotFoundException fnf) {
            System.out.println("Record sweep results file not found");
        }
	}

	private void launchRecordSweepScript() {
		String recImpDir = options.getRoomCorrectionRootPath() + "\\Rec_imp.win32";
		String drcDir = options.getRoomCorrectionRootPath() + "\\drc-3.2.0\\sample";
		String scriptName = "";
		
		lblLeftChannelPeak.setText("Left channel peak:");
		lblRightChannelPeak.setText("Right channel peak:");

		if (options.getDriverType() == Options.InterfaceDriverType.ASIO)
			scriptName = "rec_impAsio.exe";
		else
			scriptName = "rec_impDS.exe";
		
        try {
        	PrintWriter out = new PrintWriter(new FileWriter("drcWrapperRecordSweep.bat", false));
        	out.println("cd " + recImpDir);
        	out.println(scriptName + " LeftSpeakerImpulseResponse" + cboSamplingRate.getSelectedItem() + ".pcm " + cboSamplingRate.getSelectedItem() + " 20 21000 45 " + cboLeftOutputChannel.getSelectedIndex() + ":" + playbackInterfaces.get(lstInterfaces.getSelectedIndex()).getDeviceNumber() + " " + cboInputChannel.getSelectedIndex() + ":" + (recordingInterfaces.get(lstRecordingInterfaces.getSelectedIndex()).getDeviceNumber()));
        	out.println("move /y LeftSpeakerImpulseResponse" + cboSamplingRate.getSelectedItem() + ".pcm \"" + drcDir + "\"");
        	out.println(scriptName + " RightSpeakerImpulseResponse" + cboSamplingRate.getSelectedItem() + ".pcm " + cboSamplingRate.getSelectedItem() + " 20 21000 45 " + cboRightOutputChannel.getSelectedIndex() + ":" + playbackInterfaces.get(lstInterfaces.getSelectedIndex()).getDeviceNumber() + " " + cboInputChannel.getSelectedIndex() + ":" + (recordingInterfaces.get(lstRecordingInterfaces.getSelectedIndex()).getDeviceNumber()));
        	out.println("move /y RightSpeakerImpulseResponse" + cboSamplingRate.getSelectedItem() + ".pcm \"" + drcDir + "\"");
        	out.close();
        	
    		String command = "cmd.exe /c \"drcWrapperRecordSweep.bat 1>recordSweepOutput.txt 2>&1\"";
    		Runtime rt = Runtime.getRuntime();
    		Process p = rt.exec(command);
    		p.waitFor();
        	parseSweepResultsFile();
    	}
    		catch(Exception exc){
    		exc.printStackTrace();
    	}		
	}
	
	
	private void updatePlaybackFields(SoundInterface si) {
		DefaultComboBoxModel leftModel = (DefaultComboBoxModel)cboLeftOutputChannel.getModel();
		DefaultComboBoxModel rightModel = (DefaultComboBoxModel)cboRightOutputChannel.getModel();
		leftModel.removeAllElements();
		rightModel.removeAllElements();
		for (int counter = 0; counter < si.getOutputChannels(); counter++) {
			leftModel.addElement(counter + 1);
			rightModel.addElement(counter + 1);
		}
		
		if (cboLeftOutputChannel.getItemCount() > 1)
			cboLeftOutputChannel.setSelectedIndex(0);

		if (cboRightOutputChannel.getItemCount() > 1)
			cboRightOutputChannel.setSelectedIndex(1);

		DefaultComboBoxModel rateModel = (DefaultComboBoxModel)cboSamplingRate.getModel();
		rateModel.removeAllElements();
		if (si.isProbeStatusSuccessful()) {
			String[] rates = si.getSupportedSampleRates();
			for (int counter = 0; counter < rates.length; counter++) {
				rateModel.addElement(rates[counter]);
			}
			updateCommonSampleRates();
		}
	}

	private void updateRecordingFields(SoundInterface si) {		
		DefaultComboBoxModel inputModel = (DefaultComboBoxModel)cboInputChannel.getModel();
		inputModel.removeAllElements();
		for (int counter = 0; counter < si.getInputChannels(); counter++) {
			inputModel.addElement(counter + 1);
		}
		
		DefaultComboBoxModel rateModel = (DefaultComboBoxModel)cboSamplingRate.getModel();
		rateModel.removeAllElements();
		if (si.isProbeStatusSuccessful()) {
			String[] rates = si.getSupportedSampleRates();
			for (int counter = 0; counter < rates.length; counter++) {
				rateModel.addElement(rates[counter]);
			}
			updateCommonSampleRates();
		}
	}
	
	private void updateCommonSampleRates() {
		SoundInterface outputInterface = null;
		SoundInterface inputInterface = null;
		String [] outputRates;
		
		DefaultComboBoxModel rateModel = (DefaultComboBoxModel)cboSamplingRate.getModel();
		
		if (lstInterfaces.getSelectedIndex() > -1) 
			outputInterface = playbackInterfaces.get(lstInterfaces.getSelectedIndex());
		
		if (lstRecordingInterfaces.getSelectedIndex() > -1) 
			inputInterface = recordingInterfaces.get(lstRecordingInterfaces.getSelectedIndex());
				
		if (outputInterface != null && inputInterface != null) {
			rateModel.removeAllElements();
			outputRates = outputInterface.getSupportedSampleRates();
			for (int counter = 0; counter < outputRates.length; counter++) {
				if (inputInterface.handlesSampleRate(outputRates[counter])) {
					rateModel.addElement(outputRates[counter]);
				}
			}
		}
		
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(800, 600);
		this.setLayout(new GridBagLayout());

		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 2;
		gridBagConstraints2.anchor = GridBagConstraints.WEST;
		gridBagConstraints2.gridy = 7;
		lblRightChannelPeak = new JLabel();
		lblRightChannelPeak.setText("Right channel peak:");
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 2;
		gridBagConstraints1.anchor = GridBagConstraints.WEST;
		gridBagConstraints1.gridy = 6;
		lblLeftChannelPeak = new JLabel();
		lblLeftChannelPeak.setText("Left channel peak:");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 6;
		lblRecordingInterfaces = new JLabel();
		lblRecordingInterfaces.setText("Recording Interfaces");

		GridBagConstraints lstRecordingInterfacesConstraints = new GridBagConstraints();
		lstRecordingInterfacesConstraints.fill = GridBagConstraints.BOTH;
		lstRecordingInterfacesConstraints.gridy = 7;
		lstRecordingInterfacesConstraints.weightx = 1.0;
		lstRecordingInterfacesConstraints.weighty = 1.0;
		lstRecordingInterfacesConstraints.gridx = 0;
		lstRecordingInterfacesConstraints.gridheight = 3;
		
		// Add top row spacer.
		GridBagConstraints topRowConstraint = new GridBagConstraints();
		topRowConstraint.gridx = 0;
		topRowConstraint.gridy = 0;
		topRowConstraint.gridwidth = 3;
		topRowConstraint.fill = GridBagConstraints.BOTH;
		topRowConstraint.weightx = 0.0;
		topRowConstraint.weighty = 0.0;
		
		GridBagConstraints cboRightOutputChannelConstraints = new GridBagConstraints();
		cboRightOutputChannelConstraints.fill = GridBagConstraints.NONE;
		cboRightOutputChannelConstraints.gridy = 5;
		cboRightOutputChannelConstraints.weightx = 1.0;
		cboRightOutputChannelConstraints.anchor = GridBagConstraints.NORTHWEST;
		cboRightOutputChannelConstraints.insets = new Insets(5, 5, 5, 5);
		cboRightOutputChannelConstraints.gridx = 1;
		
		GridBagConstraints cboLeftOutputChannelConstraints = new GridBagConstraints();
		cboLeftOutputChannelConstraints.fill = GridBagConstraints.NONE;
		cboLeftOutputChannelConstraints.gridy = 3;
		cboLeftOutputChannelConstraints.weightx = 1.0;
		cboLeftOutputChannelConstraints.anchor = GridBagConstraints.NORTHWEST;
		cboLeftOutputChannelConstraints.insets = new Insets(5, 5, 5, 5);
		cboLeftOutputChannelConstraints.gridx = 1;
		
		GridBagConstraints btnInterfacesConstraints = new GridBagConstraints();
		btnInterfacesConstraints.gridx = 0;
		btnInterfacesConstraints.gridy = 1;
		btnInterfacesConstraints.gridwidth = 1;
		btnInterfacesConstraints.anchor = GridBagConstraints.NORTHWEST;
		btnInterfacesConstraints.insets = new Insets(5, 5, 5, 5);
		btnInterfacesConstraints.gridheight = 1;
		
		GridBagConstraints btnRecordConstraints = new GridBagConstraints();
		btnRecordConstraints.gridx = 2;
		btnRecordConstraints.gridy = 4;
		btnRecordConstraints.gridheight = 1;
		btnRecordConstraints.anchor = GridBagConstraints.NORTHWEST;
		btnRecordConstraints.insets = new Insets(5, 5, 5, 5);
		btnRecordConstraints.gridwidth = 1;
		
		GridBagConstraints lblInterfacesConstraints = new GridBagConstraints();
		lblInterfacesConstraints.gridx = 0;
		lblInterfacesConstraints.insets = new Insets(5, 5, 5, 5);
		lblInterfacesConstraints.anchor = GridBagConstraints.SOUTHWEST;
		lblInterfacesConstraints.gridy = 2;
		
		GridBagConstraints lstInterfacesConstaints = new GridBagConstraints();
		lstInterfacesConstaints.gridx = 0;
		lstInterfacesConstaints.fill = GridBagConstraints.BOTH;
		lstInterfacesConstaints.anchor = GridBagConstraints.NORTHWEST;
		lstInterfacesConstaints.gridheight = 3;
		lstInterfacesConstaints.weightx = 0.5;
		lstInterfacesConstaints.weighty = 0.5;
		lstInterfacesConstaints.gridy = 3;
		
		GridBagConstraints lblLeftOutputChannelConstraints = new GridBagConstraints();
		lblLeftOutputChannelConstraints.gridx = 1;
		lblLeftOutputChannelConstraints.gridy = 2;
		lblLeftOutputChannelConstraints.gridheight = 1;
		lblLeftOutputChannelConstraints.insets = new Insets(5, 5, 5, 5);
		lblLeftOutputChannelConstraints.anchor = GridBagConstraints.SOUTHWEST;
		lblLeftOutputChannelConstraints.gridwidth = 1;

		GridBagConstraints lblRightOutputChannelConstraints = new GridBagConstraints();
		lblRightOutputChannelConstraints.gridx = 1;
		lblRightOutputChannelConstraints.gridy = 4;
		lblRightOutputChannelConstraints.gridheight = 1;
		lblRightOutputChannelConstraints.insets = new Insets(5, 5, 5, 5);
		lblRightOutputChannelConstraints.anchor = GridBagConstraints.SOUTHWEST;
		lblRightOutputChannelConstraints.gridwidth = 1;
		
		GridBagConstraints lblInputChannelConstraints = new GridBagConstraints();
		lblInputChannelConstraints.gridx = 1;
		lblInputChannelConstraints.gridy = 6;
		lblInputChannelConstraints.gridheight = 1;
		lblInputChannelConstraints.insets = new Insets(5, 5, 5, 5);
		lblInputChannelConstraints.anchor = GridBagConstraints.SOUTHWEST;
		lblInputChannelConstraints.gridwidth = 1;

		GridBagConstraints cboInputChannelConstraints = new GridBagConstraints();
		cboInputChannelConstraints.fill = GridBagConstraints.NONE;
		cboInputChannelConstraints.gridy = 7;
		cboInputChannelConstraints.weightx = 1.0;
		cboInputChannelConstraints.anchor = GridBagConstraints.NORTHWEST;
		cboInputChannelConstraints.insets = new Insets(5, 5, 5, 5);
		cboInputChannelConstraints.gridx = 1;

		GridBagConstraints lblSamplingRateConstraints = new GridBagConstraints();
		lblSamplingRateConstraints.gridx = 2;
		lblSamplingRateConstraints.gridy = 2;
		lblSamplingRateConstraints.gridheight = 1;
		lblSamplingRateConstraints.insets = new Insets(5, 5, 5, 5);
		lblSamplingRateConstraints.anchor = GridBagConstraints.SOUTHWEST;
		lblSamplingRateConstraints.gridwidth = 1;

		GridBagConstraints cboSamplingRateConstraints = new GridBagConstraints();
		cboSamplingRateConstraints.fill = GridBagConstraints.NONE;
		cboSamplingRateConstraints.gridy = 3;
		cboSamplingRateConstraints.weightx = 1.0;
		cboSamplingRateConstraints.anchor = GridBagConstraints.NORTHWEST;
		cboSamplingRateConstraints.insets = new Insets(5, 5, 5, 5);
		cboSamplingRateConstraints.gridx = 2;
		
		GridBagConstraints bottomRowConstraint = new GridBagConstraints();
		bottomRowConstraint.gridx = 0;
		bottomRowConstraint.gridy = 11;
		bottomRowConstraint.gridwidth = 3;
		bottomRowConstraint.fill = GridBagConstraints.BOTH;
		bottomRowConstraint.weightx = 0.0;
		bottomRowConstraint.weighty = 0.0;


		this.add(getPnlTopSpace(), topRowConstraint);
		this.add(getBtnInterfaces(), btnInterfacesConstraints);
		this.add(getLblPlaybackInterfaces(), lblInterfacesConstraints);
		this.add(getLstInterfaces(), lstInterfacesConstaints);
		this.add(getLblLeftOutputChannel(), lblLeftOutputChannelConstraints);
		this.add(getCboLeftOutputChannel(), cboLeftOutputChannelConstraints);
		this.add(getLblRightOutputChannel(), lblRightOutputChannelConstraints);
		this.add(getCboRightOutputChannel(), cboRightOutputChannelConstraints);
		this.add(getLblInputChannel(), lblInputChannelConstraints);
		this.add(getCboInputChannel(), cboInputChannelConstraints);
		this.add(getLblSamplingRate(), lblSamplingRateConstraints);
		this.add(getCboSamplingRate(), cboSamplingRateConstraints);
		this.add(getBtnRecord(), btnRecordConstraints);
		this.add(getPnlBottomSpace(), bottomRowConstraint);
		this.add(getLstRecordingInterfaces(), lstRecordingInterfacesConstraints);
		this.add(lblRecordingInterfaces, gridBagConstraints);
		this.add(lblLeftChannelPeak, gridBagConstraints1);
		this.add(lblRightChannelPeak, gridBagConstraints2);
	}

	private JPanel getPnlTopSpace() {
		if (pnlTopSpace == null) {
			pnlTopSpace = new JPanel();
			pnlTopSpace.setPreferredSize(new Dimension(800, 25));
			// pnlTopSpace.add(new JLabel("  "));
		}
		return pnlTopSpace;
	}

	private JPanel getPnlBottomSpace() {
		if (pnlBottomSpace == null) {
			pnlBottomSpace = new JPanel();
			pnlBottomSpace.setPreferredSize(new Dimension(800, 100));
			// pnlBottomSpace(new JLabel("  "));
		}
		return pnlBottomSpace;
	}

	private JLabel getLblLeftOutputChannel() {
		if (lblLeftOutputChannel == null) {
			lblLeftOutputChannel = new JLabel();
			lblLeftOutputChannel.setText("Left Output Channel");
		}
		return lblLeftOutputChannel;
	}
	
	private JLabel getLblRightOutputChannel() {
		if (lblRightOutputChannel == null) {
			lblRightOutputChannel = new JLabel();
			lblRightOutputChannel.setText("Right Output Channel");
		}
		return lblRightOutputChannel;
	}

	private JLabel getLblInputChannel() {
		if (lblInputChannel == null) {
			lblInputChannel = new JLabel();
			lblInputChannel.setText("Input Channel");
		}
		return lblInputChannel;
	}
	
	private JLabel getLblSamplingRate() {
		if (lblSamplingRate == null) {
			lblSamplingRate = new JLabel();
			lblSamplingRate.setText("Sampling Rate");
		}
		return lblSamplingRate;
	}

	private JLabel getLblPlaybackInterfaces() {
		if (lblPlaybackInterfaces == null) {
			lblPlaybackInterfaces = new JLabel();
			lblPlaybackInterfaces.setText("Playback Interfaces");
		}
		return lblPlaybackInterfaces;
	}

	private JButton getBtnInterfaces() {
		if (btnInterfaces == null) {
			btnInterfaces = new JButton();
			btnInterfaces.setText("Get Sound Interfaces");
			btnInterfaces.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SoundInterfaceParser sip = new SoundInterfaceParser(options);
					playbackInterfaces = sip.getPlaybackInterfaces();
					recordingInterfaces = sip.getRecordingInterfaces();
					DefaultListModel dfl = (DefaultListModel)lstInterfaces.getModel();
					dfl.clear();
		//			for (SoundInterface t: interfaces) {
		//				dfl.addElement(t.getName());
		//			}
					for (SoundInterface t: playbackInterfaces) {
						dfl.addElement(t.getName());
					}

					DefaultListModel dflr = (DefaultListModel)lstRecordingInterfaces.getModel();
					dflr.clear();
					for (SoundInterface t: recordingInterfaces) {
						dflr.addElement(t.getName());
					}
				}
			});
		}
		return btnInterfaces;
	}

	private JButton getBtnRecord() {
		if (btnRecord == null) {
			btnRecord = new JButton();
			btnRecord.setText("Record Sweep");
			btnRecord.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					launchRecordSweepScript();
				}
			});
		}
		return btnRecord;
	}

	/**
	 * This method initializes lstInterfaces	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getLstInterfaces() {
		if (lstInterfaces == null) {
			lstInterfaces = new JList(new DefaultListModel());
			lstInterfaces
					.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
						public void valueChanged(javax.swing.event.ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;
							
							JList interfaceList = (JList)e.getSource();
						    int index = interfaceList.getSelectedIndex();
						    if (index > -1)
						    	updatePlaybackFields(playbackInterfaces.get(index));
						    enableDisableRecordButton();
						}
					});
		}
		return lstInterfaces;
	}

	/**
	 * This method initializes cboLeftOutputChannel	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getCboLeftOutputChannel() {
		if (cboLeftOutputChannel == null) {
			cboLeftOutputChannel = new JComboBox();
			cboLeftOutputChannel.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					enableDisableRecordButton();
				}
			});
		}
		return cboLeftOutputChannel;
	}

	/**
	 * This method initializes cboRightOutputChannel	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getCboRightOutputChannel() {
		if (cboRightOutputChannel == null) {
			cboRightOutputChannel = new JComboBox();
			cboRightOutputChannel.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					enableDisableRecordButton();
				}
			});
		}
		return cboRightOutputChannel;
	}
	
	/**
	 * This method initializes cboInputChannel	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getCboInputChannel() {
		if (cboInputChannel == null) {
			cboInputChannel = new JComboBox();
		}
		return cboInputChannel;
	}

	/**
	 * This method initializes cboSamplingRate	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getCboSamplingRate() {
		if (cboSamplingRate== null) {
			cboSamplingRate = new JComboBox();
			cboSamplingRate.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					enableDisableRecordButton();
				}
			});
		}
		return cboSamplingRate;
	}

	/**
	 * This method initializes lstRecordingInterfaces	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getLstRecordingInterfaces() {
		if (lstRecordingInterfaces == null) {
			lstRecordingInterfaces = new JList(new DefaultListModel());
			lstRecordingInterfaces
					.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
						public void valueChanged(javax.swing.event.ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;
							
							JList recordingInterfaceList = (JList)e.getSource();
						    int index = recordingInterfaceList.getSelectedIndex();
						    if (index > -1)
						    	updateRecordingFields(recordingInterfaces.get(index));
						    enableDisableRecordButton();
						}
					});
		}
		return lstRecordingInterfaces;
	}

}
