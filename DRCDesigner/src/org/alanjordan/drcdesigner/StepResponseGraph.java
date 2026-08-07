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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

public class StepResponseGraph extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final double PRE_PEAK_RATIO = 0.25;
    private static final double POST_PEAK_RATIO = 0.75;

    private static final Color BEFORE_LEFT_COLOR = new Color(20, 140, 70);
    private static final Color BEFORE_RIGHT_COLOR = new Color(40, 90, 210);
    private static final Color AFTER_LEFT_COLOR = new Color(210, 80, 35);
    private static final Color AFTER_RIGHT_COLOR = new Color(160, 70, 190);

    private static final int DEFAULT_DISPLAY_WINDOW_MS = 120;

    private FontMetrics metrics;

    private int topPadding = 10;
    private int rightPadding = 50;
    private int leftPadding = 58;
    private int bottomPadding = 26;

    private int graphXStart = leftPadding;
    private int graphXEnd;
    private int graphYStart;
    private int graphYEnd;

    private double[] measuredLeft;
    private double[] measuredRight;
    private double[] predictedLeft;
    private double[] predictedRight;
    private int sampleRateHz;
    private int displayWindowMs = DEFAULT_DISPLAY_WINDOW_MS;
    private boolean showBeforeLeft = true;
    private boolean showBeforeRight = true;
    private boolean showAfterLeft = true;
    private boolean showAfterRight = true;
    private double hoverTimeMs = Double.NaN;
    private int legendX;
    private int legendY;
    private int legendWidth;
    private int legendHeight;

    public StepResponseGraph() {
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHoverTimeFromMouse(e.getX(), e.getY());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoverTimeMs = Double.NaN;
                repaint();
            }
        });
    }

    public void setCurves(double[] measuredLeft,
                          double[] measuredRight,
                          double[] predictedLeft,
                          double[] predictedRight,
                          int sampleRateHz) {
        this.measuredLeft = measuredLeft;
        this.measuredRight = measuredRight;
        this.predictedLeft = predictedLeft;
        this.predictedRight = predictedRight;
        this.sampleRateHz = sampleRateHz;
        repaint();
    }

    public void setDisplayWindowMs(int displayWindowMs) {
        this.displayWindowMs = Math.max(1, displayWindowMs);
        repaint();
    }

    public int getDisplayWindowMs() {
        return displayWindowMs;
    }

    public void setCurveVisibility(boolean showBeforeLeft,
                                   boolean showBeforeRight,
                                   boolean showAfterLeft,
                                   boolean showAfterRight) {
        this.showBeforeLeft = showBeforeLeft;
        this.showBeforeRight = showBeforeRight;
        this.showAfterLeft = showAfterLeft;
        this.showAfterRight = showAfterRight;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        graphXEnd = this.getWidth() - rightPadding;
        graphYStart = topPadding + 18;
        graphYEnd = this.getHeight() - bottomPadding;

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
        int fontSize = (int) Math.round(7.0 * screenRes / 72.0);
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        g2.setFont(font);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        metrics = g2.getFontMetrics();

        drawFrameBorder(g2);
        drawGrid(g2);
        drawCurves(g2);
        drawLegend(g2);
        drawMouseTimeReadout(g2);
        drawDisclaimer(g2);
    }

    private void updateHoverTimeFromMouse(int mouseX, int mouseY) {
        if (mouseX < graphXStart || mouseX > graphXEnd || mouseY < graphYStart || mouseY > graphYEnd) {
            if (Double.isFinite(hoverTimeMs)) {
                hoverTimeMs = Double.NaN;
                repaint();
            }
            return;
        }

        double newTimeMs = translateXToCenteredMs(mouseX, getDisplayWindowMs());
        if (!Double.isFinite(hoverTimeMs) || Math.abs(newTimeMs - hoverTimeMs) > 0.001) {
            hoverTimeMs = newTimeMs;
            repaint();
        }
    }

    private void drawGrid(Graphics2D g) {
        int areaHeight = graphYEnd - graphYStart;
        if (areaHeight <= 0) {
            return;
        }

        g.setColor(Color.black);
        g.setStroke(new BasicStroke(1f));

        double[] yAmps = {1.0, 0.5, 0.0, -0.5, -1.0};
        String[] yLabels = {"+1.0", "+0.5", "0.0", "-0.5", "-1.0"};
        for (int i = 0; i < yAmps.length; i++) {
            int y = (int) Math.round(translateAmplitudeToY(yAmps[i]));
            g.setColor(i == 2 ? Color.black : Color.gray);
            g.draw(new Line2D.Double(graphXStart, y, graphXEnd, y));

            int labelX = leftPadding - metrics.stringWidth(yLabels[i]) - 6;
            g.setColor(Color.black);
            g.drawString(yLabels[i], labelX, y + (metrics.getAscent() / 2) - 1);
        }

        int windowMs = getDisplayWindowMs();
        double[] relativeMarks = {-0.25, 0.0, 0.25, 0.5, 0.75};
        for (int i = 0; i < relativeMarks.length; i++) {
            double t = relativeMarks[i];
            double x = translateCenteredMsToX(t * windowMs, windowMs);
            g.setColor(t == 0.0 ? Color.black : Color.gray);
            g.draw(new Line2D.Double(x, graphYStart, x, graphYEnd));

            double labelMs = t * windowMs;
            String label = String.format("%.1fms", labelMs);
            if (Math.abs(labelMs - Math.rint(labelMs)) < 0.0001) {
                label = String.format("%.0fms", labelMs);
            }

            int labelW = metrics.stringWidth(label);
            int drawX = (int) Math.round(x - (labelW / 2.0));
            int drawY = graphYEnd + metrics.getAscent() + 2;
            g.setColor(Color.black);
            g.drawString(label, drawX, drawY);
        }

        g.drawString("Step amplitude (normalized)", 6, graphYStart - 2);
    }

    private void drawCurves(Graphics2D g) {
        int windowMs = getDisplayWindowMs();
        if (sampleRateHz <= 0 || windowMs <= 0) {
            return;
        }

        int maxSamples = Math.max(1, (int) Math.round((sampleRateHz * windowMs) / 1000.0));
        int preWindowSamples = Math.max(1, (int) Math.round(maxSamples * PRE_PEAK_RATIO));
        int postWindowSamples = Math.max(1, maxSamples - preWindowSamples);

        StepWindow beforeLeft = showBeforeLeft ? buildStepWindow(measuredLeft, preWindowSamples, postWindowSamples, windowMs) : null;
        StepWindow beforeRight = showBeforeRight ? buildStepWindow(measuredRight, preWindowSamples, postWindowSamples, windowMs) : null;
        StepWindow afterLeft = showAfterLeft ? buildStepWindow(predictedLeft, preWindowSamples, postWindowSamples, windowMs) : null;
        StepWindow afterRight = showAfterRight ? buildStepWindow(predictedRight, preWindowSamples, postWindowSamples, windowMs) : null;

        double sharedPeak = 0.0;
        sharedPeak = Math.max(sharedPeak, findStepPeak(beforeLeft));
        sharedPeak = Math.max(sharedPeak, findStepPeak(beforeRight));
        sharedPeak = Math.max(sharedPeak, findStepPeak(afterLeft));
        sharedPeak = Math.max(sharedPeak, findStepPeak(afterRight));
        if (sharedPeak <= 0.0 || !Double.isFinite(sharedPeak)) {
            sharedPeak = 1.0;
        }

        drawStepWindow(g, beforeLeft, BEFORE_LEFT_COLOR, 1.4f, sharedPeak, windowMs);
        drawStepWindow(g, beforeRight, BEFORE_RIGHT_COLOR, 1.4f, sharedPeak, windowMs);
        drawStepWindow(g, afterLeft, AFTER_LEFT_COLOR, 2.0f, sharedPeak, windowMs);
        drawStepWindow(g, afterRight, AFTER_RIGHT_COLOR, 2.0f, sharedPeak, windowMs);
    }

    private StepWindow buildStepWindow(double[] samples,
                                       int preWindowSamples,
                                       int postWindowSamples,
                                       int windowMs) {
        if (samples == null || samples.length < 2) {
            return null;
        }

        int peakIndex = findPeakIndex(samples);
        if (peakIndex < 0) {
            return null;
        }

        int start = Math.max(0, peakIndex - preWindowSamples);
        int end = Math.min(samples.length - 1, peakIndex + postWindowSamples);
        if (end - start < 1) {
            return null;
        }

        int count = end - start + 1;
        double[] timeMs = new double[count];
        double[] stepValues = new double[count];

        double cumulative = 0.0;
        int out = 0;
        for (int i = start; i <= end; i++) {
            double relativeTimeMs = (1000.0 * (i - peakIndex)) / sampleRateHz;
            if (relativeTimeMs < (-windowMs * PRE_PEAK_RATIO) || relativeTimeMs > (windowMs * POST_PEAK_RATIO)) {
                continue;
            }

            cumulative += samples[i];
            timeMs[out] = relativeTimeMs;
            stepValues[out] = cumulative;
            out++;
        }

        if (out < 2) {
            return null;
        }

        if (out < count) {
            double[] trimmedTime = new double[out];
            double[] trimmedStep = new double[out];
            System.arraycopy(timeMs, 0, trimmedTime, 0, out);
            System.arraycopy(stepValues, 0, trimmedStep, 0, out);
            return new StepWindow(trimmedTime, trimmedStep);
        }

        return new StepWindow(timeMs, stepValues);
    }

    private void drawStepWindow(Graphics2D g,
                                StepWindow window,
                                Color color,
                                float strokeWidth,
                                double sharedPeak,
                                int windowMs) {
        if (window == null || window.timeMs.length < 2) {
            return;
        }

        g.setColor(color);
        g.setStroke(new BasicStroke(strokeWidth));

        double previousX = Double.NaN;
        double previousY = Double.NaN;
        for (int i = 0; i < window.timeMs.length; i++) {
            double normalized = window.stepValues[i] / sharedPeak;
            if (normalized > 1.0) {
                normalized = 1.0;
            }
            else if (normalized < -1.0) {
                normalized = -1.0;
            }

            double x = translateCenteredMsToX(window.timeMs[i], windowMs);
            double y = translateAmplitudeToY(normalized);

            if (Double.isFinite(previousX) && Double.isFinite(previousY)) {
                g.draw(new Line2D.Double(previousX, previousY, x, y));
            }

            previousX = x;
            previousY = y;
        }
    }

    private double findStepPeak(StepWindow window) {
        if (window == null) {
            return 0.0;
        }

        double peak = 0.0;
        for (int i = 0; i < window.stepValues.length; i++) {
            peak = Math.max(peak, Math.abs(window.stepValues[i]));
        }
        return peak;
    }

    private int findPeakIndex(double[] samples) {
        if (samples == null || samples.length == 0) {
            return -1;
        }

        int bestIndex = 0;
        double bestValue = 0.0;
        for (int i = 0; i < samples.length; i++) {
            double value = Math.abs(samples[i]);
            if (value > bestValue) {
                bestValue = value;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    private double translateCenteredMsToX(double relativeTimeMs, int windowMs) {
        if (windowMs <= 0) {
            return graphXStart;
        }

        double minMs = -windowMs * PRE_PEAK_RATIO;
        double maxMs = windowMs * POST_PEAK_RATIO;
        double clamped = Math.max(minMs, Math.min(maxMs, relativeTimeMs));
        double normalized = (clamped - minMs) / (maxMs - minMs);
        double width = graphXEnd - graphXStart;
        return graphXStart + (normalized * width);
    }

    private double translateXToCenteredMs(double x, int windowMs) {
        if (windowMs <= 0 || graphXEnd <= graphXStart) {
            return 0.0;
        }

        double minMs = -windowMs * PRE_PEAK_RATIO;
        double maxMs = windowMs * POST_PEAK_RATIO;
        double normalized = (x - graphXStart) / (double) (graphXEnd - graphXStart);
        if (normalized < 0.0) {
            normalized = 0.0;
        }
        else if (normalized > 1.0) {
            normalized = 1.0;
        }

        return minMs + (normalized * (maxMs - minMs));
    }

    private double translateAmplitudeToY(double amplitude) {
        double clamped = Math.max(-1.0, Math.min(1.0, amplitude));
        double normalized = (1.0 - clamped) / 2.0;
        return graphYStart + ((graphYEnd - graphYStart) * normalized);
    }

    private void drawLegend(Graphics2D g) {
        legendWidth = 220;
        legendHeight = 78;
        int graphHeight = graphYEnd - graphYStart;

        legendX = graphXStart + 8;
        legendY = graphYStart + (int) Math.round(graphHeight * 0.70);
        if (legendX < graphXStart + 4) {
            legendX = graphXStart + 4;
        }
        if (legendX + legendWidth > graphXEnd - 4) {
            legendX = graphXEnd - legendWidth - 4;
        }
        if (legendY < graphYStart + 4) {
            legendY = graphYStart + 4;
        }
        if (legendY + legendHeight > graphYEnd - 4) {
            legendY = graphYEnd - legendHeight - 4;
        }

        g.setColor(new Color(255, 255, 255, 220));
        g.fillRect(legendX, legendY, legendWidth, legendHeight);
        g.setColor(Color.darkGray);
        g.drawRect(legendX, legendY, legendWidth, legendHeight);

        int lineX1 = legendX + 10;
        int lineX2 = legendX + 30;
        int y1 = legendY + 16;
        int y2 = legendY + 32;
        int y3 = legendY + 48;
        int y4 = legendY + 64;

        g.setStroke(new BasicStroke(2f));

        g.setColor(BEFORE_LEFT_COLOR);
        g.draw(new Line2D.Double(lineX1, y1, lineX2, y1));
        g.setColor(Color.black);
        g.drawString("Before Step Left", legendX + 36, y1 + 4);

        g.setColor(BEFORE_RIGHT_COLOR);
        g.draw(new Line2D.Double(lineX1, y2, lineX2, y2));
        g.setColor(Color.black);
        g.drawString("Before Step Right", legendX + 36, y2 + 4);

        g.setColor(AFTER_LEFT_COLOR);
        g.draw(new Line2D.Double(lineX1, y3, lineX2, y3));
        g.setColor(Color.black);
        g.drawString("After Step Left", legendX + 36, y3 + 4);

        g.setColor(AFTER_RIGHT_COLOR);
        g.draw(new Line2D.Double(lineX1, y4, lineX2, y4));
        g.setColor(Color.black);
        g.drawString("After Step Right", legendX + 36, y4 + 4);
    }

    private void drawMouseTimeReadout(Graphics2D g) {
        if (!Double.isFinite(hoverTimeMs) || legendWidth <= 0 || legendHeight <= 0) {
            return;
        }

        String text = String.format("Time: %.2f ms", hoverTimeMs);
        Font originalFont = g.getFont();
        Font readoutFont = originalFont.deriveFont(Math.max(6.0f, originalFont.getSize2D()));
        g.setFont(readoutFont);
        FontMetrics fm = g.getFontMetrics();

        int textX = legendX + 8;
        int textY = legendY + legendHeight + fm.getAscent() + 4;
        if (textY > this.getHeight() - 4) {
            g.setFont(originalFont);
            return;
        }

        int boxWidth = Math.min(legendWidth - 8, fm.stringWidth(text) + 8);
        int boxHeight = fm.getHeight() + 4;
        int boxX = legendX + 4;
        int boxY = textY - fm.getAscent() - 2;

        g.setColor(new Color(255, 255, 255, 215));
        g.fillRect(boxX, boxY, boxWidth, boxHeight);
        g.setColor(Color.darkGray);
        g.drawRect(boxX, boxY, boxWidth, boxHeight);

        g.setColor(Color.black);
        g.drawString(text, textX, textY - 1);
        g.setFont(originalFont);
    }

    private void drawDisclaimer(Graphics2D g) {
        String disclaimer = "Step response preview from peak-centered impulse window, normalized to shared step peak.";
        Font originalFont = g.getFont();
        Font disclaimerFont = originalFont.deriveFont(Math.max(6.0f, originalFont.getSize2D() - 1.0f));
        g.setFont(disclaimerFont);
        FontMetrics fm = g.getFontMetrics();

        int textX = graphXStart + 4;
        int textY = graphYEnd + fm.getAscent() + 6;
        if (textY > this.getHeight() - 4) {
            textY = this.getHeight() - 4;
        }

        int availableWidth = Math.max(20, graphXEnd - graphXStart - 8);
        String toDraw = disclaimer;
        while (fm.stringWidth(toDraw) > availableWidth && toDraw.length() > 12) {
            toDraw = toDraw.substring(0, toDraw.length() - 4).trim() + "...";
        }

        g.setColor(Color.darkGray);
        g.drawString(toDraw, textX, textY);
        g.setFont(originalFont);
    }

    private void drawFrameBorder(Graphics2D g) {
        g.setPaint(Color.black);
        g.draw(new Line2D.Double(0, 0, this.getWidth() - 1, 0));
        g.draw(new Line2D.Double(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight() - 1));
        g.draw(new Line2D.Double(this.getWidth() - 1, this.getHeight() - 1, 0, this.getHeight() - 1));
        g.draw(new Line2D.Double(0, this.getHeight() - 1, 0, 0));
    }

    private static class StepWindow {
        private final double[] timeMs;
        private final double[] stepValues;

        private StepWindow(double[] timeMs, double[] stepValues) {
            this.timeMs = timeMs;
            this.stepValues = stepValues;
        }
    }
}
