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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.CardLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PredictedResponsePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final String VIEW_PREDICTED = "Predicted Response";
    private static final String VIEW_IMPULSE = "Impulse Response";
    private static final String VIEW_STEP = "Step Response";
    private static final String VIEW_DAC_RISK = "DAC Clip Risk (Absolute)";
    private static final String CARD_PREDICTED = "card_predicted";
    private static final String CARD_IMPULSE = "card_impulse";
    private static final String CARD_STEP = "card_step";
    private static final String CARD_DAC_RISK = "card_dac_risk";

    private final Options options;
    private PredictedResponseGraph predictedGraph;
    private ImpulseResponseGraph impulseGraph;
    private StepResponseGraph stepGraph;
    private DacClipRiskGraph dacClipRiskGraph;
    private JPanel graphHost;
    private JComboBox<String> graphModeCombo;
    private JComboBox<String> filterSetCombo;
    private JComboBox<String> sampleRateCombo;
    private JComboBox<String> impulseWindowCombo;
    private JCheckBox impulseBeforeLeftCheckBox;
    private JCheckBox impulseBeforeRightCheckBox;
    private JCheckBox impulseAfterLeftCheckBox;
    private JCheckBox impulseAfterRightCheckBox;
    private JLabel statusLabel;
    private boolean suppressControlEvents;
    private int impulseWindowSelectionMs = 10;
    private int stepWindowSelectionMs = 40;
    private PredictedResponsePreviewUtil.Result lastResult;

    public PredictedResponsePanel(Options options) {
        super();
        this.options = options;
        initialize();
        refreshGraph();
    }

    private void initialize() {
        this.setLayout(new GridBagLayout());

        GridBagConstraints graphConstraints = new GridBagConstraints();
        graphConstraints.gridx = 0;
        graphConstraints.gridy = 0;
        graphConstraints.gridwidth = 16;
        graphConstraints.fill = GridBagConstraints.BOTH;
        graphConstraints.weightx = 1.0;
        graphConstraints.weighty = 1.0;
        graphConstraints.insets = new Insets(4, 4, 4, 4);

        GridBagConstraints filterLabelConstraints = new GridBagConstraints();
        filterLabelConstraints.gridx = 0;
        filterLabelConstraints.gridy = 1;
        filterLabelConstraints.anchor = GridBagConstraints.WEST;
        filterLabelConstraints.insets = new Insets(0, 6, 6, 4);

        GridBagConstraints filterComboConstraints = new GridBagConstraints();
        filterComboConstraints.gridx = 1;
        filterComboConstraints.gridy = 1;
        filterComboConstraints.anchor = GridBagConstraints.WEST;
        filterComboConstraints.insets = new Insets(0, 0, 6, 6);

        GridBagConstraints viewLabelConstraints = new GridBagConstraints();
        viewLabelConstraints.gridx = 4;
        viewLabelConstraints.gridy = 1;
        viewLabelConstraints.anchor = GridBagConstraints.WEST;
        viewLabelConstraints.insets = new Insets(0, 6, 6, 4);

        GridBagConstraints viewComboConstraints = new GridBagConstraints();
        viewComboConstraints.gridx = 5;
        viewComboConstraints.gridy = 1;
        viewComboConstraints.anchor = GridBagConstraints.WEST;
        viewComboConstraints.insets = new Insets(0, 0, 6, 6);

        GridBagConstraints rateLabelConstraints = new GridBagConstraints();
        rateLabelConstraints.gridx = 2;
        rateLabelConstraints.gridy = 1;
        rateLabelConstraints.anchor = GridBagConstraints.WEST;
        rateLabelConstraints.insets = new Insets(0, 6, 6, 4);

        GridBagConstraints rateComboConstraints = new GridBagConstraints();
        rateComboConstraints.gridx = 3;
        rateComboConstraints.gridy = 1;
        rateComboConstraints.anchor = GridBagConstraints.WEST;
        rateComboConstraints.insets = new Insets(0, 0, 6, 6);

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 14;
        buttonConstraints.gridy = 1;
        buttonConstraints.anchor = GridBagConstraints.WEST;
        buttonConstraints.insets = new Insets(0, 6, 6, 6);

        GridBagConstraints statusConstraints = new GridBagConstraints();
        statusConstraints.gridx = 15;
        statusConstraints.gridy = 1;
        statusConstraints.anchor = GridBagConstraints.WEST;
        statusConstraints.weightx = 1.0;
        statusConstraints.insets = new Insets(0, 6, 6, 6);

        GridBagConstraints impulseWindowLabelConstraints = new GridBagConstraints();
        impulseWindowLabelConstraints.gridx = 6;
        impulseWindowLabelConstraints.gridy = 1;
        impulseWindowLabelConstraints.anchor = GridBagConstraints.WEST;
        impulseWindowLabelConstraints.insets = new Insets(0, 6, 6, 4);

        GridBagConstraints impulseWindowComboConstraints = new GridBagConstraints();
        impulseWindowComboConstraints.gridx = 7;
        impulseWindowComboConstraints.gridy = 1;
        impulseWindowComboConstraints.anchor = GridBagConstraints.WEST;
        impulseWindowComboConstraints.insets = new Insets(0, 0, 6, 6);

        GridBagConstraints impulseBeforeLeftConstraints = new GridBagConstraints();
        impulseBeforeLeftConstraints.gridx = 8;
        impulseBeforeLeftConstraints.gridy = 1;
        impulseBeforeLeftConstraints.anchor = GridBagConstraints.WEST;
        impulseBeforeLeftConstraints.insets = new Insets(0, 6, 6, 4);

        GridBagConstraints impulseBeforeRightConstraints = new GridBagConstraints();
        impulseBeforeRightConstraints.gridx = 9;
        impulseBeforeRightConstraints.gridy = 1;
        impulseBeforeRightConstraints.anchor = GridBagConstraints.WEST;
        impulseBeforeRightConstraints.insets = new Insets(0, 0, 6, 4);

        GridBagConstraints impulseAfterLeftConstraints = new GridBagConstraints();
        impulseAfterLeftConstraints.gridx = 10;
        impulseAfterLeftConstraints.gridy = 1;
        impulseAfterLeftConstraints.anchor = GridBagConstraints.WEST;
        impulseAfterLeftConstraints.insets = new Insets(0, 0, 6, 4);

        GridBagConstraints impulseAfterRightConstraints = new GridBagConstraints();
        impulseAfterRightConstraints.gridx = 11;
        impulseAfterRightConstraints.gridy = 1;
        impulseAfterRightConstraints.anchor = GridBagConstraints.WEST;
        impulseAfterRightConstraints.insets = new Insets(0, 0, 6, 4);

        this.add(getGraphHost(), graphConstraints);
        this.add(new JLabel("Filter:"), filterLabelConstraints);
        this.add(getFilterSetCombo(), filterComboConstraints);
        this.add(new JLabel("Rate:"), rateLabelConstraints);
        this.add(getSampleRateCombo(), rateComboConstraints);
        this.add(new JLabel("View:"), viewLabelConstraints);
        this.add(getGraphModeCombo(), viewComboConstraints);
        this.add(new JLabel("Impulse window:"), impulseWindowLabelConstraints);
        this.add(getImpulseWindowCombo(), impulseWindowComboConstraints);
        this.add(getImpulseBeforeLeftCheckBox(), impulseBeforeLeftConstraints);
        this.add(getImpulseBeforeRightCheckBox(), impulseBeforeRightConstraints);
        this.add(getImpulseAfterLeftCheckBox(), impulseAfterLeftConstraints);
        this.add(getImpulseAfterRightCheckBox(), impulseAfterRightConstraints);
        this.add(getRefreshButton(), buttonConstraints);
        this.add(getStatusLabel(), statusConstraints);

        refreshFilterSetOptions();
        refreshSampleRateOptions();
        updateGraphMode();
    }

    private PredictedResponseGraph getPredictedGraph() {
        if (predictedGraph == null) {
            predictedGraph = new PredictedResponseGraph();
        }
        return predictedGraph;
    }

    private ImpulseResponseGraph getImpulseGraph() {
        if (impulseGraph == null) {
            impulseGraph = new ImpulseResponseGraph();
        }
        return impulseGraph;
    }

    private StepResponseGraph getStepGraph() {
        if (stepGraph == null) {
            stepGraph = new StepResponseGraph();
        }
        return stepGraph;
    }

    private DacClipRiskGraph getDacClipRiskGraph() {
        if (dacClipRiskGraph == null) {
            dacClipRiskGraph = new DacClipRiskGraph();
        }
        return dacClipRiskGraph;
    }

    private JPanel getGraphHost() {
        if (graphHost == null) {
            graphHost = new JPanel(new CardLayout());
            graphHost.add(getPredictedGraph(), CARD_PREDICTED);
            graphHost.add(getImpulseGraph(), CARD_IMPULSE);
            graphHost.add(getStepGraph(), CARD_STEP);
            graphHost.add(getDacClipRiskGraph(), CARD_DAC_RISK);
        }
        return graphHost;
    }

    private JLabel getStatusLabel() {
        if (statusLabel == null) {
            statusLabel = new JLabel("Status: waiting for impulse/filter files");
            statusLabel.setToolTipText(statusLabel.getText());
        }
        return statusLabel;
    }

    private void setStatusText(String statusText) {
        JLabel label = getStatusLabel();
        label.setText(statusText);
        label.setToolTipText(statusText);
    }

    private JComboBox<String> getFilterSetCombo() {
        if (filterSetCombo == null) {
            filterSetCombo = new JComboBox<String>();
            filterSetCombo.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (suppressControlEvents) {
                        return;
                    }
                    refreshSampleRateOptions();
                    refreshGraph();
                }
            });
        }
        return filterSetCombo;
    }

    private JComboBox<String> getSampleRateCombo() {
        if (sampleRateCombo == null) {
            sampleRateCombo = new JComboBox<String>();
            sampleRateCombo.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (suppressControlEvents) {
                        return;
                    }
                    refreshGraph();
                }
            });
        }
        return sampleRateCombo;
    }

    private JComboBox<String> getGraphModeCombo() {
        if (graphModeCombo == null) {
            graphModeCombo = new JComboBox<String>(new String[] {VIEW_PREDICTED, VIEW_IMPULSE, VIEW_STEP, VIEW_DAC_RISK});
            graphModeCombo.setSelectedItem(VIEW_PREDICTED);
            graphModeCombo.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (suppressControlEvents) {
                        return;
                    }
                    updateGraphMode();
                }
            });
        }
        return graphModeCombo;
    }

    private JComboBox<String> getImpulseWindowCombo() {
        if (impulseWindowCombo == null) {
            impulseWindowCombo = new JComboBox<String>(new String[] {"1 ms", "3 ms", "5 ms", "10 ms", "20 ms", "40 ms", "60 ms", "120 ms", "250 ms"});
            impulseWindowCombo.setSelectedItem("10 ms");
            impulseWindowCombo.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (suppressControlEvents) {
                        return;
                    }

                    int selectedMs = parseWindowMs((String) getImpulseWindowCombo().getSelectedItem(), 10);
                    String mode = (String) getGraphModeCombo().getSelectedItem();
                    if (VIEW_STEP.equals(mode)) {
                        stepWindowSelectionMs = selectedMs;
                    }
                    else {
                        impulseWindowSelectionMs = selectedMs;
                    }

                    updateImpulseGraphSettings();
                    updateStatusForCurrentMode();
                }
            });
        }
        return impulseWindowCombo;
    }

    private JCheckBox getImpulseBeforeLeftCheckBox() {
        if (impulseBeforeLeftCheckBox == null) {
            impulseBeforeLeftCheckBox = createImpulseTraceCheckBox("B-L", false);
            impulseBeforeLeftCheckBox.setToolTipText("Left channel before correction");
        }
        return impulseBeforeLeftCheckBox;
    }

    private JCheckBox getImpulseBeforeRightCheckBox() {
        if (impulseBeforeRightCheckBox == null) {
            impulseBeforeRightCheckBox = createImpulseTraceCheckBox("B-R", false);
            impulseBeforeRightCheckBox.setToolTipText("Right channel before correction");
        }
        return impulseBeforeRightCheckBox;
    }

    private JCheckBox getImpulseAfterLeftCheckBox() {
        if (impulseAfterLeftCheckBox == null) {
            impulseAfterLeftCheckBox = createImpulseTraceCheckBox("A-L", true);
            impulseAfterLeftCheckBox.setToolTipText("Left channel after correction");
        }
        return impulseAfterLeftCheckBox;
    }

    private JCheckBox getImpulseAfterRightCheckBox() {
        if (impulseAfterRightCheckBox == null) {
            impulseAfterRightCheckBox = createImpulseTraceCheckBox("A-R", true);
            impulseAfterRightCheckBox.setToolTipText("Right channel after correction");
        }
        return impulseAfterRightCheckBox;
    }

    private JCheckBox createImpulseTraceCheckBox(String label, boolean selected) {
        JCheckBox checkBox = new JCheckBox(label, selected);
        checkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (suppressControlEvents) {
                    return;
                }
                updateImpulseGraphTraceVisibility();
                updateStatusForCurrentMode();
            }
        });
        return checkBox;
    }

    private void updateGraphMode() {
        String mode = (String) getGraphModeCombo().getSelectedItem();
        CardLayout cardLayout = (CardLayout) getGraphHost().getLayout();
        if (VIEW_STEP.equals(mode)) {
            cardLayout.show(getGraphHost(), CARD_STEP);
        }
        else if (VIEW_IMPULSE.equals(mode)) {
            cardLayout.show(getGraphHost(), CARD_IMPULSE);
        }
        else if (VIEW_DAC_RISK.equals(mode)) {
            cardLayout.show(getGraphHost(), CARD_DAC_RISK);
        }
        else {
            cardLayout.show(getGraphHost(), CARD_PREDICTED);
        }

        syncWindowSelectionForCurrentMode();
        updateImpulseControlVisibility();
        updateStatusForCurrentMode();
    }

    private void syncWindowSelectionForCurrentMode() {
        String mode = (String) getGraphModeCombo().getSelectedItem();
        if (!VIEW_IMPULSE.equals(mode) && !VIEW_STEP.equals(mode)) {
            return;
        }

        int windowMs = VIEW_STEP.equals(mode) ? stepWindowSelectionMs : impulseWindowSelectionMs;
        String targetLabel = Integer.toString(windowMs) + " ms";

        suppressControlEvents = true;
        try {
            getImpulseWindowCombo().setSelectedItem(targetLabel);
        }
        finally {
            suppressControlEvents = false;
        }

        updateImpulseGraphSettings();
    }

    private void updateImpulseControlVisibility() {
        String mode = (String) getGraphModeCombo().getSelectedItem();
        boolean visible = VIEW_IMPULSE.equals(mode) || VIEW_STEP.equals(mode);
        getImpulseWindowCombo().setVisible(visible);
        getImpulseBeforeLeftCheckBox().setVisible(visible);
        getImpulseBeforeRightCheckBox().setVisible(visible);
        getImpulseAfterLeftCheckBox().setVisible(visible);
        getImpulseAfterRightCheckBox().setVisible(visible);
    }

    private void updateImpulseGraphSettings() {
        int windowMs = parseWindowMs((String) getImpulseWindowCombo().getSelectedItem(), 120);
        getImpulseGraph().setDisplayWindowMs(windowMs);
        getStepGraph().setDisplayWindowMs(windowMs);
    }

    private void updateImpulseGraphTraceVisibility() {
        getImpulseGraph().setCurveVisibility(getImpulseBeforeLeftCheckBox().isSelected(),
                getImpulseBeforeRightCheckBox().isSelected(),
                getImpulseAfterLeftCheckBox().isSelected(),
                getImpulseAfterRightCheckBox().isSelected());
        getStepGraph().setCurveVisibility(getImpulseBeforeLeftCheckBox().isSelected(),
            getImpulseBeforeRightCheckBox().isSelected(),
            getImpulseAfterLeftCheckBox().isSelected(),
            getImpulseAfterRightCheckBox().isSelected());
    }

    private int parseWindowMs(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        String digits = value.replaceAll("[^0-9]", "");
        if (digits.length() == 0) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(digits);
        }
        catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    private void refreshFilterSetOptions() {
        String previousSelection = null;
        if (filterSetCombo != null && filterSetCombo.getSelectedItem() != null) {
            previousSelection = filterSetCombo.getSelectedItem().toString();
        }

        JComboBox<String> combo = getFilterSetCombo();
        suppressControlEvents = true;
        try {
            combo.removeAllItems();

            String[] labels = PredictedResponsePreviewUtil.getAvailableFilterLabels(options);
            for (String label : labels) {
                combo.addItem(label);
            }

            if (labels.length == 0) {
                combo.addItem("(none found)");
                combo.setEnabled(false);
                return;
            }

            combo.setEnabled(true);
            if (previousSelection != null) {
                for (int i = 0; i < combo.getItemCount(); i++) {
                    if (previousSelection.equalsIgnoreCase(combo.getItemAt(i))) {
                        combo.setSelectedIndex(i);
                        return;
                    }
                }
            }

            combo.setSelectedIndex(0);
        }
        finally {
            suppressControlEvents = false;
        }
    }

    private void refreshSampleRateOptions() {
        String previousSelection = null;
        if (sampleRateCombo != null && sampleRateCombo.getSelectedItem() != null) {
            previousSelection = sampleRateCombo.getSelectedItem().toString();
        }

        String selectedLabel = null;
        if (getFilterSetCombo().isEnabled() && getFilterSetCombo().getSelectedItem() != null) {
            selectedLabel = getFilterSetCombo().getSelectedItem().toString();
        }

        JComboBox<String> combo = getSampleRateCombo();
        suppressControlEvents = true;
        try {
            combo.removeAllItems();
            combo.addItem("Auto");

            String[] rates = PredictedResponsePreviewUtil.getAvailableSampleRates(options, selectedLabel);
            for (int i = 0; i < rates.length; i++) {
                combo.addItem(rates[i]);
            }

            if (combo.getItemCount() == 0) {
                combo.addItem("Auto");
            }

            combo.setEnabled(true);

            if (previousSelection != null) {
                for (int i = 0; i < combo.getItemCount(); i++) {
                    if (previousSelection.equalsIgnoreCase(combo.getItemAt(i))) {
                        combo.setSelectedIndex(i);
                        return;
                    }
                }
            }

            combo.setSelectedItem("Auto");
        }
        finally {
            suppressControlEvents = false;
        }
    }

    private JButton getRefreshButton() {
        JButton button = new JButton("Refresh Prediction");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                refreshGraph();
            }
        });
        return button;
    }

    public void refreshGraph() {
        refreshFilterSetOptions();
        refreshSampleRateOptions();

        String selectedLabel = null;
        if (getFilterSetCombo().isEnabled() && getFilterSetCombo().getSelectedItem() != null) {
            selectedLabel = getFilterSetCombo().getSelectedItem().toString();
        }

        String selectedSampleRate = null;
        if (getSampleRateCombo().isEnabled() && getSampleRateCombo().getSelectedItem() != null) {
            String rate = getSampleRateCombo().getSelectedItem().toString();
            if (!"Auto".equalsIgnoreCase(rate)) {
                selectedSampleRate = rate;
            }
        }

        PredictedResponsePreviewUtil.Result result = PredictedResponsePreviewUtil.compute(options, selectedLabel, selectedSampleRate);
        if (result == null) {
            lastResult = null;
            getPredictedGraph().setCurves(null, null, null, null);
            getImpulseGraph().setCurves(null, null, null, null, 0);
            getStepGraph().setCurves(null, null, null, null, 0);
            getDacClipRiskGraph().setCurves(null, null);
            setStatusText("Status: prediction unavailable (missing impulse/filter match)");
            return;
        }

        lastResult = result;

        getPredictedGraph().setCurves(result.getMeasuredLeft(), result.getMeasuredRight(), result.getPredictedLeft(), result.getPredictedRight());
        getImpulseGraph().setCurves(result.getMeasuredLeftImpulse(), result.getMeasuredRightImpulse(), result.getPredictedLeftImpulse(), result.getPredictedRightImpulse(), result.getSampleRateHz());
        getStepGraph().setCurves(result.getMeasuredLeftImpulse(), result.getMeasuredRightImpulse(), result.getPredictedLeftImpulse(), result.getPredictedRightImpulse(), result.getSampleRateHz());
        updateImpulseGraphSettings();
        updateImpulseGraphTraceVisibility();
        getDacClipRiskGraph().setCurves(result.getDacClipRiskLeft(), result.getDacClipRiskRight());

        updateStatusForCurrentMode();
    }

    private void updateStatusForCurrentMode() {
        if (lastResult == null) {
            return;
        }

        String mode = (String) getGraphModeCombo().getSelectedItem();
        String prefix = "Status: " + formatRateKhz(lastResult.getSampleRate()) + "  " + lastResult.getFilterLabel();

        if (VIEW_DAC_RISK.equals(mode)) {
            double maxDacRisk = findMaxAmplitude(lastResult.getDacClipRiskLeft(), lastResult.getDacClipRiskRight());
            setStatusText(prefix
                + "  risk " + formatDbCompact(maxDacRisk)
                + "  preatten>= " + formatDbCompact(lastResult.getRequiredPreattenuationDb())
                + "  rec " + formatDbCompact(lastResult.getRecommendedPreattenuationDb()));
            return;
        }

        if (VIEW_IMPULSE.equals(mode)) {
            double beforePeak = Math.max(findPeakAbs(lastResult.getMeasuredLeftImpulse()), findPeakAbs(lastResult.getMeasuredRightImpulse()));
            double afterPeak = Math.max(findPeakAbs(lastResult.getPredictedLeftImpulse()), findPeakAbs(lastResult.getPredictedRightImpulse()));
            setStatusText(prefix
                    + "  impulse before pk " + formatAmpCompact(beforePeak)
                    + "  after pk " + formatAmpCompact(afterPeak)
                    + "  win " + Integer.toString(getImpulseGraph().getDisplayWindowMs()) + "ms"
                    + "  peak-centered");
            return;
        }

        if (VIEW_STEP.equals(mode)) {
            setStatusText(prefix
                + "  step response"
                + "  win " + Integer.toString(getStepGraph().getDisplayWindowMs()) + "ms"
                + "  peak-centered");
            return;
        }

        setStatusText(prefix + "  predicted L/R vs measured");
    }

    private double findMaxAmplitude(FrequencyAmplitudePoints leftPoints, FrequencyAmplitudePoints rightPoints) {
        double max = 0.0;
        max = Math.max(max, findMaxAmplitude(leftPoints));
        max = Math.max(max, findMaxAmplitude(rightPoints));
        return max;
    }

    private double findMaxAmplitude(FrequencyAmplitudePoints points) {
        if (points == null) {
            return 0.0;
        }

        double max = 0.0;
        int count = points.getNumberOfFrequencyDataPoints();
        for (int i = 1; i <= count; i++) {
            max = Math.max(max, points.getFrequencyAmplitudePoint(i).getAmplitude());
        }
        return max;
    }

    private String formatDb(double value) {
        return String.format("%.2f dB", value);
    }

    private String formatDbCompact(double value) {
        return String.format("%.1fdB", value);
    }

    private String formatRateKhz(String sampleRate) {
        try {
            int rate = Integer.parseInt(sampleRate);
            return String.format("%.1fk", rate / 1000.0);
        }
        catch (NumberFormatException nfe) {
            return sampleRate;
        }
    }

    private double findPeakAbs(double[] samples) {
        if (samples == null) {
            return 0.0;
        }

        double peak = 0.0;
        for (int i = 0; i < samples.length; i++) {
            peak = Math.max(peak, Math.abs(samples[i]));
        }
        return peak;
    }

    private String formatAmpCompact(double value) {
        return String.format("%.3f", value);
    }



}
