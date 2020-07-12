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

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JCheckBoxMenuItem;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JTabbedPane;

import java.awt.GridBagConstraints;
import java.io.File;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JMenuItem;

public class DRCDesigner extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JTabbedPane tabbedPaneMain = null;
	private RecordSweepPanel rsp = null;
	private StandardFiltersPanel sfp = null;
	private CustomizedFilterPanel cfp = null;
	private TargetDesignerPanel tdp = null;
	private Options options = null;
	private JMenuBar mnuDrcWrapper = null;
	private JMenu mnuOptions = null;
	private JMenu mnuHelp = null;
	private ButtonGroup rdoGrpInterfaceDriver = new ButtonGroup();  //  @jve:decl-index=0:
	private JRadioButtonMenuItem mnuRdoAsio = null;
	private JRadioButtonMenuItem mnuRdoDirectSound = null;
	private JMenuItem mnuItmSetDrcDirectory = null;
	private JMenuItem mnuItmDRCDesignerHelp = null;
	private JMenuItem mnuItmAboutDRCDesigner = null;
	private JFileChooser fcDrcDirectory;
	private JCheckBoxMenuItem mnuChkSavePcmFiles;


	private JTabbedPane getTabbedPaneMain() {
		if (tabbedPaneMain == null) {
			tabbedPaneMain = new JTabbedPane();
		}
		tabbedPaneMain.setPreferredSize(new Dimension(800, 600));
		if (rsp == null) {
			rsp = new RecordSweepPanel(options);
		}		
		tabbedPaneMain.addTab("Record Sweep",rsp);
		
		if (tdp == null) {
			tdp = new TargetDesignerPanel(options);
		}
		tabbedPaneMain.addTab("Target Designer", tdp);

		if (sfp == null) {
			sfp = new StandardFiltersPanel(options);
		}			
		tabbedPaneMain.addTab("Generate Standard Filters", sfp);		
		
		if (cfp == null) {
			cfp = new CustomizedFilterPanel(options);
		}
		tabbedPaneMain.addTab("Generate Custom Filters", cfp);

		return tabbedPaneMain;
	}

	/**
	 * This method initializes mnuDrcWrapper	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getMnuDrcWrapper() {
		if (mnuDrcWrapper == null) {
			mnuDrcWrapper = new JMenuBar();
			mnuDrcWrapper.add(getMnuOptions());
			mnuDrcWrapper.add(getMnuHelp());
		}
		return mnuDrcWrapper;
	}

	/**
	 * This method initializes mnuOptions	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getMnuOptions() {
		if (mnuOptions == null) {
			mnuOptions = new JMenu("Options");
			if (rdoGrpInterfaceDriver == null) {
				rdoGrpInterfaceDriver = new ButtonGroup();
			}
			rdoGrpInterfaceDriver.add(getMnuRdoAsio());
			rdoGrpInterfaceDriver.add(getMnuRdoDirectSound());
			mnuOptions.add(getMnuRdoAsio());
			mnuOptions.add(getMnuRdoDirectSound());

			mnuOptions.add(getMnuChkSavePcmFiles());

			mnuOptions.add(getMnuItmSetDrcDirectory());
		}
		return mnuOptions;
	}

	
	private JMenu getMnuHelp() {
		if (mnuHelp == null) {
			mnuHelp = new JMenu("Help");
			mnuHelp.add(getMnuItmDRCDesignerHelp());
			mnuHelp.add(getMnuItmAboutDRCDesigner());
		}
		return mnuHelp;
	}

	/**
	 * This method initializes mnuRdoAsio	
	 * 	
	 * @return javax.swing.JRadioButtonMenuItem	
	 */
	private JRadioButtonMenuItem getMnuRdoAsio() {
		if (mnuRdoAsio == null) {
			mnuRdoAsio = new JRadioButtonMenuItem("ASIO");
			mnuRdoAsio.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					options.setDriverType(Options.InterfaceDriverType.ASIO);
				}
			});
		}
		return mnuRdoAsio;
	}

	/**
	 * This method initializes mnuRdoDirectSound	
	 * 	
	 * @return javax.swing.JRadioButtonMenuItem	
	 */
	private JRadioButtonMenuItem getMnuRdoDirectSound() {
		if (mnuRdoDirectSound == null) {
			mnuRdoDirectSound = new JRadioButtonMenuItem("Direct Sound");
			mnuRdoDirectSound.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					options.setDriverType(Options.InterfaceDriverType.DIRECT_SOUND);
				}
			});
		}
		return mnuRdoDirectSound;
	}

	/**
	 * This method initializes mnuChkSavePcmFiles
	 *
	 * @return javax.swing.JRadioButtonMenuItem
	 */
	private JCheckBoxMenuItem getMnuChkSavePcmFiles() {
		if (mnuChkSavePcmFiles == null) {
			mnuChkSavePcmFiles = new JCheckBoxMenuItem("Save PCM Files");
			mnuChkSavePcmFiles.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					options.setSavePcmFiles(mnuChkSavePcmFiles.getState());
				}
			});
		}
		return mnuChkSavePcmFiles;
	}


	/**
	 * This method initializes mnuItmSetDrcDirectory	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getMnuItmSetDrcDirectory() {
		if (mnuItmSetDrcDirectory == null) {
			mnuItmSetDrcDirectory = new JMenuItem("Set DRC Application Directory");
			mnuItmSetDrcDirectory.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					fcDrcDirectory = new JFileChooser();
					fcDrcDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		            int retval = fcDrcDirectory.showOpenDialog(jContentPane);
		            if (retval == JFileChooser.APPROVE_OPTION) {
		            	options.setRoomCorrectionRoot(fcDrcDirectory.getSelectedFile());
		            }
				}
			});
		}
		return mnuItmSetDrcDirectory;
	}

	private JMenuItem getMnuItmDRCDesignerHelp() {
		if (mnuItmDRCDesignerHelp == null) {
			mnuItmDRCDesignerHelp = new JMenuItem("DRC Designer Help");
			mnuItmDRCDesignerHelp.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    try {
				        java.net.URL url = getClass().getResource("HelpFrameset.html");
				        @SuppressWarnings("unused")
						HelpWindow hw = new HelpWindow("Digital Room Correction Designer Help", url);
				    } catch (Exception exc) {
				        System.out.println(exc.getMessage());
				    }

				}
			});
		}
		return mnuItmDRCDesignerHelp;
	}

	private JMenuItem getMnuItmAboutDRCDesigner() {
		if (mnuItmAboutDRCDesigner == null) {
			mnuItmAboutDRCDesigner = new JMenuItem("About DRC Designer");
			mnuItmAboutDRCDesigner.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    try {
				        java.net.URL url = getClass().getResource("AboutDRCDesigner.html");
				        @SuppressWarnings("unused")
						HelpWindow hw = new HelpWindow("About Digital Room Correction Designer", url);
				    } catch (Exception exc) {
				        System.out.println(exc.getMessage());
				    }

				}
			});
		}
		return mnuItmAboutDRCDesigner;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DRCDesigner thisClass = new DRCDesigner();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	public DRCDesigner() {
		super();
        setIconImage(Toolkit.getDefaultToolkit().getImage(DRCDesigner.class.getResource("music_green.png")));
        options = new Options();
        options.initOptions();
		initialize();
        initializeOptions();
	}

	private void initializeOptions() {
		//TODO: change this to allow user to set room correction folder
        if (options.getRoomCorrectionRoot() == null) {
            //options.setRoomCorrectionRoot(new File("/DRCDesigner"));
        	options.setRoomCorrectionRoot(new File(System.getProperty("user.dir")));
        }
		//TODO: change this to allow user to set sound driver type in options menu
        if (options.getDriverType() != null) {
        	if (options.getDriverType() == Options.InterfaceDriverType.ASIO) {
        		mnuRdoAsio.setSelected(true);
        	}
        	else {
        		mnuRdoDirectSound.setSelected(true);
        	}
        }

		mnuChkSavePcmFiles.setSelected(options.isSavePcmFiles());
	}

	private void initialize() {
		this.setSize(800, 600);
		this.setJMenuBar(getMnuDrcWrapper());
		this.setContentPane(getJContentPane());
		this.setTitle("Digital Room Correction Designer");
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
			    options.saveOptions();
			}
		});
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridx = 0;
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getTabbedPaneMain(), gridBagConstraints);
		}
		return jContentPane;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
