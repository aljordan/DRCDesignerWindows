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
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class TargetDesigner extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Color LEFT_RESPONSE_COLOR = new Color(20, 140, 70);
	private static final Color RIGHT_RESPONSE_COLOR = new Color(40, 90, 210);
	private FrequencyAmplitudePoints fap;
	private String[] amplitudeLabels = new String[16];
	private int topPadding = 10;
	private int rightPadding = 50;
	private int graphXLeftPadding = 10;
	private FontMetrics metrics;
	private int spaceForAmplitudeLabels = 40;
	private int spaceForFrequencyLabels = 20;
	private int graphXStart = spaceForAmplitudeLabels + graphXLeftPadding;
	private int graphXEnd;
	private int graphYStart;
	private int graphYEnd;
	private int graphXAvailableSpace;
	private TargetDesigner currentInstance;

	// Major X-axis points below
	private int khzPoint_0;
	private int khzPoint_100;
	private int khzPoint_400;
	private int khzPoint_1000;
	private int khzPoint_6000;
	private int khzPoint_20000;
	private int khzPoint_semifinal;
	private int khzPoint_final;
	private static final float X_AXIS_LABEL_FONT_DELTA = 1.0f;

	private static final double MAX_DISPLAY_FREQUENCY = 22050.0;
	private static final double LOG_SCALE_MIN_FREQUENCY = 1.0;
	// Set to false to restore the legacy piecewise x-axis mapping.
	private static final boolean USE_CONTINUOUS_LOG_X_AXIS = true;
	// Set to false to hide hover/click frequency readout overlay.
	private static final boolean USE_HOVER_CLICK_READOUT = true;
	private static final double LOG_AXIS_MIN_FREQUENCY = 20.0;
	private static final double LOW_FREQUENCY_STUB_MAX = 20.0;
	private static final double LOW_FREQUENCY_STUB_RATIO = 0.03;
	// Set to false to revert to the previous uniform cubic B-spline renderer.
	private static final boolean USE_MONOTONE_INTERPOLATION = false;
	// Set to false to use fully parametric B-spline X (can backtrack on some control-point shapes).
	private static final boolean USE_BSPLINE_MONOTONIC_X = true;
	// Set to false to disable long-span amplitude blending for B-spline mode.
	private static final boolean USE_BSPLINE_LONG_SPAN_LINEAR_BLEND = false;
	private static final double BSPLINE_LONG_SPAN_RATIO_START = 8.0;
	private static final double BSPLINE_LONG_SPAN_RATIO_FULL = 30.0;
	// Set to false to disable endpoint bridging for uniform cubic B-spline rendering.
	private static final boolean USE_BSPLINE_ENDPOINT_BRIDGING = true;
	private static final int READOUT_BOX_WIDTH = 250;
	private static final int READOUT_BOX_HEIGHT_PADDING = 12;
	private static final int READOUT_BOX_TEXT_LINES = 3;
	private Options options;
	private boolean mouseInGraph;
	private double hoverFrequency;
	private double hoverAmplitude;
	private double lastClickFrequency;
	private double lastClickAmplitude;

	/**
	 * This is the default constructor
	 */
	public TargetDesigner(Options options) {
		super();
		this.options = options;
		fap = null;
		mouseInGraph = false;
		hoverFrequency = -1;
		hoverAmplitude = 0;
		lastClickFrequency = -1;
		lastClickAmplitude = 0;
		initialize();
		initializeAmplitudeLabels();
		currentInstance = this;
	}

	public void drawTarget(FrequencyAmplitudePoints fap) {
		this.fap = fap;
		options.setPoints(fap);
		this.repaint();
	}

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
		computeMajorXAxisFrequencyPoints();
		drawFrequencyLines(g2);
		drawMajorFrequencyLabels(g2);
		drawMeasuredResponseOverlays(g2);

		if (fap != null) {
			drawFrequencyAmplitudePoints(g2);
		}

		drawResponseLegend(g2);

		if (USE_HOVER_CLICK_READOUT) {
			drawHoverClickReadout(g2);
		}
	}

	private void drawHoverClickReadout(Graphics2D g) {
		int lineHeight = metrics.getHeight();
		int boxWidth = READOUT_BOX_WIDTH;
		int boxHeight = lineHeight * READOUT_BOX_TEXT_LINES + READOUT_BOX_HEIGHT_PADDING;
		int boxX = graphXStart + ((graphXEnd - graphXStart - boxWidth) / 2);
		int boxY = graphYStart + ((graphYEnd - graphYStart - boxHeight) / 2);

		g.setColor(new Color(255, 255, 255, 220));
		g.fillRect(boxX, boxY, boxWidth, boxHeight);
		g.setColor(Color.darkGray);
		g.drawRect(boxX, boxY, boxWidth, boxHeight);

		String hoverText = "Hover: outside graph";
		if (mouseInGraph && hoverFrequency >= 0.0) {
			hoverText = "Hover: " + Math.round(hoverFrequency) + " Hz, " + String.format("%.1f", hoverAmplitude) + " dB";
		}

		String clickText = "Last click: n/a";
		if (lastClickFrequency >= 0.0) {
			clickText = "Last click: " + Math.round(lastClickFrequency) + " Hz, " + String.format("%.1f", lastClickAmplitude) + " dB";
		}

		g.setColor(Color.black);
		g.drawString("Readout", boxX + 8, boxY + lineHeight);
		g.drawString(hoverText, boxX + 8, boxY + (lineHeight * 2));
		g.drawString(clickText, boxX + 8, boxY + (lineHeight * 3));
	}

	private void drawPoint(Graphics2D g, int x, int y) {
		int radius = 5;
		g.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);
	}

	private void drawMeasuredResponseOverlays(Graphics2D g) {
		drawMeasuredResponseCurve(g, options.getLeftChannelResponsePoints(), LEFT_RESPONSE_COLOR);
		drawMeasuredResponseCurve(g, options.getRightChannelResponsePoints(), RIGHT_RESPONSE_COLOR);
	}

	private void drawMeasuredResponseCurve(Graphics2D g, FrequencyAmplitudePoints responsePoints, Color color) {
		if (responsePoints == null || responsePoints.getNumberOfFrequencyDataPoints() < 2) {
			return;
		}

		Shape originalClip = g.getClip();
		g.clip(new Rectangle2D.Double(graphXStart, graphYStart, graphXEnd - graphXStart, graphYEnd - graphYStart));

		try {
			g.setColor(color);
			g.setStroke(new BasicStroke(1.5f));

			double previousX = Double.NaN;
			double previousY = Double.NaN;
			int pointCount = responsePoints.getNumberOfFrequencyDataPoints();
			for (int i = 1; i <= pointCount; i++) {
				FrequencyAmplitudePoint point = responsePoints.getFrequencyAmplitudePoint(i);
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
		finally {
			g.setClip(originalClip);
		}
	}

	private void drawResponseLegend(Graphics2D g) {
		boolean hasLeft = options.getLeftChannelResponsePoints() != null;
		boolean hasRight = options.getRightChannelResponsePoints() != null;
		if (!hasLeft && !hasRight) {
			return;
		}

		int legendPadding = 8;
		int lineSampleWidth = 18;
		int rowHeight = Math.max(14, metrics.getHeight());
		int legendWidth = 120;
		int rowCount = (hasLeft ? 1 : 0) + (hasRight ? 1 : 0);
		int legendHeight = (rowHeight * rowCount) + (legendPadding * 2);

		int readoutLineHeight = metrics.getHeight();
		int readoutHeight = readoutLineHeight * READOUT_BOX_TEXT_LINES + READOUT_BOX_HEIGHT_PADDING;
		int readoutX = graphXStart + ((graphXEnd - graphXStart - READOUT_BOX_WIDTH) / 2);
		int readoutY = graphYStart + ((graphYEnd - graphYStart - readoutHeight) / 2);

		int legendX = readoutX + ((READOUT_BOX_WIDTH - legendWidth) / 2);
		int legendY = readoutY + readoutHeight + 8;

		int minX = graphXStart + 6;
		int maxX = graphXEnd - legendWidth - 6;
		legendX = Math.max(minX, Math.min(maxX, legendX));

		int minY = graphYStart + 6;
		int maxY = graphYEnd - legendHeight - 6;
		if (legendY > maxY) {
			legendY = readoutY - legendHeight - 8;
		}
		legendY = Math.max(minY, Math.min(maxY, legendY));

		g.setColor(new Color(255, 255, 255, 220));
		g.fillRect(legendX, legendY, legendWidth, legendHeight);
		g.setColor(Color.darkGray);
		g.drawRect(legendX, legendY, legendWidth, legendHeight);

		g.setStroke(new BasicStroke(2f));
		int rowIndex = 0;
		if (hasLeft) {
			int rowY = legendY + legendPadding + (rowIndex * rowHeight) + (rowHeight / 2) + 1;
			g.setColor(LEFT_RESPONSE_COLOR);
			g.draw(new Line2D.Double(legendX + legendPadding, rowY, legendX + legendPadding + lineSampleWidth, rowY));
			g.setColor(Color.black);
			g.drawString("Left response", legendX + legendPadding + lineSampleWidth + 6, rowY + (rowHeight / 3));
			rowIndex++;
		}

		if (hasRight) {
			int rowY = legendY + legendPadding + (rowIndex * rowHeight) + (rowHeight / 2) + 1;
			g.setColor(RIGHT_RESPONSE_COLOR);
			g.draw(new Line2D.Double(legendX + legendPadding, rowY, legendX + legendPadding + lineSampleWidth, rowY));
			g.setColor(Color.black);
			g.drawString("Right response", legendX + legendPadding + lineSampleWidth + 6, rowY + (rowHeight / 3));
		}
	}

	private void drawFrequencyAmplitudePoints(Graphics2D g) {
		g.setStroke(new BasicStroke(2F));
		int pointCount = fap.getNumberOfFrequencyDataPoints();
		if (pointCount < 1) {
			return;
		}

		List<SplineControlPoint> sourcePoints = new ArrayList<SplineControlPoint>();
		for (int index = 1; index <= pointCount; index++) {
			FrequencyAmplitudePoint point = fap.getFrequencyAmplitudePoint(index);
			sourcePoints.add(new SplineControlPoint(point.getFrequency(), point.getAmplitude()));
		}

		g.setColor(Color.blue);
		for (SplineControlPoint point : sourcePoints) {
			drawPoint(g, translateFrequencyToXAxisPoint(point.frequency), translateAmplitudeToYAxisPoint(point.amplitude));
		}

		if (sourcePoints.size() < 2) {
			return;
		}

		g.setPaint(Color.red);

		List<SplineControlPoint> logDomainControlPoints = new ArrayList<SplineControlPoint>();
		SplineControlPoint firstPoint = sourcePoints.get(0);
		int firstPositiveIndex = -1;

		for (int index = 0; index < sourcePoints.size(); index++) {
			if (sourcePoints.get(index).frequency > 0.0) {
				firstPositiveIndex = index;
				break;
			}
		}

		if (firstPositiveIndex > -1 && firstPoint.frequency == 0.0) {
			SplineControlPoint firstPositivePoint = sourcePoints.get(firstPositiveIndex);
			SplineControlPoint firstLogPoint = firstPositivePoint;

			if (firstPositivePoint.frequency > LOG_SCALE_MIN_FREQUENCY) {
				double fakeAmplitude = interpolate(
						firstPoint.frequency,
						firstPoint.amplitude,
						firstPositivePoint.frequency,
						firstPositivePoint.amplitude,
						LOG_SCALE_MIN_FREQUENCY);
				firstLogPoint = new SplineControlPoint(LOG_SCALE_MIN_FREQUENCY, fakeAmplitude);
			}

			g.draw(new Line2D.Double(
					translateFrequencyToXAxisDoublePoint(firstPoint.frequency),
					translateAmplitudeToYAxisDoublePoint(firstPoint.amplitude),
					translateFrequencyToXAxisDoublePoint(firstLogPoint.frequency),
					translateAmplitudeToYAxisDoublePoint(firstLogPoint.amplitude)));

			logDomainControlPoints.add(firstLogPoint);
			for (int index = firstPositiveIndex; index < sourcePoints.size(); index++) {
				SplineControlPoint point = sourcePoints.get(index);
				if (point.frequency > 0.0 && !samePoint(point, firstLogPoint)) {
					logDomainControlPoints.add(point);
				}
			}
		} else {
			for (SplineControlPoint point : sourcePoints) {
				if (point.frequency > 0.0) {
					logDomainControlPoints.add(point);
				}
			}
		}

		if (USE_MONOTONE_INTERPOLATION) {
			drawCurveInGraphClip(g, logDomainControlPoints, true);
		} else {
			drawCurveInGraphClip(g, logDomainControlPoints, false);
		}
	}

	private void drawCurveInGraphClip(Graphics2D g, List<SplineControlPoint> controlPoints, boolean useMonotone) {
		Shape originalClip = g.getClip();
		g.clip(new Rectangle2D.Double(graphXStart, graphYStart, graphXEnd - graphXStart, graphYEnd - graphYStart));
		try {
			if (useMonotone) {
				drawMonotoneCubicInterpolation(g, controlPoints);
			} else {
				drawUniformCubicBSpline(g, controlPoints);
			}
		} finally {
			g.setClip(originalClip);
		}
	}

	private boolean samePoint(SplineControlPoint a, SplineControlPoint b) {
		return a.frequency == b.frequency && a.amplitude == b.amplitude;
	}

	private void drawUniformCubicBSpline(Graphics2D g, List<SplineControlPoint> controlPoints) {
		if (controlPoints.size() < 2) {
			return;
		}

		SplineControlPoint firstControlPoint = controlPoints.get(0);
		SplineControlPoint lastControlPoint = controlPoints.get(controlPoints.size() - 1);
		double firstControlX = translateFrequencyToXAxisDoublePoint(firstControlPoint.frequency);
		double firstControlY = translateAmplitudeToYAxisDoublePoint(firstControlPoint.amplitude);
		double lastControlX = translateFrequencyToXAxisDoublePoint(lastControlPoint.frequency);
		double lastControlY = translateAmplitudeToYAxisDoublePoint(lastControlPoint.amplitude);

		ScreenPoint previous = null;
		ScreenPoint firstDrawnPoint = null;
		ScreenPoint lastDrawnPoint = null;
		for (int segment = 0; segment < controlPoints.size() - 1; segment++) {
			SplineControlPoint p0 = controlPoints.get(Math.max(segment - 1, 0));
			SplineControlPoint p1 = controlPoints.get(segment);
			SplineControlPoint p2 = controlPoints.get(segment + 1);
			SplineControlPoint p3 = controlPoints.get(Math.min(segment + 2, controlPoints.size() - 1));
			double longSpanBlend = calculateLongSpanBlend(p1.frequency, p2.frequency);

			int segmentWidthPixels = Math.abs(translateFrequencyToXAxisPoint(p2.frequency) - translateFrequencyToXAxisPoint(p1.frequency));
			int samplesPerSegment = Math.max(40, Math.min(600, segmentWidthPixels * 8));

			for (int sample = 0; sample <= samplesPerSegment; sample++) {
				double t = (double) sample / (double) samplesPerSegment;
				SplineSample splinePoint = evaluateUniformCubicBSpline(p0, p1, p2, p3, t);
				double logFrequency = splinePoint.logFrequency;
				if (USE_BSPLINE_MONOTONIC_X) {
					logFrequency = p1.logFrequency + ((p2.logFrequency - p1.logFrequency) * t);
				}
				double amplitude = splinePoint.amplitude;
				if (USE_BSPLINE_LONG_SPAN_LINEAR_BLEND && longSpanBlend > 0.0) {
					double linearAmplitude = interpolate(
							p1.logFrequency,
							p1.amplitude,
							p2.logFrequency,
							p2.amplitude,
							logFrequency);
					amplitude = blend(amplitude, linearAmplitude, longSpanBlend);
				}
				double frequency = Math.pow(10.0, logFrequency);
				frequency = Math.max(LOG_SCALE_MIN_FREQUENCY, Math.min(MAX_DISPLAY_FREQUENCY, frequency));
				double x = translateFrequencyToXAxisDoublePoint(frequency);
				double y = translateAmplitudeToYAxisDoublePoint(amplitude);
				double drawX = x;
				double drawY = y;

				if (!Double.isFinite(drawX) || !Double.isFinite(drawY)) {
					previous = null;
					continue;
				}
				if (previous != null) {
					// Monotonic-X mode prevents backtracking while preserving B-spline amplitude smoothing.
					g.draw(new Line2D.Double(previous.x, previous.y, drawX, drawY));
				}
				previous = new ScreenPoint(drawX, drawY);
				if (firstDrawnPoint == null) {
					firstDrawnPoint = previous;
				}
				lastDrawnPoint = previous;
			}
		}

		if (USE_BSPLINE_ENDPOINT_BRIDGING && firstDrawnPoint != null && lastDrawnPoint != null) {
			g.draw(new Line2D.Double(firstControlX, firstControlY, firstDrawnPoint.x, firstDrawnPoint.y));
			g.draw(new Line2D.Double(lastDrawnPoint.x, lastDrawnPoint.y, lastControlX, lastControlY));
		}
	}

	private double calculateLongSpanBlend(double leftFrequency, double rightFrequency) {
		if (rightFrequency <= leftFrequency) {
			return 0.0;
		}
		double safeLeft = Math.max(LOG_SCALE_MIN_FREQUENCY, leftFrequency);
		double safeRight = Math.max(LOG_SCALE_MIN_FREQUENCY, rightFrequency);
		double ratio = safeRight / safeLeft;
		if (ratio <= BSPLINE_LONG_SPAN_RATIO_START) {
			return 0.0;
		}
		if (ratio >= BSPLINE_LONG_SPAN_RATIO_FULL) {
			return 1.0;
		}
		return (ratio - BSPLINE_LONG_SPAN_RATIO_START) / (BSPLINE_LONG_SPAN_RATIO_FULL - BSPLINE_LONG_SPAN_RATIO_START);
	}

	private double blend(double a, double b, double blendAmount) {
		double clampedBlend = Math.max(0.0, Math.min(1.0, blendAmount));
		return (a * (1.0 - clampedBlend)) + (b * clampedBlend);
	}

	private void drawMonotoneCubicInterpolation(Graphics2D g, List<SplineControlPoint> controlPoints) {
		int pointCount = controlPoints.size();
		if (pointCount < 2) {
			return;
		}

		double[] x = new double[pointCount];
		double[] y = new double[pointCount];
		for (int i = 0; i < pointCount; i++) {
			x[i] = controlPoints.get(i).logFrequency;
			y[i] = controlPoints.get(i).amplitude;
		}

		double[] slopes = computeMonotoneSlopes(x, y);
		ScreenPoint previous = null;

		for (int segment = 0; segment < pointCount - 1; segment++) {
			double x0 = x[segment];
			double x1 = x[segment + 1];
			double y0 = y[segment];
			double y1 = y[segment + 1];
			double h = x1 - x0;
			if (h <= 0.0) {
				continue;
			}

			int segmentWidthPixels = Math.abs(
					translateFrequencyToXAxisPoint(controlPoints.get(segment + 1).frequency)
					- translateFrequencyToXAxisPoint(controlPoints.get(segment).frequency));
			int samplesPerSegment = Math.max(40, Math.min(600, segmentWidthPixels * 8));

			for (int sample = 0; sample <= samplesPerSegment; sample++) {
				double t = (double) sample / (double) samplesPerSegment;
				double t2 = t * t;
				double t3 = t2 * t;

				double h00 = (2.0 * t3) - (3.0 * t2) + 1.0;
				double h10 = t3 - (2.0 * t2) + t;
				double h01 = (-2.0 * t3) + (3.0 * t2);
				double h11 = t3 - t2;

				double logFrequency = x0 + (t * h);
				double amplitude =
						(h00 * y0) +
						(h10 * h * slopes[segment]) +
						(h01 * y1) +
						(h11 * h * slopes[segment + 1]);

				double frequency = Math.pow(10.0, logFrequency);
				frequency = Math.max(LOG_SCALE_MIN_FREQUENCY, Math.min(MAX_DISPLAY_FREQUENCY, frequency));
				double sx = translateFrequencyToXAxisDoublePoint(frequency);
				double sy = translateAmplitudeToYAxisDoublePoint(amplitude);
				double drawX = sx;
				double drawY = sy;

				if (!Double.isFinite(drawX) || !Double.isFinite(drawY)) {
					previous = null;
					continue;
				}
				if (previous != null) {
					g.draw(new Line2D.Double(previous.x, previous.y, drawX, drawY));
				}
				previous = new ScreenPoint(drawX, drawY);
			}
		}
	}

	private double[] computeMonotoneSlopes(double[] x, double[] y) {
		int n = x.length;
		double[] slopes = new double[n];
		if (n == 2) {
			double delta = (y[1] - y[0]) / (x[1] - x[0]);
			slopes[0] = delta;
			slopes[1] = delta;
			return slopes;
		}

		double[] h = new double[n - 1];
		double[] delta = new double[n - 1];
		for (int i = 0; i < n - 1; i++) {
			h[i] = x[i + 1] - x[i];
			delta[i] = (y[i + 1] - y[i]) / h[i];
		}

		slopes[0] = delta[0];
		slopes[n - 1] = delta[n - 2];

		for (int i = 1; i < n - 1; i++) {
			if (delta[i - 1] * delta[i] <= 0.0) {
				slopes[i] = 0.0;
			} else {
				double w1 = (2.0 * h[i]) + h[i - 1];
				double w2 = h[i] + (2.0 * h[i - 1]);
				slopes[i] = (w1 + w2) / ((w1 / delta[i - 1]) + (w2 / delta[i]));
			}
		}

		for (int i = 0; i < n - 1; i++) {
			if (delta[i] == 0.0) {
				slopes[i] = 0.0;
				slopes[i + 1] = 0.0;
			} else {
				double a = slopes[i] / delta[i];
				double b = slopes[i + 1] / delta[i];
				double sum = (a * a) + (b * b);
				if (sum > 9.0) {
					double tau = 3.0 / Math.sqrt(sum);
					slopes[i] = tau * a * delta[i];
					slopes[i + 1] = tau * b * delta[i];
				}
			}
		}

		return slopes;
	}

	private SplineSample evaluateUniformCubicBSpline(SplineControlPoint p0, SplineControlPoint p1, SplineControlPoint p2, SplineControlPoint p3, double t) {
		double t2 = t * t;
		double t3 = t2 * t;

		double b0 = (-t3 + (3.0 * t2) - (3.0 * t) + 1.0) / 6.0;
		double b1 = ((3.0 * t3) - (6.0 * t2) + 4.0) / 6.0;
		double b2 = ((-3.0 * t3) + (3.0 * t2) + (3.0 * t) + 1.0) / 6.0;
		double b3 = t3 / 6.0;

		double logFrequency =
				(p0.logFrequency * b0) +
				(p1.logFrequency * b1) +
				(p2.logFrequency * b2) +
				(p3.logFrequency * b3);

		double amplitude =
				(p0.amplitude * b0) +
				(p1.amplitude * b1) +
				(p2.amplitude * b2) +
				(p3.amplitude * b3);

		return new SplineSample(logFrequency, amplitude);
	}

	private double interpolate(double x1, double y1, double x2, double y2, double x) {
		if (x2 == x1) {
			return y1;
		}
		double ratio = (x - x1) / (x2 - x1);
		return y1 + ((y2 - y1) * ratio);
	}

	private int translateFrequencyToXAxisPoint(double frequency) {
		double translated = translateFrequencyToXAxisDoublePoint(frequency);
		if (translated < 0) {
			return -1;
		}
		return (int)Math.round(translated);
	}

	private double translateFrequencyToXAxisDoublePoint(double frequency) {
		if (USE_CONTINUOUS_LOG_X_AXIS) {
			return translateFrequencyToXAxisDoublePointContinuous(frequency);
		}

		double area = 0.0;
		double percentage = 0.0;
		if (frequency >= 0 && frequency <= 100) {
			area = khzPoint_100 - khzPoint_0;
			percentage = frequency / 100.0;
			return khzPoint_0 + (area * percentage);
		}
		if (frequency > 100 && frequency <= 400) {
			area = khzPoint_400 - khzPoint_100;
			percentage = (frequency - 100.0) / 300.0;
			return khzPoint_100 + (area * percentage);
		}
		if (frequency > 400 && frequency <= 1000) {
			area = khzPoint_1000 - khzPoint_400;
			percentage = (frequency - 400.0) / 600.0;
			return khzPoint_400 + (area * percentage);
		}
		if (frequency > 1000 && frequency <= 6000) {
			area = khzPoint_6000 - khzPoint_1000;
			percentage = (frequency - 1000.0) / 5000.0;
			return khzPoint_1000 + (area * percentage);
		}
		if (frequency > 6000 && frequency <= 20000) {
			area = khzPoint_20000 - khzPoint_6000;
			percentage = (frequency - 6000.0) / 14000.0;
			return khzPoint_6000 + (area * percentage);
		}
		if (frequency > 20000 && frequency <= 21000) {
			area = khzPoint_semifinal - khzPoint_20000;
			percentage = (frequency - 20000.0) / 1000.0;
			return khzPoint_20000 + (area * percentage);
		}
		if (frequency > 21000 && frequency <= 22050) {
			area = khzPoint_final - khzPoint_semifinal;
			percentage = (frequency - 21000.0) / 1050.0;
			return khzPoint_semifinal + (area * percentage);
		}
		return -1.0;
	}

	private double translateFrequencyToXAxisDoublePointContinuous(double frequency) {
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

	private double translateXAxisPointToFrequency(int x) {
		if (USE_CONTINUOUS_LOG_X_AXIS) {
			return translateXAxisPointToFrequencyContinuous(x);
		}

		int area = 0;
		double percentage = 0;
		if (x >= khzPoint_0 && x <= khzPoint_100) {
			area = khzPoint_100 - khzPoint_0;
			int selectedPoint = x - khzPoint_0;
			percentage = (double)selectedPoint / (double)area;
			return (double)Math.round(percentage * 100);
		}
		if (x > khzPoint_100 && x <= khzPoint_400) {
			area = khzPoint_400 - khzPoint_100;
			int selectedPoint = x - khzPoint_100;
			percentage = (double)selectedPoint / (double)area;
			return (double)Math.round(percentage * (400 - 100) + 100);
		}
		if (x > khzPoint_400 && x <= khzPoint_1000) {
			area = khzPoint_1000 - khzPoint_400;
			int selectedPoint = x - khzPoint_400;
			percentage = (double)selectedPoint / (double)area;
			return (double)Math.round(percentage * (1000 - 400) + 400);
		}
		if (x > khzPoint_1000 && x <= khzPoint_6000) {
			area = khzPoint_6000 - khzPoint_1000;
			int selectedPoint = x - khzPoint_1000;
			percentage = (double)selectedPoint / (double)area;
			return (double)Math.round(percentage * (6000 - 1000) + 1000);
		}
		if (x > khzPoint_6000 && x <= khzPoint_20000) {
			area = khzPoint_20000 - khzPoint_6000;
			int selectedPoint = x - khzPoint_6000;
			percentage = (double)selectedPoint / (double)area;
			return (double)Math.round(percentage * (20000 - 6000) + 6000);
		}

		if (x > khzPoint_20000 && x <= khzPoint_semifinal) {
			return 21000;
		}

		if (x > khzPoint_semifinal && x <= khzPoint_final) {
			return 22050;
		}

		else {
			return -1;
		}
	}

	private double translateXAxisPointToFrequencyContinuous(int x) {
		if (x < graphXStart || x > graphXEnd) {
			return -1;
		}

		double width = graphXEnd - graphXStart;
		if (width <= 0.0) {
			return -1;
		}

		double stubWidth = width * LOW_FREQUENCY_STUB_RATIO;
		double position = x - graphXStart;

		if (position <= stubWidth) {
			double t = stubWidth == 0.0 ? 0.0 : (position / stubWidth);
			return Math.round(LOW_FREQUENCY_STUB_MAX * t);
		}

		double usableWidth = width - stubWidth;
		double t = usableWidth == 0.0 ? 1.0 : ((position - stubWidth) / usableWidth);
		t = Math.max(0.0, Math.min(1.0, t));
		double logMin = Math.log10(LOG_AXIS_MIN_FREQUENCY);
		double logMax = Math.log10(MAX_DISPLAY_FREQUENCY);
		double logFreq = logMin + ((logMax - logMin) * t);
		double frequency = Math.pow(10.0, logFreq);

		// Preserve easy selection of the two fixed high-frequency control points.
		if (frequency >= 20500.0 && frequency < 21525.0) {
			return 21000;
		}
		if (frequency >= 21525.0) {
			return 22050;
		}

		return Math.round(frequency);
	}

	private int translateAmplitudeToYAxisPoint(double amplitude) {
		double translated = translateAmplitudeToYAxisDoublePoint(amplitude);
		return (int)Math.round(translated);
	}

	private double translateAmplitudeToYAxisDoublePoint(double amplitude) {
		int area = graphYEnd - graphYStart;
		double percentage = (amplitude / 30.0) * 100;
		if (percentage < 0.0)
			percentage *= -1;
		return graphYStart + (area * (percentage * .01));
	}

	private double translateYAxisPointToAmplitude(int y) {
		int area = graphYEnd - graphYStart;
		int selectedPoint = y - graphYStart;
		double percentage = (double) selectedPoint / (double) area;
		double result = (percentage * 30) * (-1);
		// round result to nearest .5
		double f = 0.5;
		double rounded = f * Math.round(result / f);
		return rounded;
	}

	private void computeMajorXAxisFrequencyPoints() {
		graphXAvailableSpace = this.getWidth() - spaceForAmplitudeLabels - rightPadding;
		if (USE_CONTINUOUS_LOG_X_AXIS) {
			khzPoint_0 = (int) Math.round(translateFrequencyToXAxisDoublePointContinuous(0.0));
			khzPoint_100 = (int) Math.round(translateFrequencyToXAxisDoublePointContinuous(100.0));
			khzPoint_400 = (int) Math.round(translateFrequencyToXAxisDoublePointContinuous(400.0));
			khzPoint_1000 = (int) Math.round(translateFrequencyToXAxisDoublePointContinuous(1000.0));
			khzPoint_6000 = (int) Math.round(translateFrequencyToXAxisDoublePointContinuous(6000.0));
			khzPoint_20000 = (int) Math.round(translateFrequencyToXAxisDoublePointContinuous(20000.0));
			khzPoint_semifinal = (int) Math.round(translateFrequencyToXAxisDoublePointContinuous(21000.0));
			khzPoint_final = (int) Math.round(translateFrequencyToXAxisDoublePointContinuous(22050.0));
			return;
		}

		khzPoint_0 = graphXStart;
		khzPoint_100 = (int) ((graphXAvailableSpace * .2) + graphXStart);
		khzPoint_400 = (int) ((graphXAvailableSpace * .4) + graphXStart);
		khzPoint_1000 = (int) ((graphXAvailableSpace * .6) + graphXStart);
		khzPoint_6000 = (int) ((graphXAvailableSpace * .75) + graphXStart);
		khzPoint_20000 = (int) ((graphXAvailableSpace * .90) + graphXStart);
		khzPoint_semifinal = (int) ((graphXAvailableSpace * .95) + graphXStart);
		khzPoint_final = this.getWidth() - rightPadding;
	}

	private void drawFrequencyLines(Graphics2D g) {
		if (USE_CONTINUOUS_LOG_X_AXIS) {
			drawContinuousFrequencyLines(g);
			return;
		}

		String label = "";
		//draw major lines
		g.setPaint(Color.black);
		g.draw(new Line2D.Double(khzPoint_0, graphYStart, khzPoint_0, graphYEnd));
		g.draw(new Line2D.Double(khzPoint_100, graphYStart, khzPoint_100, graphYEnd));
		g.draw(new Line2D.Double(khzPoint_400, graphYStart, khzPoint_400, graphYEnd));
		g.draw(new Line2D.Double(khzPoint_1000, graphYStart, khzPoint_1000, graphYEnd));
		g.draw(new Line2D.Double(khzPoint_6000, graphYStart, khzPoint_6000, graphYEnd));
		g.draw(new Line2D.Double(khzPoint_20000, graphYStart, khzPoint_20000, graphYEnd));
		g.draw(new Line2D.Double(khzPoint_semifinal, graphYStart, khzPoint_semifinal, graphYEnd));
		g.draw(new Line2D.Double(khzPoint_final, graphYStart, khzPoint_final, graphYEnd));

		//draw lines between 0 and 100
		g.setPaint(Color.gray);
		int spacing = (khzPoint_100 - khzPoint_0) / 5;
		for (int counter = 1; counter <= 4; counter++) {
			g.draw(new Line2D.Double(khzPoint_0 + (spacing * counter), graphYStart, khzPoint_0 + (spacing * counter), graphYEnd));
			switch (counter) {
			case 1:
				label = "20Hz";
				break;
			case 2:
				label = "40Hz";
				break;
			case 3:
				label = "60Hz";
				break;
			case 4:
				label = "80Hz";
				break;
			}
			g.drawString(label, khzPoint_0 + (spacing * counter) - (metrics.stringWidth(label) / 2), graphYStart - metrics.getHeight());
		}

		//draw lines between 100 and 400
		spacing = (int) Math.round((khzPoint_400 - khzPoint_100) / 3.0);
		for (int counter = 1; counter <= 2; counter++) {
			g.draw(new Line2D.Double(khzPoint_100 + (spacing * counter), graphYStart, khzPoint_100 + (spacing * counter), graphYEnd));
			switch (counter) {
			case 1:
				label = "200Hz";
				break;
			case 2:
				label = "300Hz";
				break;
			}
			g.drawString(label, khzPoint_100 + (spacing * counter) - (metrics.stringWidth(label) / 2), graphYStart - metrics.getHeight());
		}

		//draw lines between 400 and 1000
		spacing = (int) Math.round((khzPoint_1000 - khzPoint_400) / 3.0);
		for (int counter = 1; counter <= 2; counter++) {
			g.draw(new Line2D.Double(khzPoint_400 + (spacing * counter), graphYStart, khzPoint_400 + (spacing * counter), graphYEnd));
			switch (counter) {
			case 1:
				label = "600Hz";
				break;
			case 2:
				label = "800Hz";
				break;
			}
			g.drawString(label, khzPoint_400 + (spacing * counter) - (metrics.stringWidth(label) / 2), graphYStart - metrics.getHeight());
		}

		//draw lines between 1000 and 6000
		spacing = (int) Math.round((khzPoint_6000 - khzPoint_1000) / 5.0);
		for (int counter = 1; counter <= 4; counter++) {
			g.draw(new Line2D.Double(khzPoint_1000 + (spacing * counter), graphYStart, khzPoint_1000 + (spacing * counter), graphYEnd));
		}

		//draw lines between 6000 and 20000
		spacing = (int) Math.round((khzPoint_20000 - khzPoint_6000) / 7.0);
		for (int counter = 1; counter <= 6; counter++) {
			g.draw(new Line2D.Double(khzPoint_6000 + (spacing * counter), graphYStart, khzPoint_6000 + (spacing * counter), graphYEnd));
		}
	}

	private void drawContinuousFrequencyLines(Graphics2D g) {
		double[] majorFrequencies = {0, 100, 400, 1000, 6000, 20000, 21000, 22050};
		double[] minorFrequencies = {20, 40, 60, 80, 200, 300, 600, 800, 2000, 3000, 4000, 5000, 8000, 10000, 12000, 14000, 16000, 18000};

		g.setPaint(Color.black);
		for (double frequency : majorFrequencies) {
			double x = translateFrequencyToXAxisDoublePoint(frequency);
			g.draw(new Line2D.Double(x, graphYStart, x, graphYEnd));
		}

		g.setPaint(Color.gray);
		for (double frequency : minorFrequencies) {
			double x = translateFrequencyToXAxisDoublePoint(frequency);
			g.draw(new Line2D.Double(x, graphYStart, x, graphYEnd));
		}

		Font originalFont = g.getFont();
		Font xAxisLabelFont = originalFont.deriveFont(Math.max(6.0f, originalFont.getSize2D() - X_AXIS_LABEL_FONT_DELTA));
		g.setFont(xAxisLabelFont);
		FontMetrics xAxisMetrics = g.getFontMetrics();

		String[] labeledMinorLabels = {"20Hz", "40Hz", "60Hz", "80Hz", "200Hz", "300Hz", "600Hz", "800Hz"};
		double[] labeledMinorFrequencies = {20, 40, 60, 80, 200, 300, 600, 800};
		for (int i = 0; i < labeledMinorFrequencies.length; i++) {
			double x = translateFrequencyToXAxisDoublePoint(labeledMinorFrequencies[i]);
			String label = labeledMinorLabels[i];
			g.drawString(label, (int) Math.round(x - (xAxisMetrics.stringWidth(label) / 2.0)), graphYStart - xAxisMetrics.getHeight());
		}

		g.setFont(originalFont);
	}

	private void drawMajorFrequencyLabels(Graphics2D g) {
		Font originalFont = g.getFont();
		Font xAxisLabelFont = originalFont.deriveFont(Math.max(6.0f, originalFont.getSize2D() - X_AXIS_LABEL_FONT_DELTA));
		g.setFont(xAxisLabelFont);
		FontMetrics xAxisMetrics = g.getFontMetrics();

		g.setPaint(Color.black);
		int yPoint = graphYStart - xAxisMetrics.getHeight();
		String label = "0Hz";
		g.drawString(label, khzPoint_0 - (xAxisMetrics.stringWidth(label) / 2), yPoint);
		label = "100Hz";
		g.drawString(label, khzPoint_100 - (xAxisMetrics.stringWidth(label) / 2), yPoint);
		label = "400Hz";
		g.drawString(label, khzPoint_400 - (xAxisMetrics.stringWidth(label) / 2), yPoint);
		label = "1kHz";
		g.drawString(label, khzPoint_1000 - (xAxisMetrics.stringWidth(label) / 2), yPoint);
		label = "6kHz";
		g.drawString(label, khzPoint_6000 - (xAxisMetrics.stringWidth(label) / 2), yPoint);
		label = "20kHz";
		g.drawString(label, khzPoint_20000 - (xAxisMetrics.stringWidth(label) / 2), yPoint);

		g.setFont(originalFont);
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

		// draw odd dB lines
		g.setPaint(Color.gray);
		for (int counter = 0; counter < amplitudeLabels.length - 1; counter++) {
			g.draw(new Line2D.Double(graphXStart, (spacing * counter) + start + (int) (spacing * .5), graphXEnd, (spacing * counter) + start + (int) (spacing * .5)));
		}
	}

	private void drawFrameBorder(Graphics2D g) {
		g.setPaint(Color.black);
		g.draw(new Line2D.Double(0, 0, this.getWidth() - 1, 0));
		g.draw(new Line2D.Double(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight() - 1));
		g.draw(new Line2D.Double(this.getWidth() - 1, this.getHeight() - 1, 0, this.getHeight() - 1));
		g.draw(new Line2D.Double(0, this.getHeight() - 1, 0, 0));
	}

	private void drawAmplitudeLabels(Graphics2D g) {
		g.setPaint(Color.black);
		int stringHeight = metrics.getHeight();
		int stringWidth;
		int availableHeight = this.getHeight() - topPadding - spaceForFrequencyLabels;
		int spacing = availableHeight / amplitudeLabels.length;
		int start = topPadding + (int) (stringHeight * 1.25) + spaceForFrequencyLabels;
		for (int counter = 0; counter < amplitudeLabels.length; counter++) {
			stringWidth = metrics.stringWidth(amplitudeLabels[counter]);
			g.drawString(amplitudeLabels[counter], spaceForAmplitudeLabels - stringWidth, (spacing * counter) + start);
		}
	}

	private void initializeAmplitudeLabels() {
		int currentAmplitude = 0;
		int counter = 0;
		do {
			amplitudeLabels[counter] = Integer.toString(currentAmplitude) + " dB";
			counter++;
			currentAmplitude -= 2;
		} while (currentAmplitude >= -30);
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		this.setSize(800, 600);
		this.setLayout(new GridBagLayout());
		this.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseExited(java.awt.event.MouseEvent e) {
				if (USE_HOVER_CLICK_READOUT) {
					mouseInGraph = false;
					currentInstance.repaint();
				}
			}

			public void mouseClicked(java.awt.event.MouseEvent e) {
				JFrame topFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, TargetDesigner.this);
				boolean cancelHighFrequencyPointCreation = false;
				int x1 = e.getX();
				int y1 = e.getY();
				// check to see if click is within the drawable graph
				if ((x1 >= graphXStart && x1 <= graphXEnd) && (y1 >= graphYStart && y1 <= graphYEnd)) {
					double amplitude = translateYAxisPointToAmplitude(y1);
					double frequency = translateXAxisPointToFrequency(x1);
					if (USE_HOVER_CLICK_READOUT) {
						lastClickFrequency = frequency;
						lastClickAmplitude = amplitude;
					}
					if (frequency > -1) {
						// check to see if there is already a nearby point - if not, draw the point
						int similarIndex = fap.findSimilarPoint(frequency, amplitude);
						if (similarIndex == -1) {
							if (SwingUtilities.isLeftMouseButton(e)) {
								fap.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(frequency, amplitude));
								currentInstance.repaint();
							}
						} else { // if there is a nearby point, delete it or edit it with right button press.
							if (SwingUtilities.isMiddleMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
								cancelHighFrequencyPointCreation = true;
								// create dialog box to edit point
								FrequencyAmplitudeDialog fad = new FrequencyAmplitudeDialog(topFrame, fap.getFrequencyAmplitudePoint(similarIndex + 1));
								FrequencyAmplitudePoint point = fad.run();
								if (point != null) {
									fap.editFrequencyAmplitudePoint(similarIndex, point.getFrequency(), point.getAmplitude());
									currentInstance.repaint();
								}
							} else if (SwingUtilities.isLeftMouseButton(e)) {
								fap.removePoint(similarIndex);
								currentInstance.repaint();
							}
						}
						// but if frequency is 21000 or 22050 draw if
						// after deleting it because we only want one of each of those
						if (!cancelHighFrequencyPointCreation && (frequency == 21000 || frequency == 22050)) {
							fap.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(frequency, amplitude));
							currentInstance.repaint();
						}
						options.setPoints(fap);
					}
				}
				if (USE_HOVER_CLICK_READOUT) {
					updateHoverReadout(e.getX(), e.getY());
					currentInstance.repaint();
				}
			}
		});

		this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			public void mouseMoved(java.awt.event.MouseEvent e) {
				if (USE_HOVER_CLICK_READOUT) {
					updateHoverReadout(e.getX(), e.getY());
					currentInstance.repaint();
				}
			}
		});
	}

	private void updateHoverReadout(int mouseX, int mouseY) {
		mouseInGraph = (mouseX >= graphXStart && mouseX <= graphXEnd) && (mouseY >= graphYStart && mouseY <= graphYEnd);
		if (!mouseInGraph) {
			hoverFrequency = -1;
			hoverAmplitude = 0;
			return;
		}

		hoverFrequency = translateXAxisPointToFrequency(mouseX);
		hoverAmplitude = translateYAxisPointToAmplitude(mouseY);
	}

	private static class SplineControlPoint {
		private final double frequency;
		private final double amplitude;
		private final double logFrequency;

		private SplineControlPoint(double frequency, double amplitude) {
			this.frequency = frequency;
			this.amplitude = amplitude;
			double adjustedFrequency = Math.max(LOG_SCALE_MIN_FREQUENCY, frequency);
			this.logFrequency = Math.log10(adjustedFrequency);
		}
	}

	private static class SplineSample {
		private final double logFrequency;
		private final double amplitude;

		private SplineSample(double logFrequency, double amplitude) {
			this.logFrequency = logFrequency;
			this.amplitude = amplitude;
		}
	}

	private static class ScreenPoint {
		private final double x;
		private final double y;

		private ScreenPoint(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}
