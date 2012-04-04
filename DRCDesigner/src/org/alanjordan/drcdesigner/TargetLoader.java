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

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JRadioButton;

public class TargetLoader extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel lblLoadTarget = null;
	private ButtonGroup btnGrpTarget;  //  @jve:decl-index=0:
	private JRadioButton rdoFlat = null;
	private JRadioButton rdoBK = null;
	private JRadioButton rdoBK2 = null;
	private JRadioButton rdoBK3 = null;
	private JRadioButton rdoDeepTilt = null;
	private JRadioButton rdoDeepTilt2 = null;
	private TargetDesignerPanel parent;

	/**
	 * This is the default constructor
	 */
	public TargetLoader(TargetDesignerPanel parent) {
		super();
		initialize();
		this.parent = parent;
	}
	
	public void setSelectedTarget(Targets.TargetNames targetName) {
		switch (targetName) {
			case Flat:
				rdoFlat.doClick();
			break;
			case BK:
				rdoBK.doClick();
			break;
			case BK2:
				rdoBK2.doClick();
			break;
			case BK3:
				rdoBK3.doClick();
			break;
			case DeepTilt:
				rdoDeepTilt.doClick();
			break;
			case DeepTilt2:
				rdoDeepTilt2.doClick();
			break;
		}
	}


	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		btnGrpTarget = getBtnGrpTarget();

		GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
		gridBagConstraints11.gridx = 6;
		gridBagConstraints11.gridy = 0;
		GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
		gridBagConstraints21.gridx = 5;
		gridBagConstraints21.gridy = 0;
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 4;
		gridBagConstraints4.gridy = 0;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 3;
		gridBagConstraints3.gridy = 0;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 2;
		gridBagConstraints2.gridy = 0;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.gridy = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		lblLoadTarget = new JLabel();
		lblLoadTarget.setText("Load Target: ");
		this.setSize(440, 30);
		this.setLayout(new GridBagLayout());
		this.add(lblLoadTarget, gridBagConstraints);
		this.add(getRdoFlat(), gridBagConstraints1);
		this.add(getRdoBK(), gridBagConstraints2);
		this.add(getRdoBK2(), gridBagConstraints3);
		this.add(getRdoBK3(), gridBagConstraints4);
		this.add(getRdoDeepTilt(), gridBagConstraints21);
		this.add(getRdoDeepTilt2(), gridBagConstraints11);
		btnGrpTarget.add(rdoFlat);
		btnGrpTarget.add(rdoBK);
		btnGrpTarget.add(rdoBK2);
		btnGrpTarget.add(rdoBK3);
		btnGrpTarget.add(rdoDeepTilt);
		btnGrpTarget.add(rdoDeepTilt2);
	}

	/**
	 * This method initializes rdoFlat	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRdoFlat() {
		if (rdoFlat == null) {
			rdoFlat = new JRadioButton();
			rdoFlat.setText("Flat");
			rdoFlat.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 parent.drawTarget(Targets.TargetNames.Flat);
				}
			});
		}
		return rdoFlat;
	}

	/**
	 * This method initializes rdoBK	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRdoBK() {
		if (rdoBK == null) {
			rdoBK = new JRadioButton();
			rdoBK.setText("B&K");
			rdoBK.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 parent.drawTarget(Targets.TargetNames.BK);
				}
			});
		}
		return rdoBK;
	}

	/**
	 * This method initializes rdoBK2	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRdoBK2() {
		if (rdoBK2 == null) {
			rdoBK2 = new JRadioButton();
			rdoBK2.setText("B&K2");
			rdoBK2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 parent.drawTarget(Targets.TargetNames.BK2);
				}
			});
		}
		return rdoBK2;
	}

	/**
	 * This method initializes rdoBK3	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRdoBK3() {
		if (rdoBK3 == null) {
			rdoBK3 = new JRadioButton();
			rdoBK3.setText("B&K3");
			rdoBK3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 parent.drawTarget(Targets.TargetNames.BK3);
				}
			});
		}
		return rdoBK3;
	}
	
	private ButtonGroup getBtnGrpTarget() {
		if (btnGrpTarget == null) {
			btnGrpTarget = new ButtonGroup();
		}
		return btnGrpTarget;
	}

	/**
	 * This method initializes rdoDeepTilt2	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRdoDeepTilt2() {
		if (rdoDeepTilt2 == null) {
			rdoDeepTilt2 = new JRadioButton();
			rdoDeepTilt2.setText("Deep Tilt 2");
			rdoDeepTilt2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 parent.drawTarget(Targets.TargetNames.DeepTilt2);
				}
			});
		}
		return rdoDeepTilt2;
	}

	/**
	 * This method initializes rdoDeepTilt	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRdoDeepTilt() {
		if (rdoDeepTilt == null) {
			rdoDeepTilt = new JRadioButton();
			rdoDeepTilt.setText("Deep Tilt");
			rdoDeepTilt.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 parent.drawTarget(Targets.TargetNames.DeepTilt);
				}
			});
		}
		return rdoDeepTilt;
	}


}
