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
import java.io.FileFilter;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public final class PredictedResponsePreviewUtil {
    private static final Logger LOGGER = AppLogger.getLogger();
    private static final Pattern LEFT_FILTER_FILE_PATTERN = Pattern.compile("^LeftSpeaker(\\d+)(.*)\\.pcm$");
    private static final Pattern RIGHT_FILTER_FILE_PATTERN = Pattern.compile("^RightSpeaker(\\d+)(.*)\\.pcm$");
    private static final Pattern STEREO_FILTER_FILE_PATTERN = Pattern.compile("(?i)^stereo(\\d+)(.*)\\.wav$");
    private static final Pattern PL_MAX_GAIN_PATTERN = Pattern.compile("^\\s*PLMaxGain\\s*=\\s*([+-]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?)");

    public static class Result {
        private final FrequencyAmplitudePoints measuredLeft;
        private final FrequencyAmplitudePoints measuredRight;
        private final FrequencyAmplitudePoints predictedLeft;
        private final FrequencyAmplitudePoints predictedRight;
        private final double[] measuredLeftImpulse;
        private final double[] measuredRightImpulse;
        private final double[] predictedLeftImpulse;
        private final double[] predictedRightImpulse;
        private final FrequencyAmplitudePoints dacClipRiskLeft;
        private final FrequencyAmplitudePoints dacClipRiskRight;
        private final int sampleRateHz;
        private final String sampleRate;
        private final String filterLabel;
        private final double requiredPreattenuationDb;
        private final double recommendedPreattenuationDb;

        public Result(FrequencyAmplitudePoints measuredLeft,
                      FrequencyAmplitudePoints measuredRight,
                      FrequencyAmplitudePoints predictedLeft,
                      FrequencyAmplitudePoints predictedRight,
                      double[] measuredLeftImpulse,
                      double[] measuredRightImpulse,
                      double[] predictedLeftImpulse,
                      double[] predictedRightImpulse,
                      FrequencyAmplitudePoints dacClipRiskLeft,
                      FrequencyAmplitudePoints dacClipRiskRight,
                      int sampleRateHz,
                      String sampleRate,
                      String filterLabel,
                      double requiredPreattenuationDb,
                      double recommendedPreattenuationDb) {
            this.measuredLeft = measuredLeft;
            this.measuredRight = measuredRight;
            this.predictedLeft = predictedLeft;
            this.predictedRight = predictedRight;
            this.measuredLeftImpulse = measuredLeftImpulse;
            this.measuredRightImpulse = measuredRightImpulse;
            this.predictedLeftImpulse = predictedLeftImpulse;
            this.predictedRightImpulse = predictedRightImpulse;
            this.dacClipRiskLeft = dacClipRiskLeft;
            this.dacClipRiskRight = dacClipRiskRight;
            this.sampleRateHz = sampleRateHz;
            this.sampleRate = sampleRate;
            this.filterLabel = filterLabel;
            this.requiredPreattenuationDb = requiredPreattenuationDb;
            this.recommendedPreattenuationDb = recommendedPreattenuationDb;
        }

        public FrequencyAmplitudePoints getMeasuredLeft() {
            return measuredLeft;
        }

        public FrequencyAmplitudePoints getMeasuredRight() {
            return measuredRight;
        }

        public FrequencyAmplitudePoints getPredictedLeft() {
            return predictedLeft;
        }

        public FrequencyAmplitudePoints getPredictedRight() {
            return predictedRight;
        }

        public double[] getMeasuredLeftImpulse() {
            return measuredLeftImpulse;
        }

        public double[] getMeasuredRightImpulse() {
            return measuredRightImpulse;
        }

        public double[] getPredictedLeftImpulse() {
            return predictedLeftImpulse;
        }

        public double[] getPredictedRightImpulse() {
            return predictedRightImpulse;
        }

        public FrequencyAmplitudePoints getDacClipRiskLeft() {
            return dacClipRiskLeft;
        }

        public FrequencyAmplitudePoints getDacClipRiskRight() {
            return dacClipRiskRight;
        }

        public int getSampleRateHz() {
            return sampleRateHz;
        }

        public String getSampleRate() {
            return sampleRate;
        }

        public String getFilterLabel() {
            return filterLabel;
        }

        public double getRequiredPreattenuationDb() {
            return requiredPreattenuationDb;
        }

        public double getRecommendedPreattenuationDb() {
            return recommendedPreattenuationDb;
        }
    }

    private static class FilterPair {
        private final File leftFile;
        private final File rightFile;
        private final File stereoWavFile;
        private final String sampleRate;
        private final String suffix;
        private final String label;

        private FilterPair(File leftFile, File rightFile, File stereoWavFile, String sampleRate, String suffix, String label) {
            this.leftFile = leftFile;
            this.rightFile = rightFile;
            this.stereoWavFile = stereoWavFile;
            this.sampleRate = sampleRate;
            this.suffix = suffix;
            this.label = label;
        }
    }

    private static class StereoCurves {
        private final double[] left;
        private final double[] right;

        private StereoCurves(double[] left, double[] right) {
            this.left = left;
            this.right = right;
        }
    }

    private PredictedResponsePreviewUtil() {
    }

    public static Result compute(Options options) {
        return compute(options, null, null);
    }

    public static Result compute(Options options, String selectedFilterLabel) {
        return compute(options, selectedFilterLabel, null);
    }

    public static Result compute(Options options, String selectedFilterLabel, String selectedSampleRate) {
        if (options == null || options.getRoomCorrectionRootPath() == null) {
            return null;
        }

        FilterPair filterPair = findBestMatchingFilterPair(options, selectedFilterLabel, selectedSampleRate);
        if (filterPair == null) {
            return null;
        }

        String sampleRate = filterPair.sampleRate;

        int rate;
        try {
            rate = Integer.parseInt(sampleRate);
        }
        catch (NumberFormatException nfe) {
            LOGGER.warning("Invalid sample rate: " + sampleRate);
            return null;
        }

        File measuredLeft = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\LeftSpeakerImpulseResponse" + sampleRate + ".pcm");
        File measuredRight = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\RightSpeakerImpulseResponse" + sampleRate + ".pcm");
        if (!measuredLeft.exists() || !measuredRight.exists()) {
            return null;
        }

        PcmResponseAnalyzer analyzer = new PcmResponseAnalyzer();
        PcmResponseAnalyzer.AnalysisSettings measuredSettings = new PcmResponseAnalyzer.AnalysisSettings();
        measuredSettings.setPcmEncoding(PcmResponseAnalyzer.PcmEncoding.FLOAT32_LE);
        measuredSettings.setFftSize(65536);
        measuredSettings.setSmoothingMode(PcmResponseAnalyzer.SmoothingMode.NONE);
        measuredSettings.setSmoothingStrength(1.0);
        measuredSettings.setNormalizeToPeak(false);
        measuredSettings.setMinFrequencyHz(20.0);
        measuredSettings.setMaxFrequencyHz(ResponseDisplayFrequencyCap.getEffectiveCapForSampleRate(rate));
        measuredSettings.setCenterWindowAroundPeak(true);
        measuredSettings.setApplyHannWindow(true);

        PcmResponseAnalyzer.ResponseCurve measuredLeftRaw = analyzeFile(analyzer, measuredLeft, rate, measuredSettings);
        PcmResponseAnalyzer.ResponseCurve measuredRightRaw = analyzeFile(analyzer, measuredRight, rate, measuredSettings);
        double[] measuredLeftSamples = readFloat32LePcmSamples(measuredLeft);
        double[] measuredRightSamples = readFloat32LePcmSamples(measuredRight);
        if (measuredLeftSamples == null || measuredRightSamples == null) {
            return null;
        }

        double[] filterLeftSamples;
        double[] filterRightSamples;
        if (filterPair.stereoWavFile != null) {
            StereoCurves stereoCurves = readStereoWavFilterSamples(filterPair.stereoWavFile);
            if (stereoCurves == null) {
                return null;
            }
            filterLeftSamples = stereoCurves.left;
            filterRightSamples = stereoCurves.right;
        }
        else {
            filterLeftSamples = readFloat32LePcmSamples(filterPair.leftFile);
            filterRightSamples = readFloat32LePcmSamples(filterPair.rightFile);
        }

        if (measuredLeftRaw == null || measuredRightRaw == null || filterLeftSamples == null || filterRightSamples == null) {
            return null;
        }

        double[] predictedLeftSamples = convolveSamples(measuredLeftSamples, filterLeftSamples);
        double[] predictedRightSamples = convolveSamples(measuredRightSamples, filterRightSamples);
        if (predictedLeftSamples == null || predictedRightSamples == null) {
            return null;
        }

        PcmResponseAnalyzer.ResponseCurve predictedLeftRaw = analyzer.analyzeSamples(predictedLeftSamples, rate, measuredSettings);
        PcmResponseAnalyzer.ResponseCurve predictedRightRaw = analyzer.analyzeSamples(predictedRightSamples, rate, measuredSettings);

        PcmResponseAnalyzer.AnalysisSettings erbSettings = new PcmResponseAnalyzer.AnalysisSettings();
        erbSettings.setSmoothingMode(PcmResponseAnalyzer.SmoothingMode.ERB);
        erbSettings.setSmoothingStrength(1.0);
        erbSettings.setNormalizeToPeak(false);

        PcmResponseAnalyzer.AnalysisSettings filterSpectrumSettings = new PcmResponseAnalyzer.AnalysisSettings();
        filterSpectrumSettings.setPcmEncoding(PcmResponseAnalyzer.PcmEncoding.FLOAT32_LE);
        filterSpectrumSettings.setFftSize(65536);
        filterSpectrumSettings.setSmoothingMode(PcmResponseAnalyzer.SmoothingMode.NONE);
        filterSpectrumSettings.setSmoothingStrength(1.0);
        filterSpectrumSettings.setNormalizeToPeak(false);
        filterSpectrumSettings.setMinFrequencyHz(20.0);
        filterSpectrumSettings.setMaxFrequencyHz(ResponseDisplayFrequencyCap.getEffectiveCapForSampleRate(rate));
        filterSpectrumSettings.setCenterWindowAroundPeak(false);
        filterSpectrumSettings.setApplyHannWindow(false);

        PcmResponseAnalyzer.ResponseCurve measuredLeftErb = analyzer.smoothResponseCurve(measuredLeftRaw, erbSettings);
        PcmResponseAnalyzer.ResponseCurve measuredRightErb = analyzer.smoothResponseCurve(measuredRightRaw, erbSettings);
        PcmResponseAnalyzer.ResponseCurve predictedLeftErb = analyzer.smoothResponseCurve(predictedLeftRaw, erbSettings);
        PcmResponseAnalyzer.ResponseCurve predictedRightErb = analyzer.smoothResponseCurve(predictedRightRaw, erbSettings);

        PcmResponseAnalyzer.ResponseCurve filterLeftRaw = analyzer.analyzeSamples(filterLeftSamples, rate, filterSpectrumSettings);
        PcmResponseAnalyzer.ResponseCurve filterRightRaw = analyzer.analyzeSamples(filterRightSamples, rate, filterSpectrumSettings);
        PcmResponseAnalyzer.ResponseCurve filterLeftErb = analyzer.smoothResponseCurve(filterLeftRaw, erbSettings);
        PcmResponseAnalyzer.ResponseCurve filterRightErb = analyzer.smoothResponseCurve(filterRightRaw, erbSettings);

        PcmResponseAnalyzer.ResponseCurve dacAbsoluteLeft = filterLeftErb;
        PcmResponseAnalyzer.ResponseCurve dacAbsoluteRight = filterRightErb;

        FrequencyAmplitudePoints dacClipRiskLeftPoints = buildExcessGainPoints(dacAbsoluteLeft, 450);
        FrequencyAmplitudePoints dacClipRiskRightPoints = buildExcessGainPoints(dacAbsoluteRight, 450);

        double requiredPreattenuationDb = Math.max(maxPositiveAmplitude(dacAbsoluteLeft), maxPositiveAmplitude(dacAbsoluteRight));
        double p95Left = positivePercentileAmplitude(dacAbsoluteLeft, 0.95);
        double p95Right = positivePercentileAmplitude(dacAbsoluteRight, 0.95);
        double percentileBased = Math.max(p95Left, p95Right) + 1.0;
        double recommendedPreattenuationDb = Math.max(0.0, Math.min(requiredPreattenuationDb, percentileBased));

        double sharedPeak = maxPeak(measuredLeftErb, measuredRightErb, predictedLeftErb, predictedRightErb);
        if (!Double.isFinite(sharedPeak)) {
            return null;
        }

        FrequencyAmplitudePoints measuredLeftPoints = measuredLeftErb.offsetDb(-sharedPeak).toFrequencyAmplitudePoints(450);
        FrequencyAmplitudePoints measuredRightPoints = measuredRightErb.offsetDb(-sharedPeak).toFrequencyAmplitudePoints(450);
        FrequencyAmplitudePoints predictedLeftPoints = predictedLeftErb.offsetDb(-sharedPeak).toFrequencyAmplitudePoints(450);
        FrequencyAmplitudePoints predictedRightPoints = predictedRightErb.offsetDb(-sharedPeak).toFrequencyAmplitudePoints(450);

        return new Result(measuredLeftPoints,
                measuredRightPoints,
                predictedLeftPoints,
                predictedRightPoints,
            measuredLeftSamples,
            measuredRightSamples,
            predictedLeftSamples,
            predictedRightSamples,
                dacClipRiskLeftPoints,
                dacClipRiskRightPoints,
            rate,
                sampleRate,
                filterPair.label,
                requiredPreattenuationDb,
                recommendedPreattenuationDb);
    }

    private static double maxPositiveAmplitude(PcmResponseAnalyzer.ResponseCurve curve) {
        if (curve == null || curve.isEmpty()) {
            return 0.0;
        }

        double max = 0.0;
        double[] amplitudes = curve.getAmplitudesDb();
        for (int i = 0; i < amplitudes.length; i++) {
            max = Math.max(max, amplitudes[i]);
        }
        return Math.max(0.0, max);
    }

    private static double positivePercentileAmplitude(PcmResponseAnalyzer.ResponseCurve curve, double percentile) {
        if (curve == null || curve.isEmpty()) {
            return 0.0;
        }

        double[] amplitudes = curve.getAmplitudesDb();
        List<Double> positives = new ArrayList<Double>();
        for (int i = 0; i < amplitudes.length; i++) {
            if (amplitudes[i] > 0.0) {
                positives.add(amplitudes[i]);
            }
        }

        if (positives.isEmpty()) {
            return 0.0;
        }

        Collections.sort(positives);
        double clampedPercentile = Math.max(0.0, Math.min(1.0, percentile));
        int index = (int) Math.round(clampedPercentile * (positives.size() - 1));
        return positives.get(index).doubleValue();
    }

    private static FrequencyAmplitudePoints buildExcessGainPoints(PcmResponseAnalyzer.ResponseCurve curve, int maxPoints) {
        FrequencyAmplitudePoints points = new FrequencyAmplitudePoints();
        if (curve == null || curve.isEmpty()) {
            return points;
        }

        FrequencyAmplitudePoints source = curve.toFrequencyAmplitudePoints(maxPoints);
        for (int i = 1; i <= source.getNumberOfFrequencyDataPoints(); i++) {
            FrequencyAmplitudePoint point = source.getFrequencyAmplitudePoint(i);
            double riskDb = Math.max(0.0, point.getAmplitude());
            points.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(point.getFrequency(), riskDb));
        }
        return points;
    }

    public static String[] getAvailableFilterLabels(Options options) {
        List<FilterPair> pairs = findAllFilterPairs(options);
        if (pairs.isEmpty()) {
            return new String[0];
        }

        List<String> labels = new ArrayList<String>();
        for (FilterPair pair : pairs) {
            if (!containsIgnoreCase(labels, pair.label)) {
                labels.add(pair.label);
            }
        }

        Collections.sort(labels, new Comparator<String>() {
            public int compare(String a, String b) {
                if ("Base".equalsIgnoreCase(a) && !"Base".equalsIgnoreCase(b)) {
                    return -1;
                }
                if (!"Base".equalsIgnoreCase(a) && "Base".equalsIgnoreCase(b)) {
                    return 1;
                }
                return a.compareToIgnoreCase(b);
            }
        });

        return labels.toArray(new String[labels.size()]);
    }

    public static String[] getAvailableSampleRates(Options options, String selectedFilterLabel) {
        List<FilterPair> pairs = findAllFilterPairs(options);
        if (pairs.isEmpty()) {
            return new String[0];
        }

        List<String> rates = new ArrayList<String>();
        for (FilterPair pair : pairs) {
            if (selectedFilterLabel != null
                    && selectedFilterLabel.length() > 0
                    && !selectedFilterLabel.equalsIgnoreCase(pair.label)) {
                continue;
            }

            File measuredLeft = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\LeftSpeakerImpulseResponse" + pair.sampleRate + ".pcm");
            File measuredRight = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\RightSpeakerImpulseResponse" + pair.sampleRate + ".pcm");
            if (!measuredLeft.exists() || !measuredRight.exists()) {
                continue;
            }

            if (!containsIgnoreCase(rates, pair.sampleRate)) {
                rates.add(pair.sampleRate);
            }
        }

        Collections.sort(rates, new Comparator<String>() {
            public int compare(String a, String b) {
                try {
                    int ai = Integer.parseInt(a);
                    int bi = Integer.parseInt(b);
                    return ai - bi;
                }
                catch (NumberFormatException nfe) {
                    return a.compareToIgnoreCase(b);
                }
            }
        });

        return rates.toArray(new String[rates.size()]);
    }

    private static PcmResponseAnalyzer.ResponseCurve analyzeFile(PcmResponseAnalyzer analyzer, File file, int sampleRate, PcmResponseAnalyzer.AnalysisSettings settings) {
        try {
            return analyzer.analyzeFile(file, sampleRate, settings);
        }
        catch (Exception exc) {
            LOGGER.warning("Unable to analyze file " + file.getName() + ": " + exc.getMessage());
            return null;
        }
    }

    private static StereoCurves readStereoWavFilterSamples(File stereoWavFile) {
        try {
            double[][] channels = readStereoWavChannels(stereoWavFile);
            if (channels == null || channels.length < 2) {
                return null;
            }

            return new StereoCurves(channels[0], channels[1]);
        }
        catch (Exception exc) {
            LOGGER.warning("Unable to read stereo wav " + stereoWavFile.getName() + ": " + exc.getMessage());
            return null;
        }
    }

    private static double[] readFloat32LePcmSamples(File pcmFile) {
        if (pcmFile == null || !pcmFile.exists()) {
            return null;
        }

        try {
            byte[] rawBytes = Files.readAllBytes(pcmFile.toPath());
            if (rawBytes.length < 4) {
                return new double[0];
            }

            ByteBuffer buffer = ByteBuffer.wrap(rawBytes).order(ByteOrder.LITTLE_ENDIAN);
            int count = rawBytes.length / 4;
            double[] samples = new double[count];
            for (int i = 0; i < count; i++) {
                samples[i] = buffer.getFloat();
            }
            return samples;
        }
        catch (Exception exc) {
            LOGGER.warning("Unable to read PCM file " + pcmFile.getName() + ": " + exc.getMessage());
            return null;
        }
    }

    private static double[] convolveSamples(double[] inputA, double[] inputB) {
        if (inputA == null || inputB == null || inputA.length == 0 || inputB.length == 0) {
            return null;
        }

        int outputLength = inputA.length + inputB.length - 1;
        int fftLength = nextPowerOfTwo(outputLength);

        double[] aReal = new double[fftLength];
        double[] aImag = new double[fftLength];
        double[] bReal = new double[fftLength];
        double[] bImag = new double[fftLength];

        System.arraycopy(inputA, 0, aReal, 0, inputA.length);
        System.arraycopy(inputB, 0, bReal, 0, inputB.length);

        fftInPlace(aReal, aImag, false);
        fftInPlace(bReal, bImag, false);

        for (int i = 0; i < fftLength; i++) {
            double real = (aReal[i] * bReal[i]) - (aImag[i] * bImag[i]);
            double imag = (aReal[i] * bImag[i]) + (aImag[i] * bReal[i]);
            aReal[i] = real;
            aImag[i] = imag;
        }

        fftInPlace(aReal, aImag, true);

        double[] output = new double[outputLength];
        System.arraycopy(aReal, 0, output, 0, outputLength);
        return output;
    }

    private static int nextPowerOfTwo(int value) {
        int power = 1;
        while (power < value) {
            power <<= 1;
        }
        return power;
    }

    private static void fftInPlace(double[] real, double[] imag, boolean inverse) {
        int n = real.length;
        if (n != imag.length) {
            throw new IllegalArgumentException("real and imag arrays must have same length");
        }
        if ((n & (n - 1)) != 0) {
            throw new IllegalArgumentException("FFT length must be a power of two");
        }

        int j = 0;
        for (int i = 1; i < n; i++) {
            int bit = n >> 1;
            while ((j & bit) != 0) {
                j ^= bit;
                bit >>= 1;
            }
            j ^= bit;

            if (i < j) {
                double tempReal = real[i];
                real[i] = real[j];
                real[j] = tempReal;

                double tempImag = imag[i];
                imag[i] = imag[j];
                imag[j] = tempImag;
            }
        }

        for (int len = 2; len <= n; len <<= 1) {
            double angle = (inverse ? 2.0 : -2.0) * Math.PI / len;
            double wLenReal = Math.cos(angle);
            double wLenImag = Math.sin(angle);

            for (int i = 0; i < n; i += len) {
                double wReal = 1.0;
                double wImag = 0.0;

                for (int k = 0; k < len / 2; k++) {
                    int evenIndex = i + k;
                    int oddIndex = i + k + (len / 2);

                    double oddReal = (real[oddIndex] * wReal) - (imag[oddIndex] * wImag);
                    double oddImag = (real[oddIndex] * wImag) + (imag[oddIndex] * wReal);

                    double evenReal = real[evenIndex];
                    double evenImag = imag[evenIndex];

                    real[evenIndex] = evenReal + oddReal;
                    imag[evenIndex] = evenImag + oddImag;
                    real[oddIndex] = evenReal - oddReal;
                    imag[oddIndex] = evenImag - oddImag;

                    double nextWReal = (wReal * wLenReal) - (wImag * wLenImag);
                    wImag = (wReal * wLenImag) + (wImag * wLenReal);
                    wReal = nextWReal;
                }
            }
        }

        if (inverse) {
            double invN = 1.0 / n;
            for (int i = 0; i < n; i++) {
                real[i] *= invN;
                imag[i] *= invN;
            }
        }
    }

    private static double[][] readStereoWavChannels(File wavFile) throws Exception {
        AudioInputStream stream = AudioSystem.getAudioInputStream(wavFile);
        try {
            AudioFormat format = stream.getFormat();
            int channels = format.getChannels();
            if (channels < 2) {
                throw new IOException("Filter wav is not stereo: " + wavFile.getName());
            }

            int sampleSizeBits = format.getSampleSizeInBits();
            int bytesPerSample = Math.max(1, sampleSizeBits / 8);
            int frameSize = format.getFrameSize();
            if (frameSize <= 0 || bytesPerSample <= 0) {
                throw new IOException("Unsupported wav frame format for " + wavFile.getName());
            }

            byte[] audioBytes = readAllBytes(stream);
            int frameCount = audioBytes.length / frameSize;
            double[] left = new double[frameCount];
            double[] right = new double[frameCount];

            boolean bigEndian = format.isBigEndian();
            AudioFormat.Encoding encoding = format.getEncoding();
            for (int frame = 0; frame < frameCount; frame++) {
                int frameOffset = frame * frameSize;
                left[frame] = decodeSample(audioBytes, frameOffset, bytesPerSample, sampleSizeBits, encoding, bigEndian);
                right[frame] = decodeSample(audioBytes, frameOffset + bytesPerSample, bytesPerSample, sampleSizeBits, encoding, bigEndian);
            }

            return new double[][] {left, right};
        }
        finally {
            stream.close();
        }
    }

    private static byte[] readAllBytes(AudioInputStream stream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static double decodeSample(byte[] data,
                                       int offset,
                                       int bytesPerSample,
                                       int sampleSizeBits,
                                       AudioFormat.Encoding encoding,
                                       boolean bigEndian) {
        if (AudioFormat.Encoding.PCM_FLOAT.equals(encoding) && sampleSizeBits == 32) {
            int bits = readInt32(data, offset, bigEndian);
            return Float.intBitsToFloat(bits);
        }

        long rawUnsigned = 0;
        if (bigEndian) {
            for (int i = 0; i < bytesPerSample; i++) {
                rawUnsigned = (rawUnsigned << 8) | (data[offset + i] & 0xffL);
            }
        }
        else {
            for (int i = bytesPerSample - 1; i >= 0; i--) {
                rawUnsigned = (rawUnsigned << 8) | (data[offset + i] & 0xffL);
            }
        }

        int shift = (bytesPerSample * 8) - sampleSizeBits;
        if (shift > 0) {
            rawUnsigned = rawUnsigned >>> shift;
        }

        if (AudioFormat.Encoding.PCM_UNSIGNED.equals(encoding)) {
            long midpoint = 1L << (sampleSizeBits - 1);
            long signed = rawUnsigned - midpoint;
            double divisor = (double) midpoint;
            return signed / divisor;
        }

        long signBit = 1L << (sampleSizeBits - 1);
        long signed = rawUnsigned;
        if ((signed & signBit) != 0) {
            signed -= (1L << sampleSizeBits);
        }

        double divisor = (double) (1L << (sampleSizeBits - 1));
        return signed / divisor;
    }

    private static int readInt32(byte[] data, int offset, boolean bigEndian) {
        if (bigEndian) {
            return ((data[offset] & 0xff) << 24)
                    | ((data[offset + 1] & 0xff) << 16)
                    | ((data[offset + 2] & 0xff) << 8)
                    | (data[offset + 3] & 0xff);
        }

        return ((data[offset + 3] & 0xff) << 24)
                | ((data[offset + 2] & 0xff) << 16)
                | ((data[offset + 1] & 0xff) << 8)
                | (data[offset] & 0xff);
    }

    private static double maxPeak(PcmResponseAnalyzer.ResponseCurve a, PcmResponseAnalyzer.ResponseCurve b, PcmResponseAnalyzer.ResponseCurve c, PcmResponseAnalyzer.ResponseCurve d) {
        double max = Double.NEGATIVE_INFINITY;
        if (a != null) {
            max = Math.max(max, a.getPeakAmplitudeDb());
        }
        if (b != null) {
            max = Math.max(max, b.getPeakAmplitudeDb());
        }
        if (c != null) {
            max = Math.max(max, c.getPeakAmplitudeDb());
        }
        if (d != null) {
            max = Math.max(max, d.getPeakAmplitudeDb());
        }
        return max;
    }

    private static FilterPair findBestMatchingFilterPair(Options options, String selectedFilterLabel, String selectedSampleRate) {
        List<FilterPair> pairs = findAllFilterPairs(options);
        if (pairs.isEmpty()) {
            return null;
        }

        FilterPair best = null;
        long bestTimestamp = Long.MIN_VALUE;

        for (FilterPair pair : pairs) {
            if (selectedFilterLabel != null
                    && selectedFilterLabel.length() > 0
                    && !selectedFilterLabel.equalsIgnoreCase(pair.label)) {
                continue;
            }

            if (selectedSampleRate != null
                    && selectedSampleRate.length() > 0
                    && !selectedSampleRate.equalsIgnoreCase(pair.sampleRate)) {
                continue;
            }

            File measuredLeft = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\LeftSpeakerImpulseResponse" + pair.sampleRate + ".pcm");
            File measuredRight = new File(options.getRoomCorrectionRootPath() + "\\drc-3.2.3\\sample\\RightSpeakerImpulseResponse" + pair.sampleRate + ".pcm");
            if (!measuredLeft.exists() || !measuredRight.exists()) {
                continue;
            }

            long filterTimestamp;
            if (pair.stereoWavFile != null) {
                filterTimestamp = pair.stereoWavFile.lastModified();
            }
            else {
                filterTimestamp = Math.min(pair.leftFile.lastModified(), pair.rightFile.lastModified());
            }

            long pairTimestamp = Math.min(
                    Math.min(measuredLeft.lastModified(), measuredRight.lastModified()),
                    filterTimestamp);

            if (pairTimestamp > bestTimestamp) {
                bestTimestamp = pairTimestamp;
                best = pair;
            }
        }

        return best;
    }

    private static List<FilterPair> findAllFilterPairs(Options options) {
        File convolverDir = new File(options.getRoomCorrectionRootPath() + "\\ConvolverFilters");
        if (!convolverDir.exists() || !convolverDir.isDirectory()) {
            return Collections.emptyList();
        }

        File[] leftCandidates = convolverDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return pathname.isFile() && LEFT_FILTER_FILE_PATTERN.matcher(name).matches();
            }
        });

        Map<String, File> rightByKey = new HashMap<String, File>();
        File[] rightCandidates = convolverDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return pathname.isFile() && RIGHT_FILTER_FILE_PATTERN.matcher(name).matches();
            }
        });

        if (rightCandidates != null) {
            for (File right : rightCandidates) {
                Matcher matcher = RIGHT_FILTER_FILE_PATTERN.matcher(right.getName());
                if (!matcher.matches()) {
                    continue;
                }
                String sampleRate = matcher.group(1);
                String suffix = matcher.group(2);
                rightByKey.put(sampleRate + "|" + suffix, right);
            }
        }

        List<FilterPair> pairs = new ArrayList<FilterPair>();
        if (leftCandidates != null) {
            for (File left : leftCandidates) {
                Matcher matcher = LEFT_FILTER_FILE_PATTERN.matcher(left.getName());
                if (!matcher.matches()) {
                    continue;
                }

                String sampleRate = matcher.group(1);
                String suffix = matcher.group(2);
                File right = rightByKey.get(sampleRate + "|" + suffix);
                if (right == null || !right.exists()) {
                    continue;
                }

                String label = suffix.length() == 0 ? "Base" : suffix;
                pairs.add(new FilterPair(left, right, null, sampleRate, suffix, label));
            }
        }

        File[] stereoCandidates = convolverDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return pathname.isFile() && STEREO_FILTER_FILE_PATTERN.matcher(name).matches();
            }
        });

        if (stereoCandidates != null) {
            for (File stereo : stereoCandidates) {
                Matcher matcher = STEREO_FILTER_FILE_PATTERN.matcher(stereo.getName());
                if (!matcher.matches()) {
                    continue;
                }

                String sampleRate = matcher.group(1);
                String suffix = matcher.group(2);
                String label = suffix.length() == 0 ? "Base" : suffix;
                pairs.add(new FilterPair(null, null, stereo, sampleRate, suffix, label));
            }
        }

        return pairs;
    }

    private static boolean containsIgnoreCase(List<String> values, String candidate) {
        for (String value : values) {
            if (value.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }
}
