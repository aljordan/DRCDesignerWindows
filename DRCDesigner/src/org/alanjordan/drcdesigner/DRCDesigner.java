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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JMenuItem;

public class DRCDesigner extends JFrame {
	private static final Logger LOGGER = AppLogger.getLogger();

	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_WINDOW_WIDTH = 1000;
	private static final int DEFAULT_WINDOW_HEIGHT = 600;
	private JPanel jContentPane = null;
	private JTabbedPane tabbedPaneMain = null;
	private RecordSweepPanel rsp = null;
	private StandardFiltersPanel sfp = null;
	private CustomizedFilterPanel cfp = null;
	private TargetDesignerPanel tdp = null;
	private PredictedResponsePanel prp = null;
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
		tabbedPaneMain.setPreferredSize(new Dimension(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));
		if (rsp == null) {
			rsp = new RecordSweepPanel(options);
		}		
		tabbedPaneMain.addTab("Record Sweep",rsp);
		
		if (tdp == null) {
			tdp = new TargetDesignerPanel(options);
		}
		tabbedPaneMain.addTab("Target Designer", tdp);

		if (prp == null) {
			prp = new PredictedResponsePanel(options);
		}

		if (sfp == null) {
			sfp = new StandardFiltersPanel(options);
		}			
		tabbedPaneMain.addTab("Generate Standard Filters", sfp);		
		
