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
import javax.swing.JPanel;
import java.awt.GridBagConstraints;

public class TargetDesignerPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private TargetDesigner targetDesigner = null;
	private TargetLoader targetLoader = null;
	private Options options;

	/**
	 * This is the default constructor
	 */
	public TargetDesignerPanel(Options options) {
		super();
		this.options = options;
		initialize();
		if (options.getPoints() == null)
			targetLoader.setSelectedTarget(Targets.TargetNames.Flat);
		else
			targetDesigner.drawTarget(options.getPoints());
	}

	public void drawTarget(Targets.TargetNames targetName) {
		Targets t = new Targets(options);
		FrequencyAmplitudePoints fap = t.getTarget(targetName);
		targetDesigner.drawTarget(fap);
	}
	
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 50;
		gridBagConstraints.weighty = 50;		

		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.anchor = GridBagConstraints.CENTER;
		gridBagConstraints1.gridy = 1;

		this.setSize(800, 600);
		this.setLayout(new GridBagLayout());
		this.add(getTargetDesigner(), gridBagConstraints);
		this.add(getTargetLoader(), gridBagConstraints1);
	}

	/**
	 * This method initializes targetDesigner	
	 * 	
	 * @return org.alanjordan.drcdesigner.TargetDesigner	
	 */
	private TargetDesigner getTargetDesigner() {
		if (targetDesigner == null) {
			targetDesigner = new TargetDesigner(options);
		}
		return targetDesigner;
	}


	/**
	 * This method initializes targetLoader
	 * 	
	 * @return org.alanjordan.drcdesigner.TargetLoader	
	 */
	private TargetLoader getTargetLoader() {
		if (targetLoader == null) {
			targetLoader = new TargetLoader(this);
		}
		return targetLoader;
	}

}
