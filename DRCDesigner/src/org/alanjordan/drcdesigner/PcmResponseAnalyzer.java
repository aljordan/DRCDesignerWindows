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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * Parses raw PCM impulse/sweep result files and computes response curves suitable for graph overlays.
 */
public class PcmResponseAnalyzer {

    public enum PcmEncoding {
        FLOAT32_LE,
        FLOAT32_BE,
        INT16_LE,
        INT16_BE
    }

    public enum SmoothingMode {
        NONE,
        FRACTIONAL_OCTAVE,
        ERB
    }

    public static class AnalysisSettings {
        private int fftSize;
        private PcmEncoding pcmEncoding;
        private SmoothingMode smoothingMode;
        private double smoothingStrength;
        private boolean normalizeToPeak;
        private double minFrequencyHz;
        private double maxFrequencyHz;
        private boolean centerWindowAroundPeak;
        private boolean applyHannWindow;

        public AnalysisSettings() {
            // Good default for sweep impulse files produced by rec_imp + DRC workflow.
            this.fftSize = 65536;
            this.pcmEncoding = PcmEncoding.FLOAT32_LE;
            this.smoothingMode = SmoothingMode.ERB;
            this.smoothingStrength = 1.0;
            this.normalizeToPeak = true;
            this.minFrequencyHz = 20.0;
            this.maxFrequencyHz = 22050.0;
            this.centerWindowAroundPeak = true;
            this.applyHannWindow = true;
        }

        public int getFftSize() {
            return fftSize;
        }

        public void setFftSize(int fftSize) {
            this.fftSize = fftSize;
        }

        public PcmEncoding getPcmEncoding() {
            return pcmEncoding;
        }

        public void setPcmEncoding(PcmEncoding pcmEncoding) {
            this.pcmEncoding = pcmEncoding;
        }

        public SmoothingMode getSmoothingMode() {
            return smoothingMode;
        }

        public void setSmoothingMode(SmoothingMode smoothingMode) {
            this.smoothingMode = smoothingMode;
        }

        public double getSmoothingStrength() {
            return smoothingStrength;
        }

        public void setSmoothingStrength(double smoothingStrength) {
            this.smoothingStrength = smoothingStrength;
        }

        public boolean isNormalizeToPeak() {
            return normalizeToPeak;
        }

        public void setNormalizeToPeak(boolean normalizeToPeak) {
            this.normalizeToPeak = normalizeToPeak;
        }

        public double getMinFrequencyHz() {
            return minFrequencyHz;
        }

        public void setMinFrequencyHz(double minFrequencyHz) {
            this.minFrequencyHz = minFrequencyHz;
        }

        public double getMaxFrequencyHz() {
            return maxFrequencyHz;
        }

        public void setMaxFrequencyHz(double maxFrequencyHz) {
            this.maxFrequencyHz = maxFrequencyHz;
        }

        public boolean isCenterWindowAroundPeak() {
            return centerWindowAroundPeak;
        }

        public void setCenterWindowAroundPeak(boolean centerWindowAroundPeak) {
            this.centerWindowAroundPeak = centerWindowAroundPeak;
        }

        public boolean isApplyHannWindow() {
            return applyHannWindow;
        }

        public void setApplyHannWindow(boolean applyHannWindow) {
            this.applyHannWindow = applyHannWindow;
        }
    }

    public static class ResponseCurve {
        private final double[] frequenciesHz;
        private final double[] amplitudesDb;

        public ResponseCurve(double[] frequenciesHz, double[] amplitudesDb) {
            this.frequenciesHz = frequenciesHz;
            this.amplitudesDb = amplitudesDb;
        }

        public double[] getFrequenciesHz() {
            return frequenciesHz;
        }

        public double[] getAmplitudesDb() {
            return amplitudesDb;
        }

        public boolean isEmpty() {
            return frequenciesHz.length == 0 || amplitudesDb.length == 0;
        }

        public double getPeakAmplitudeDb() {
            if (isEmpty()) {
                return Double.NEGATIVE_INFINITY;
            }

            double peak = amplitudesDb[0];
            for (int i = 1; i < amplitudesDb.length; i++) {
                if (amplitudesDb[i] > peak) {
                    peak = amplitudesDb[i];
                }
            }

            return peak;
        }

        public ResponseCurve offsetDb(double deltaDb) {
            if (isEmpty()) {
                return this;
            }

            double[] shiftedAmplitudes = new double[amplitudesDb.length];
            for (int i = 0; i < amplitudesDb.length; i++) {
                shiftedAmplitudes[i] = amplitudesDb[i] + deltaDb;
            }

            return new ResponseCurve(frequenciesHz, shiftedAmplitudes);
        }

