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

import java.io.File;
import java.util.logging.Logger;

public final class ResponseCurveAnalysisUtil {
    private static final Logger LOGGER = AppLogger.getLogger();
    private static final String[] RESPONSE_SAMPLE_RATES = {"44100", "48000", "88200", "96000"};
    private static final int GRAPH_POINT_COUNT = 450;

    public enum DisplaySmoothingPreset {
        NONE("NONE", "None", PcmResponseAnalyzer.SmoothingMode.NONE, 1.0),
        OCTAVE_24("OCTAVE_24", "1/24 octave", PcmResponseAnalyzer.SmoothingMode.FRACTIONAL_OCTAVE, 0.25),
        OCTAVE_12("OCTAVE_12", "1/12 octave", PcmResponseAnalyzer.SmoothingMode.FRACTIONAL_OCTAVE, 0.5),
        OCTAVE_6("OCTAVE_6", "1/6 octave", PcmResponseAnalyzer.SmoothingMode.FRACTIONAL_OCTAVE, 1.0),
        ERB("ERB", "ERB", PcmResponseAnalyzer.SmoothingMode.ERB, 1.0);

        private final String id;
        private final String displayLabel;
        private final PcmResponseAnalyzer.SmoothingMode smoothingMode;
        private final double smoothingStrength;

        DisplaySmoothingPreset(String id, String displayLabel, PcmResponseAnalyzer.SmoothingMode smoothingMode, double smoothingStrength) {
            this.id = id;
            this.displayLabel = displayLabel;
            this.smoothingMode = smoothingMode;
            this.smoothingStrength = smoothingStrength;
        }

        public String getId() {
            return id;
        }

        public String getDisplayLabel() {
            return displayLabel;
        }

        public PcmResponseAnalyzer.SmoothingMode getSmoothingMode() {
            return smoothingMode;
        }

        public double getSmoothingStrength() {
            return smoothingStrength;
        }

        public static DisplaySmoothingPreset fromId(String id) {
            if (id == null) {
                return OCTAVE_12;
            }

            for (DisplaySmoothingPreset preset : values()) {
                if (preset.id.equalsIgnoreCase(id)) {
                    return preset;
                }
            }

            return OCTAVE_12;
        }

        public static DisplaySmoothingPreset fromDisplayLabel(String label) {
            if (label == null) {
                return OCTAVE_12;
            }

            for (DisplaySmoothingPreset preset : values()) {
                if (preset.displayLabel.equalsIgnoreCase(label)) {
                    return preset;
                }
            }

            return OCTAVE_12;
        }
    }

    private ResponseCurveAnalysisUtil() {
    }

    public static String[] getDisplaySmoothingLabels() {
        DisplaySmoothingPreset[] presets = DisplaySmoothingPreset.values();
        String[] labels = new String[presets.length];
        for (int i = 0; i < presets.length; i++) {
            labels[i] = presets[i].getDisplayLabel();
        }
        return labels;
    }

    public static String getDisplayLabelForPresetId(String presetId) {
        return DisplaySmoothingPreset.fromId(presetId).getDisplayLabel();
    }

    public static String getPresetIdForDisplayLabel(String displayLabel) {
        return DisplaySmoothingPreset.fromDisplayLabel(displayLabel).getId();
    }

    public static void analyzeAndStoreBestExistingResponseCurves(Options options) {
        String sampleRate = findBestExistingResponseSampleRate(options);
        if (sampleRate == null) {
            options.setLeftChannelResponsePoints(null);
            options.setRightChannelResponsePoints(null);
            return;
        }

        analyzeAndStoreResponseCurves(options, sampleRate);
    }

    public static void analyzeAndStoreResponseCurves(Options options, String selectedRate) {
        if (options == null || selectedRate == null || options.getRoomCorrectionRootPath() == null) {
            return;
        }

        try {
            int sampleRate = Integer.parseInt(selectedRate);
            PcmResponseAnalyzer analyzer = new PcmResponseAnalyzer();
            PcmResponseAnalyzer.AnalysisSettings settings = buildAnalysisSettings(sampleRate, options.getResponseSmoothingPreset());

            File leftFile = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\LeftSpeakerImpulseResponse" + selectedRate + ".pcm");
            File rightFile = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\RightSpeakerImpulseResponse" + selectedRate + ".pcm");

            PcmResponseAnalyzer.ResponseCurve leftCurve = analyzeCurveFile(analyzer, leftFile, sampleRate, settings);
            PcmResponseAnalyzer.ResponseCurve rightCurve = analyzeCurveFile(analyzer, rightFile, sampleRate, settings);
            applySharedNormalizationAndStore(options, leftCurve, rightCurve);
        }
        catch (Exception exc) {
            LOGGER.warning("Unable to analyze and store response curves: " + exc.getMessage());
        }
    }

