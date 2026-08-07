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
import java.awt.geom.Line2D;

import javax.swing.JPanel;

public class PredictedResponseGraph extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color LEFT_COLOR = new Color(20, 140, 70);
    private static final Color RIGHT_COLOR = new Color(40, 90, 210);
    private static final Color PREDICTED_LEFT_COLOR = new Color(210, 80, 35);
    private static final Color PREDICTED_RIGHT_COLOR = new Color(160, 70, 190);

    private static final double MAX_DISPLAY_FREQUENCY = 22050.0;
    private static final double LOG_AXIS_MIN_FREQUENCY = 20.0;
    private static final double LOW_FREQUENCY_STUB_MAX = 20.0;
    private static final double LOW_FREQUENCY_STUB_RATIO = 0.03;
    private static final double MAX_DISPLAY_DB = 0.0;
    private static final double MIN_DISPLAY_DB = -60.0;

    private final String[] amplitudeLabels = new String[16];
    private FontMetrics metrics;

    private int topPadding = 10;
    private int rightPadding = 50;
    private int spaceForAmplitudeLabels = 40;
    private int spaceForFrequencyLabels = 20;
    private int graphXLeftPadding = 10;

    private int graphXStart = spaceForAmplitudeLabels + graphXLeftPadding;
    private int graphXEnd;
    private int graphYStart;
    private int graphYEnd;

    private FrequencyAmplitudePoints measuredLeft;
    private FrequencyAmplitudePoints measuredRight;
    private FrequencyAmplitudePoints predictedLeft;
    private FrequencyAmplitudePoints predictedRight;

    public PredictedResponseGraph() {
        initializeAmplitudeLabels();
    }

    public void setCurves(FrequencyAmplitudePoints measuredLeft,
                          FrequencyAmplitudePoints measuredRight,
                          FrequencyAmplitudePoints predictedLeft,
                          FrequencyAmplitudePoints predictedRight) {
        this.measuredLeft = measuredLeft;
        this.measuredRight = measuredRight;
        this.predictedLeft = predictedLeft;
        this.predictedRight = predictedRight;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        graphXEnd = this.getWidth() - rightPadding;
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
        int fontSize = (int) Math.round(7.0 * screenRes / 72.0);
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        g2.setFont(font);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        metrics = g2.getFontMetrics();

        drawFrameBorder(g2);
        drawAmplitudeLabels(g2);
        drawAmplitudeLines(g2);
        drawFrequencyGrid(g2);
        drawCurves(g2);
        drawLegend(g2);
    }

    private void drawCurves(Graphics2D g) {
        drawCurve(g, measuredLeft, LEFT_COLOR, 1.6f);
        drawCurve(g, measuredRight, RIGHT_COLOR, 1.6f);
        drawCurve(g, predictedLeft, PREDICTED_LEFT_COLOR, 2.0f);
        drawCurve(g, predictedRight, PREDICTED_RIGHT_COLOR, 2.0f);
    }

    private void drawCurve(Graphics2D g, FrequencyAmplitudePoints points, Color color, float strokeWidth) {
        if (points == null || points.getNumberOfFrequencyDataPoints() < 2) {
            return;
        }

        g.setColor(color);
        g.setStroke(new BasicStroke(strokeWidth));

        double previousX = Double.NaN;
        double previousY = Double.NaN;
        int pointCount = points.getNumberOfFrequencyDataPoints();
        for (int i = 1; i <= pointCount; i++) {
            FrequencyAmplitudePoint point = points.getFrequencyAmplitudePoint(i);
            double x = translateFrequencyToXAxisDoublePoint(point.getFrequency());
            double y = translateAmplitudeToYAxisDoublePoint(point.getAmplitude());

            if (!Double.isFinite(x) || !Double.isFinite(y)) {
                continue;
            }

            if (Double.isFinite(previousX) && Double.isFinite(previousY)) {
                g.draw(new Line2D.Double(previousX, previousY, x, y));
            }

            previousX = x;
            previousY = y;
        }
    }

    private double translateFrequencyToXAxisDoublePoint(double frequency) {
        if (frequency < 0.0 || frequency > MAX_DISPLAY_FREQUENCY) {
            return -1.0;
        }

        double width = graphXEnd - graphXStart;
        if (width <= 0.0) {
            return -1.0;
        }

        double stubWidth = width * LOW_FREQUENCY_STUB_RATIO;
        if (frequency <= LOW_FREQUENCY_STUB_MAX) {
            double t = frequency / LOW_FREQUENCY_STUB_MAX;
            return graphXStart + (stubWidth * t);
        }

        double logMin = Math.log10(LOG_AXIS_MIN_FREQUENCY);
        double logMax = Math.log10(MAX_DISPLAY_FREQUENCY);
        double logFreq = Math.log10(Math.max(LOG_AXIS_MIN_FREQUENCY, frequency));
        double t = (logFreq - logMin) / (logMax - logMin);
        return graphXStart + stubWidth + ((width - stubWidth) * t);
    }

    private double translateAmplitudeToYAxisDoublePoint(double amplitude) {
        int area = graphYEnd - graphYStart;
        if (area <= 0) {
            return graphYStart;
        }

        double clamped = Math.max(MIN_DISPLAY_DB, Math.min(MAX_DISPLAY_DB, amplitude));
        double normalized = (MAX_DISPLAY_DB - clamped) / (MAX_DISPLAY_DB - MIN_DISPLAY_DB);
        return graphYStart + (area * normalized);
    }

    private void drawAmplitudeLines(Graphics2D g) {
        int availableHeight = this.getHeight() - topPadding - spaceForFrequencyLabels;
        int spacing = availableHeight / amplitudeLabels.length;
        int start = topPadding + metrics.getHeight() + spaceForFrequencyLabels;
        graphYStart = start;
        graphYEnd = (spacing * (amplitudeLabels.length - 1)) + start;

        g.setPaint(Color.black);
        for (int counter = 0; counter < amplitudeLabels.length; counter++) {
            g.draw(new Line2D.Double(graphXStart, (spacing * counter) + start, graphXEnd, (spacing * counter) + start));
        }

        g.setPaint(Color.gray);
        for (int counter = 0; counter < amplitudeLabels.length - 1; counter++) {
            g.draw(new Line2D.Double(graphXStart, (spacing * counter) + start + (int) (spacing * .5), graphXEnd,
                    (spacing * counter) + start + (int) (spacing * .5)));
        }
    }

    private void drawAmplitudeLabels(Graphics2D g) {
        g.setPaint(Color.black);
        int stringHeight = metrics.getHeight();
        int availableHeight = this.getHeight() - topPadding - spaceForFrequencyLabels;
        int spacing = availableHeight / amplitudeLabels.length;
        int start = topPadding + (int) (stringHeight * 1.25) + spaceForFrequencyLabels;
        for (int counter = 0; counter < amplitudeLabels.length; counter++) {
            int stringWidth = metrics.stringWidth(amplitudeLabels[counter]);
            g.drawString(amplitudeLabels[counter], spaceForAmplitudeLabels - stringWidth, (spacing * counter) + start);
        }
    }

    private void drawFrequencyGrid(Graphics2D g) {
        double[] majorFrequencies = {0, 100, 400, 1000, 6000, 20000, 21000, 22050};
        double[] minorFrequencies = {20, 40, 60, 80, 200, 300, 600, 800, 2000, 3000, 4000, 5000, 8000, 10000, 12000, 14000, 16000, 18000};

        g.setPaint(Color.black);
        for (int i = 0; i < majorFrequencies.length; i++) {
            double x = translateFrequencyToXAxisDoublePoint(majorFrequencies[i]);
            g.draw(new Line2D.Double(x, graphYStart, x, graphYEnd));
        }

        g.setPaint(Color.gray);
        for (int i = 0; i < minorFrequencies.length; i++) {
            double x = translateFrequencyToXAxisDoublePoint(minorFrequencies[i]);
            g.draw(new Line2D.Double(x, graphYStart, x, graphYEnd));
        }

        Font originalFont = g.getFont();
        Font xAxisLabelFont = originalFont.deriveFont(Math.max(6.0f, originalFont.getSize2D() - 1.0f));
        g.setFont(xAxisLabelFont);
        FontMetrics xAxisMetrics = g.getFontMetrics();

        String[] labels = {"0Hz", "20Hz", "40Hz", "60Hz", "80Hz", "100Hz", "400Hz", "1kHz", "6kHz", "20kHz"};
        double[] labelFreq = {0, 20, 40, 60, 80, 100, 400, 1000, 6000, 20000};
        int yPoint = graphYStart - xAxisMetrics.getHeight();
        g.setPaint(Color.black);
        int lastLabelRight = Integer.MIN_VALUE;
        for (int i = 0; i < labels.length; i++) {
            double x = translateFrequencyToXAxisDoublePoint(labelFreq[i]);
            int drawX = (int) Math.round(x - (xAxisMetrics.stringWidth(labels[i]) / 2.0));
            // Prevent unreadable overlap while still labeling the sub-100 Hz grid lines.
            if (drawX <= lastLabelRight + 2) {
                continue;
            }
            g.drawString(labels[i], drawX, yPoint);
            lastLabelRight = drawX + xAxisMetrics.stringWidth(labels[i]);
        }

        g.setFont(originalFont);
    }

    private void drawLegend(Graphics2D g) {
        int legendWidth = 180;
        int legendHeight = 78;
        int graphWidth = graphXEnd - graphXStart;
        int graphHeight = graphYEnd - graphYStart;

        // Place legend in the lower-middle area of the plot and clamp to graph bounds.
        int legendX = graphXStart + ((graphWidth - legendWidth) / 2);
        int legendY = graphYStart + (int) Math.round(graphHeight * 0.70);
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

        g.setColor(LEFT_COLOR);
        g.draw(new Line2D.Double(lineX1, y1, lineX2, y1));
        g.setColor(Color.black);
        g.drawString("Measured Left (ERB)", legendX + 36, y1 + 4);

        g.setColor(RIGHT_COLOR);
        g.draw(new Line2D.Double(lineX1, y2, lineX2, y2));
        g.setColor(Color.black);
        g.drawString("Measured Right (ERB)", legendX + 36, y2 + 4);

        g.setColor(PREDICTED_LEFT_COLOR);
        g.draw(new Line2D.Double(lineX1, y3, lineX2, y3));
        g.setColor(Color.black);
        g.drawString("Predicted Left", legendX + 36, y3 + 4);

        g.setColor(PREDICTED_RIGHT_COLOR);
        g.draw(new Line2D.Double(lineX1, y4, lineX2, y4));
        g.setColor(Color.black);
        g.drawString("Predicted Right", legendX + 36, y4 + 4);
    }

    private void drawFrameBorder(Graphics2D g) {
        g.setPaint(Color.black);
        g.draw(new Line2D.Double(0, 0, this.getWidth() - 1, 0));
        g.draw(new Line2D.Double(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight() - 1));
        g.draw(new Line2D.Double(this.getWidth() - 1, this.getHeight() - 1, 0, this.getHeight() - 1));
        g.draw(new Line2D.Double(0, this.getHeight() - 1, 0, 0));
    }

    private void initializeAmplitudeLabels() {
        int currentAmplitude = 0;
        int counter = 0;
        do {
            amplitudeLabels[counter] = Integer.toString(currentAmplitude) + " dB";
            counter++;
            currentAmplitude -= 4;
        } while (counter < amplitudeLabels.length);
    }
}
