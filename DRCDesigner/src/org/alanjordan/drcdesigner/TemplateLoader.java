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

public class TemplateLoader extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel lblLoadTemplate = null;
	private ButtonGroup btnGrpTypes;
	private JRadioButton rdoMinimal = null;
	private JRadioButton rdoSoft = null;
	private JRadioButton rdoNormal = null;
	private CustomizedFilterPanel parent;
	private JRadioButton rdoStrong = null;
	

	/**
	 * This is the default constructor
	 */
	public TemplateLoader(CustomizedFilterPanel parent) {
		super();
		initialize();
		this.parent = parent;
	}
	
	public void setSelectedFilterTemplate(DrcProcessor.FilterType filterType) {
		switch (filterType) {
			case minimal:
				rdoMinimal.doClick();
			break;
			case soft:
				rdoSoft.doClick();
			break;
			case normal:
				rdoNormal.doClick();
			break;
			case strong:
				rdoStrong.doClick();
			break;
		}
	}
	
	public void enable(boolean enable) {
		lblLoadTemplate.setEnabled(enable);
		rdoMinimal.setEnabled(enable);
		rdoSoft.setEnabled(enable);
		rdoNormal.setEnabled(enable);
		rdoStrong.setEnabled(enable);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 4;
		gridBagConstraints1.gridy = 0;
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 3;
		gridBagConstraints4.gridy = 0;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 2;
		gridBagConstraints3.gridy = 0;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 1;
		gridBagConstraints2.gridy = 0;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		gridBagConstraints.gridy = 0;
		btnGrpTypes = getBtnGrpTypes();
		lblLoadTemplate = new JLabel();
		lblLoadTemplate.setText("Load template: ");
		this.setSize(340, 30);
		this.setLayout(new GridBagLayout());
		this.add(lblLoadTemplate, gridBagConstraints);
		this.add(getRdoMinimal(), gridBagConstraints2);
		this.add(getRdoSoft(), gridBagConstraints3);
		this.add(getRdoNormal(), gridBagConstraints4);
		this.add(getRdoStrong(), gridBagConstraints1);
		btnGrpTypes.add(rdoMinimal);
		btnGrpTypes.add(rdoSoft);
		btnGrpTypes.add(rdoNormal);	
		btnGrpTypes.add(rdoStrong);
	}

	private ButtonGroup getBtnGrpTypes() {
		if (btnGrpTypes == null) {
			btnGrpTypes = new ButtonGroup();
		}
		return btnGrpTypes;
	}
	
	/**
	 * This method initializes rdoMinimal	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRdoMinimal() {
		if (rdoMinimal == null) {
			rdoMinimal = new JRadioButton();
			rdoMinimal.setText("Minimal");
			rdoMinimal.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					parent.loadTemplate(new ConfigurationEntries(DrcProcessor.FilterType.minimal, parent.getSamplingRate()));
				}
			});
		}
		return rdoMinimal;
	}

	/**
	 * This method initializes rdoSoft	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRdoSoft() {
		if (rdoSoft == null) {
			rdoSoft = new JRadioButton();
			rdoSoft.setText("Soft");
			rdoSoft.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					parent.loadTemplate(new ConfigurationEntries(DrcProcessor.FilterType.soft, parent.getSamplingRate()));
				}
			});
		}
		return rdoSoft;
	}

	/**
	 * This method initializes rdoNormal	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRdoNormal() {
		if (rdoNormal == null) {
			rdoNormal = new JRadioButton();
			rdoNormal.setText("Normal");
			rdoNormal.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					parent.loadTemplate(new ConfigurationEntries(DrcProcessor.FilterType.normal, parent.getSamplingRate()));
				}
			});
		}
		return rdoNormal;
	}


	/**
	 * This method initializes rdoStrong	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRdoStrong() {
		if (rdoStrong == null) {
			rdoStrong = new JRadioButton();
			rdoStrong.setText("Strong");
			rdoStrong.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					parent.loadTemplate(new ConfigurationEntries(DrcProcessor.FilterType.strong, parent.getSamplingRate()));
				}
			});
		}
		return rdoStrong;
	}

}