    public static String findBestExistingResponseSampleRate(Options options) {
        if (options == null || options.getRoomCorrectionRootPath() == null) {
            return null;
        }

        String bestRate = null;
        long bestTimestamp = Long.MIN_VALUE;

        for (String sampleRate : RESPONSE_SAMPLE_RATES) {
            File leftFile = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\LeftSpeakerImpulseResponse" + sampleRate + ".pcm");
            File rightFile = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\RightSpeakerImpulseResponse" + sampleRate + ".pcm");
            if (!leftFile.exists() || !rightFile.exists()) {
                continue;
            }

            long pairTimestamp = Math.min(leftFile.lastModified(), rightFile.lastModified());
            if (pairTimestamp > bestTimestamp) {
                bestTimestamp = pairTimestamp;
                bestRate = sampleRate;
            }
        }

        return bestRate;
    }

    private static PcmResponseAnalyzer.AnalysisSettings buildAnalysisSettings(int sampleRate, String presetId) {
        DisplaySmoothingPreset preset = DisplaySmoothingPreset.fromId(presetId);
        PcmResponseAnalyzer.AnalysisSettings settings = new PcmResponseAnalyzer.AnalysisSettings();
        settings.setPcmEncoding(PcmResponseAnalyzer.PcmEncoding.FLOAT32_LE);
        settings.setFftSize(65536);
        settings.setSmoothingMode(preset.getSmoothingMode());
        settings.setSmoothingStrength(preset.getSmoothingStrength());
        settings.setNormalizeToPeak(false);
        settings.setMinFrequencyHz(20.0);
        settings.setMaxFrequencyHz(ResponseDisplayFrequencyCap.getEffectiveCapForSampleRate(sampleRate));
        return settings;
    }

    private static PcmResponseAnalyzer.ResponseCurve analyzeCurveFile(PcmResponseAnalyzer analyzer, File file, int sampleRate, PcmResponseAnalyzer.AnalysisSettings settings) {
        if (file == null || !file.exists()) {
            return null;
        }

        try {
            return analyzer.analyzeFile(file, sampleRate, settings);
        }
        catch (Exception exc) {
            LOGGER.warning("Unable to analyze response file " + file.getName() + ": " + exc.getMessage());
            return null;
        }
    }

    private static void applySharedNormalizationAndStore(Options options, PcmResponseAnalyzer.ResponseCurve leftCurve, PcmResponseAnalyzer.ResponseCurve rightCurve) {
        double leftPeak = (leftCurve != null) ? leftCurve.getPeakAmplitudeDb() : Double.NEGATIVE_INFINITY;
        double rightPeak = (rightCurve != null) ? rightCurve.getPeakAmplitudeDb() : Double.NEGATIVE_INFINITY;
        double sharedPeak = Math.max(leftPeak, rightPeak);

        if (!Double.isFinite(sharedPeak)) {
            options.setLeftChannelResponsePoints(null);
            options.setRightChannelResponsePoints(null);
            return;
        }

        FrequencyAmplitudePoints leftPoints = null;
        if (leftCurve != null && !leftCurve.isEmpty()) {
            leftPoints = leftCurve.offsetDb(-sharedPeak).toFrequencyAmplitudePoints(GRAPH_POINT_COUNT);
        }

        FrequencyAmplitudePoints rightPoints = null;
        if (rightCurve != null && !rightCurve.isEmpty()) {
            rightPoints = rightCurve.offsetDb(-sharedPeak).toFrequencyAmplitudePoints(GRAPH_POINT_COUNT);
        }

        options.setLeftChannelResponsePoints(leftPoints);
        options.setRightChannelResponsePoints(rightPoints);
    }
}