		if (cfp == null) {
			cfp = new CustomizedFilterPanel(options);
		}
		tabbedPaneMain.addTab("Generate Custom Filters", cfp);
		tabbedPaneMain.addTab("Predicted Response", prp);

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
		            	options.setRoomCorrectionRoot(resolveSelectedRoot(fcDrcDirectory.getSelectedFile()));
		            	loadExistingResponseCurves();
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
		LOGGER.info("Starting DRCDesigner");
        options = new Options();
        options.initOptions();
		initialize();
        initializeOptions();
		loadExistingResponseCurves();
	}

	private void loadExistingResponseCurves() {
		String sampleRate = ResponseCurveAnalysisUtil.findBestExistingResponseSampleRate(options);
		if (sampleRate == null) {
			options.setLeftChannelResponsePoints(null);
			options.setRightChannelResponsePoints(null);
			return;
		}

		ResponseCurveAnalysisUtil.analyzeAndStoreResponseCurves(options, sampleRate);
		if (tdp != null) {
			tdp.repaint();
		}
	}

	private void initializeOptions() {
		File bundledRoot = detectBundledRoot();
		LOGGER.info("Bundled root: " + (bundledRoot != null ? bundledRoot.getAbsolutePath() : "not detected"));
		File writableRoot = null;
		if (bundledRoot != null) {
			writableRoot = new File(Options.getAppDataDirectory(), "RoomCorrectionRoot");
			ensureWritableRootInitialized(bundledRoot, writableRoot);
			LOGGER.info("Writable root initialized at: " + writableRoot.getAbsolutePath());
		}

		if (writableRoot != null && options.getRoomCorrectionRoot() != null) {
			File currentRoot = options.getRoomCorrectionRoot();
			File installRoot = bundledRoot.getParentFile();
			if (isSameOrDescendant(currentRoot, bundledRoot)
					|| (installRoot != null && isSameOrDescendant(currentRoot, installRoot))) {
				options.setRoomCorrectionRoot(writableRoot);
			}
		}

        if (options.getRoomCorrectionRoot() == null) {
			options.setRoomCorrectionRoot(determineDefaultRoomCorrectionRoot());
        }
		LOGGER.info("Active room-correction root: " + options.getRoomCorrectionRootPath());
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

	private File determineDefaultRoomCorrectionRoot() {
		File bundledRoot = detectBundledRoot();
		if (bundledRoot != null) {
			File writableRoot = new File(Options.getAppDataDirectory(), "RoomCorrectionRoot");
			ensureWritableRootInitialized(bundledRoot, writableRoot);
			return writableRoot;
		}

		return new File(System.getProperty("user.dir"));
	}

	private File detectBundledRoot() {
		String jpackageAppPath = System.getProperty("jpackage.app-path");
		if (jpackageAppPath == null || jpackageAppPath.length() == 0) {
			return null;
		}

		File launcherPath = new File(jpackageAppPath);
		File installRoot = launcherPath.getParentFile();
		if (installRoot == null) {
			return null;
		}

		File appDir = new File(installRoot, "app");
		if (appDir.exists() && appDir.isDirectory()) {
			return appDir;
		}

		if (installRoot.exists() && installRoot.isDirectory()) {
			return installRoot;
		}

		return null;
	}

	private File resolveSelectedRoot(File selectedRoot) {
		File bundledRoot = detectBundledRoot();
		if (bundledRoot == null || selectedRoot == null) {
			return selectedRoot;
		}

		File installRoot = bundledRoot.getParentFile();
		if (isSameOrDescendant(selectedRoot, bundledRoot)
				|| (installRoot != null && isSameOrDescendant(selectedRoot, installRoot))) {
			File writableRoot = new File(Options.getAppDataDirectory(), "RoomCorrectionRoot");
			ensureWritableRootInitialized(bundledRoot, writableRoot);
			return writableRoot;
		}

		return selectedRoot;
	}

	private boolean isSameOrDescendant(File candidate, File ancestor) {
		if (candidate == null || ancestor == null) {
			return false;
		}

		Path candidatePath = candidate.toPath().toAbsolutePath().normalize();
		Path ancestorPath = ancestor.toPath().toAbsolutePath().normalize();
		return candidatePath.startsWith(ancestorPath);
	}

	private void ensureWritableRootInitialized(File bundledRoot, File writableRoot) {
		if (!writableRoot.exists()) {
			writableRoot.mkdirs();
		}

		String[] entriesToRefresh = new String[] {
			"Rec_imp.win32",
			"drc-3.2.3",
			"sox-14.3.2",
			"rt",
			"convolver4-4vc++.zip"
		};

		for (String entryName : entriesToRefresh) {
			File source = new File(bundledRoot, entryName);
			if (!source.exists()) {
				continue;
			}

			File target = new File(writableRoot, entryName);
			try {
				copyMissingOrUpdated(source.toPath(), target.toPath(), true);
			}
			catch (IOException ioe) {
				LOGGER.warning("Unable to refresh writable root entry '" + entryName + "': " + ioe.getMessage());
			}
		}

		File convolverSource = new File(bundledRoot, "ConvolverFilters");
		if (convolverSource.exists()) {
			File convolverTarget = new File(writableRoot, "ConvolverFilters");
			try {
				copyMissingOrUpdated(convolverSource.toPath(), convolverTarget.toPath(), false);
			}
			catch (IOException ioe) {
				LOGGER.warning("Unable to initialize writable ConvolverFilters entry: " + ioe.getMessage());
			}
		}
	}

	private void copyMissingOrUpdated(Path source, Path target, boolean overwriteExistingFiles) throws IOException {
		if (Files.isDirectory(source)) {
			if (!Files.exists(target)) {
				Files.createDirectories(target);
			}

			Files.walk(source).forEach(path -> {
				try {
					Path relative = source.relativize(path);
					Path dest = target.resolve(relative);
					if (Files.isDirectory(path)) {
						if (!Files.exists(dest)) {
							Files.createDirectories(dest);
						}
					}
					else if (!Files.exists(dest)) {
						Files.copy(path, dest, StandardCopyOption.COPY_ATTRIBUTES);
					}
					else if (overwriteExistingFiles && shouldReplace(path, dest)) {
						Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
					}
				}
				catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			});
		}
		else {
			if (target.getParent() != null && !Files.exists(target.getParent())) {
				Files.createDirectories(target.getParent());
			}

			if (!Files.exists(target)) {
				Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
			}
			else if (overwriteExistingFiles && shouldReplace(source, target)) {
				Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			}
		}
	}

	private boolean shouldReplace(Path source, Path target) throws IOException {
		long sourceSize = Files.size(source);
		long targetSize = Files.size(target);
		if (sourceSize != targetSize) {
			return true;
		}

		return Files.getLastModifiedTime(source).toMillis() > Files.getLastModifiedTime(target).toMillis();
	}

	private void initialize() {
		this.setSize(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
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
