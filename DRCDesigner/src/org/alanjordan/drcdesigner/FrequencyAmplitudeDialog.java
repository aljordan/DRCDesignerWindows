/*
  Copyright 2020 Alan Brent Jordan
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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Frame;
import java.awt.Point;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Frame;
import java.awt.Point;


public class FrequencyAmplitudeDialog extends JDialog implements ActionListener {
    private JTextField frequencyField;
    private JTextField amplitudeField;
    private JButton btnOk;
    private JButton btnCancel;
    private JPanel faPanel;
    private FrequencyAmplitudePoint editedPoint;
    private FrequencyAmplitudePoint originalPoint;


    public FrequencyAmplitudeDialog(JFrame parent, FrequencyAmplitudePoint faPoint) {
        super(parent,"Edit Node", true);
        originalPoint = faPoint;
        createDialog(parent);
    }

//    private void createDialogOld() {
//        frequencyField = new JTextField(Double.toString(frequency),10);
//        amplitudeField = new JTextField(Double.toString(amplitude),10);
//        faPanel = new JPanel();
//        faPanel.add(new JLabel("Frequency:"));
//        faPanel.add(frequencyField);
//        faPanel.add(Box.createHorizontalStrut(15)); // a spacer
//        faPanel.add(new JLabel("Amplitude:"));
//        faPanel.add(amplitudeField);
//    }

    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        if (source == btnOk) {
            try {
                editedPoint = new FrequencyAmplitudePoint(Double.parseDouble(frequencyField.getText()),
                        Double.parseDouble(amplitudeField.getText()));
            } catch (NumberFormatException nfexc) {
                editedPoint = null;
            }
        }
        else {
            //editedPoint = new FrequencyAmplitudePoint(originalPoint.getFrequency(), originalPoint.getAmplitude());
            editedPoint = null;
        }
        dispose();
    }
    public FrequencyAmplitudePoint run() {
        this.setVisible(true);
        return editedPoint;
    }

    public void createDialog(JFrame parent) {
        frequencyField = new JTextField(Double.toString(originalPoint.getFrequency()),10);
        amplitudeField = new JTextField(Double.toString(originalPoint.getAmplitude()),10);
        Point loc = parent.getLocation();
        setLocation(loc.x+80,loc.y+80);
        faPanel = new JPanel();
        faPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2,2,2,2);
        JLabel frequencyLabel = new JLabel("Frequency:");
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        faPanel.add(frequencyLabel,gbc);
        gbc.gridwidth = 2;
        gbc.gridx = 1;
        gbc.gridy = 0;
        faPanel.add(frequencyField,gbc);
        JLabel amplitudeLabel = new JLabel("Amplitude:");
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        faPanel.add(amplitudeLabel,gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 1;
        gbc.gridy = 1;
        faPanel.add(amplitudeField,gbc);
        JLabel spacer = new JLabel(" ");
        gbc.gridx = 0;
        gbc.gridy = 2;
        faPanel.add(spacer,gbc);
        btnOk = new JButton("Ok");
        btnOk.addActionListener(this);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        faPanel.add(btnOk,gbc);
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(this);
        gbc.gridx = 1;
        gbc.gridy = 3;
        faPanel.add(btnCancel,gbc);
        getContentPane().add(faPanel);
        pack();
    }

}