        public FrequencyAmplitudePoints toFrequencyAmplitudePoints(int maxPoints) {
            FrequencyAmplitudePoints points = new FrequencyAmplitudePoints();
            if (frequenciesHz.length == 0 || amplitudesDb.length == 0) {
                return points;
            }

            if (maxPoints <= 0 || maxPoints >= frequenciesHz.length) {
                for (int i = 0; i < frequenciesHz.length; i++) {
                    points.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(frequenciesHz[i], amplitudesDb[i]));
                }
                return points;
            }

            double minFreq = Math.max(1.0, frequenciesHz[0]);
            double maxFreq = Math.max(minFreq, frequenciesHz[frequenciesHz.length - 1]);
            double logMin = Math.log10(minFreq);
            double logMax = Math.log10(maxFreq);

            for (int i = 0; i < maxPoints; i++) {
                double t = (maxPoints == 1) ? 0.0 : ((double) i / (double) (maxPoints - 1));
                double targetFreq = Math.pow(10.0, logMin + ((logMax - logMin) * t));
                double amp = interpolateAmplitudeAtFrequency(targetFreq);
                points.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(targetFreq, amp));
            }

            return points;
        }

        private double interpolateAmplitudeAtFrequency(double frequencyHz) {
            if (frequencyHz <= frequenciesHz[0]) {
                return amplitudesDb[0];
            }
            if (frequencyHz >= frequenciesHz[frequenciesHz.length - 1]) {
                return amplitudesDb[amplitudesDb.length - 1];
            }

            int upper = Arrays.binarySearch(frequenciesHz, frequencyHz);
            if (upper >= 0) {
                return amplitudesDb[upper];
            }
            upper = -upper - 1;
            int lower = upper - 1;

            double f1 = frequenciesHz[lower];
            double f2 = frequenciesHz[upper];
            double a1 = amplitudesDb[lower];
            double a2 = amplitudesDb[upper];

            if (f2 == f1) {
                return a1;
            }

            double ratio = (frequencyHz - f1) / (f2 - f1);
            return a1 + ((a2 - a1) * ratio);
        }
    }

    public ResponseCurve analyzeFile(File pcmFile, int sampleRate, AnalysisSettings settings) throws IOException {
        if (pcmFile == null) {
            throw new IllegalArgumentException("PCM file cannot be null");
        }
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("Sample rate must be > 0");
        }
        if (settings == null) {
            throw new IllegalArgumentException("Analysis settings cannot be null");
        }

        byte[] rawBytes = Files.readAllBytes(pcmFile.toPath());
        double[] samples = decodePcmSamples(rawBytes, settings.getPcmEncoding());

        if (samples.length == 0) {
            return new ResponseCurve(new double[0], new double[0]);
        }

        return analyzeSamples(samples, sampleRate, settings);
    }

    public ResponseCurve analyzeSamples(double[] samples, int sampleRate, AnalysisSettings settings) {
        if (samples == null || samples.length == 0) {
            return new ResponseCurve(new double[0], new double[0]);
        }
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("Sample rate must be > 0");
        }
        if (settings == null) {
            throw new IllegalArgumentException("Analysis settings cannot be null");
        }

        int fftSize = normalizeFftSize(settings.getFftSize(), samples.length);
        double[] real = new double[fftSize];
        double[] imag = new double[fftSize];

        double[] analysisWindow;
        if (settings.isCenterWindowAroundPeak()) {
            // Center analysis around the strongest impulse region for measured sweep captures.
            analysisWindow = extractWindowAroundPeak(samples, fftSize);
        }
        else {
            analysisWindow = samples;
        }

        int copied = Math.min(analysisWindow.length, fftSize);
        System.arraycopy(analysisWindow, 0, real, 0, copied);

        if (settings.isApplyHannWindow()) {
            applyHannWindow(real, copied);
        }
        fftInPlace(real, imag);

        ResponseCurve rawCurve = magnitudeSpectrum(real, imag, sampleRate, settings.getMinFrequencyHz(), settings.getMaxFrequencyHz());
        if (rawCurve.getFrequenciesHz().length == 0) {
            return rawCurve;
        }

        double[] smoothed = smooth(rawCurve.getFrequenciesHz(), rawCurve.getAmplitudesDb(), settings.getSmoothingMode(), settings.getSmoothingStrength());
        if (settings.isNormalizeToPeak()) {
            smoothed = normalizeToPeak(smoothed);
        }

        return new ResponseCurve(rawCurve.getFrequenciesHz(), smoothed);
    }

    public ResponseCurve smoothResponseCurve(ResponseCurve curve, AnalysisSettings settings) {
        if (curve == null || curve.isEmpty()) {
            return new ResponseCurve(new double[0], new double[0]);
        }
        if (settings == null) {
            throw new IllegalArgumentException("Analysis settings cannot be null");
        }

        double[] smoothed = smooth(curve.getFrequenciesHz(), curve.getAmplitudesDb(), settings.getSmoothingMode(), settings.getSmoothingStrength());
        if (settings.isNormalizeToPeak()) {
            smoothed = normalizeToPeak(smoothed);
        }

        return new ResponseCurve(curve.getFrequenciesHz(), smoothed);
    }

    private double[] extractWindowAroundPeak(double[] samples, int windowSize) {
        if (samples.length <= windowSize) {
            return samples;
        }

        int peakIndex = 0;
        double peakAbs = Math.abs(samples[0]);
        for (int i = 1; i < samples.length; i++) {
            double currentAbs = Math.abs(samples[i]);
            if (currentAbs > peakAbs) {
                peakAbs = currentAbs;
                peakIndex = i;
            }
        }

        // Keep some pre-impulse context while preserving more post-impulse decay.
        int preferredPeakOffset = windowSize / 4;
        int start = peakIndex - preferredPeakOffset;
        if (start < 0) {
            start = 0;
        }

        int maxStart = samples.length - windowSize;
        if (start > maxStart) {
            start = maxStart;
        }

        double[] window = new double[windowSize];
        System.arraycopy(samples, start, window, 0, windowSize);
        return window;
    }

    private double[] decodePcmSamples(byte[] rawBytes, PcmEncoding encoding) {
        if (rawBytes == null || rawBytes.length == 0) {
            return new double[0];
        }

        ByteOrder order = (encoding == PcmEncoding.FLOAT32_BE || encoding == PcmEncoding.INT16_BE)
                ? ByteOrder.BIG_ENDIAN
                : ByteOrder.LITTLE_ENDIAN;

        ByteBuffer buffer = ByteBuffer.wrap(rawBytes).order(order);

        if (encoding == PcmEncoding.FLOAT32_LE || encoding == PcmEncoding.FLOAT32_BE) {
            int count = rawBytes.length / 4;
            double[] samples = new double[count];
            for (int i = 0; i < count; i++) {
                samples[i] = buffer.getFloat();
            }
            return samples;
        }

        int count = rawBytes.length / 2;
        double[] samples = new double[count];
        for (int i = 0; i < count; i++) {
            samples[i] = ((double) buffer.getShort()) / 32768.0;
        }
        return samples;
    }

    private int normalizeFftSize(int requestedSize, int sampleCount) {
        int minimum = 1024;
        int size = requestedSize;
        if (size < minimum) {
            size = minimum;
        }
        size = largestPowerOfTwoLessThanOrEqualTo(size);

        int maxAllowed = largestPowerOfTwoLessThanOrEqualTo(sampleCount);
        if (maxAllowed < minimum) {
            return Math.max(2, maxAllowed);
        }

        return Math.min(size, maxAllowed);
    }

    private int largestPowerOfTwoLessThanOrEqualTo(int value) {
        if (value < 2) {
            return 1;
        }
        int power = 1;
        while ((power << 1) > 0 && (power << 1) <= value) {
            power <<= 1;
        }
        return power;
    }

    private void applyHannWindow(double[] samples, int count) {
        if (count <= 1) {
            return;
        }

        for (int i = 0; i < count; i++) {
            double window = 0.5 - (0.5 * Math.cos((2.0 * Math.PI * i) / (count - 1.0)));
            samples[i] *= window;
        }
    }

    private ResponseCurve magnitudeSpectrum(double[] real, double[] imag, int sampleRate, double minFreq, double maxFreq) {
        int n = real.length;
        if (n < 2) {
            return new ResponseCurve(new double[0], new double[0]);
        }

        double nyquist = sampleRate / 2.0;
        double effectiveMin = Math.max(0.0, minFreq);
        double effectiveMax = Math.min(nyquist, Math.max(effectiveMin, maxFreq));

        int maxBin = n / 2;
        int firstBin = 1;
        while (firstBin <= maxBin) {
            double f = ((double) firstBin * sampleRate) / n;
            if (f >= effectiveMin) {
                break;
            }
            firstBin++;
        }

        int lastBin = maxBin;
        while (lastBin >= firstBin) {
            double f = ((double) lastBin * sampleRate) / n;
            if (f <= effectiveMax) {
                break;
            }
            lastBin--;
        }

        if (lastBin < firstBin) {
            return new ResponseCurve(new double[0], new double[0]);
        }

        int size = lastBin - firstBin + 1;
        double[] frequencies = new double[size];
        double[] amplitudes = new double[size];

        final double eps = 1e-20;
        int index = 0;
        for (int bin = firstBin; bin <= lastBin; bin++) {
            double frequency = ((double) bin * sampleRate) / n;
            double re = real[bin];
            double im = imag[bin];
            double magnitude = Math.sqrt((re * re) + (im * im));
            double db = 20.0 * Math.log10(Math.max(magnitude, eps));

            frequencies[index] = frequency;
            amplitudes[index] = db;
            index++;
        }

        return new ResponseCurve(frequencies, amplitudes);
    }

    private double[] smooth(double[] frequencies, double[] amplitudes, SmoothingMode mode, double strength) {
        if (mode == null || mode == SmoothingMode.NONE) {
            return amplitudes.clone();
        }

        if (frequencies.length != amplitudes.length || frequencies.length == 0) {
            return amplitudes.clone();
        }

        if (mode == SmoothingMode.FRACTIONAL_OCTAVE) {
            return smoothFractionalOctave(frequencies, amplitudes, strength);
        }

        return smoothErb(frequencies, amplitudes, strength);
    }

    private double[] smoothFractionalOctave(double[] frequencies, double[] amplitudes, double strength) {
        // strength=1.0 maps to one-sixth octave; higher values broaden smoothing.
        double octaveWidth = Math.max(1.0 / 24.0, (1.0 / 6.0) * Math.max(0.1, strength));
        double halfRatio = Math.pow(2.0, octaveWidth * 0.5);

        double[] prefix = buildPrefixSums(amplitudes);
        double[] out = new double[amplitudes.length];

        int left = 0;
        int right = 0;
        for (int i = 0; i < frequencies.length; i++) {
            double center = frequencies[i];
            double low = center / halfRatio;
            double high = center * halfRatio;

            while (left < frequencies.length && frequencies[left] < low) {
                left++;
            }
            while (right + 1 < frequencies.length && frequencies[right + 1] <= high) {
                right++;
            }

            if (right < left) {
                right = left;
            }

            out[i] = mean(prefix, left, right);
        }

        return out;
    }

    private double[] smoothErb(double[] frequencies, double[] amplitudes, double strength) {
        // strength=1.0 uses approximately +/-1 ERB around each center frequency.
        double erbMultiplier = Math.max(0.25, strength);

        double[] prefix = buildPrefixSums(amplitudes);
        double[] out = new double[amplitudes.length];
        double minFrequency = frequencies[0];
        double maxFrequency = frequencies[frequencies.length - 1];

        int left = 0;
        int right = 0;
        for (int i = 0; i < frequencies.length; i++) {
            double center = frequencies[i];
            double erb = 24.7 * ((4.37 * center / 1000.0) + 1.0);
            double halfBandwidth = erb * erbMultiplier;

            // Keep smoothing window symmetric at the boundaries by shrinking the
            // half-bandwidth near min/max frequency instead of using a one-sided window.
            double edgeLimitedHalfBandwidth = halfBandwidth;
            double distanceToLowEdge = center - minFrequency;
            double distanceToHighEdge = maxFrequency - center;
            if (distanceToLowEdge < edgeLimitedHalfBandwidth) {
                edgeLimitedHalfBandwidth = distanceToLowEdge;
            }
            if (distanceToHighEdge < edgeLimitedHalfBandwidth) {
                edgeLimitedHalfBandwidth = distanceToHighEdge;
            }

            double low = center - edgeLimitedHalfBandwidth;
            double high = center + edgeLimitedHalfBandwidth;

            while (left < frequencies.length && frequencies[left] < low) {
                left++;
            }
            while (right + 1 < frequencies.length && frequencies[right + 1] <= high) {
                right++;
            }

            if (right < left) {
                right = left;
            }

            out[i] = mean(prefix, left, right);
        }

        return out;
    }

    private double[] buildPrefixSums(double[] values) {
        double[] prefix = new double[values.length + 1];
        for (int i = 0; i < values.length; i++) {
            prefix[i + 1] = prefix[i] + values[i];
        }
        return prefix;
    }

    private double mean(double[] prefix, int leftInclusive, int rightInclusive) {
        int count = rightInclusive - leftInclusive + 1;
        if (count <= 0) {
            return 0.0;
        }
        double total = prefix[rightInclusive + 1] - prefix[leftInclusive];
        return total / count;
    }

    private double[] normalizeToPeak(double[] amplitudes) {
        if (amplitudes.length == 0) {
            return amplitudes;
        }

        double peak = amplitudes[0];
        for (int i = 1; i < amplitudes.length; i++) {
            if (amplitudes[i] > peak) {
                peak = amplitudes[i];
            }
        }

        double[] normalized = new double[amplitudes.length];
        for (int i = 0; i < amplitudes.length; i++) {
            normalized[i] = amplitudes[i] - peak;
        }
        return normalized;
    }

    /**
     * In-place radix-2 Cooley-Tukey FFT.
     */
    private void fftInPlace(double[] real, double[] imag) {
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
            double angle = -2.0 * Math.PI / len;
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
    }
}
