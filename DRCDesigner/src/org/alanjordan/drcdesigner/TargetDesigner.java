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
import java.awt.Toolkit;
import java.awt.geom.Line2D;

import javax.swing.*;

public class TargetDesigner extends JPanel {

	private static final long serialVersionUID = 1L;
	private FrequencyAmplitudePoints fap;// = new Targets().getTarget(Targets.TargetNames.Flat);
	private String[] amplitudeLabels = new String[16];
	private int topPadding = 10; // top and bottom blank space will be half of padding
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
	private int khzPoint_0; //start of graphX percent
	private int khzPoint_100; //20% point of graphX
	private int khzPoint_400; //40% of graphX
	private int khzPoint_1000; //60% of graphX
	private int khzPoint_6000; //75% of graphX
	private int khzPoint_20000; //90% of graphX
	private int khzPoint_semifinal; //95% of graphX - this will be halfway between 20000 and final point below  
	private int khzPoint_final; //100% of graphX - this will change depending on the sampling frequency 
	private Options options;
	/**
	 * This is the default constructor
	 */
	public TargetDesigner(Options options) {
		super();
		this.options = options;
		fap = null;
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
	    int fontSize = (int)Math.round(7.0 * screenRes / 72.0);
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
		
		if (fap != null) {
			drawFrequencyAmplitudePoints(g2);
		}
	}
	
	
	
	private void drawPoint(Graphics2D g, int x, int y) {
		int radius = 5;
		g.drawOval(x - radius, y - radius, 2*radius, 2*radius);
	}

	
	
	private void drawFrequencyAmplitudePoints(Graphics2D g) {
	    g.setStroke(new BasicStroke(2F));  // set stroke width of 2
		int counter = 1;
		double x1;
		double x2;
		double y1;
		double y2;
		do {
			x1 = translateFrequencyToXAxisPoint(fap.getFrequencyAmplitudePoint(counter).getFrequency());
			y1 = translateAmplitudeToYAxisPoint(fap.getFrequencyAmplitudePoint(counter).getAmplitude());
			x2 = translateFrequencyToXAxisPoint(fap.getFrequencyAmplitudePoint(counter + 1).getFrequency());
			y2 = translateAmplitudeToYAxisPoint(fap.getFrequencyAmplitudePoint(counter + 1).getAmplitude());
			g.setColor(Color.blue);
			drawPoint(g,(int)x1, (int)y1);
			g.setPaint(Color.red);
			g.draw(new Line2D.Double(x1, y1, x2, y2));
			counter+=1;
		} while (counter < fap.getNumberOfFrequencyDataPoints() -2 );
		
		// draw from third to last point to semi-final point
		x1 = translateFrequencyToXAxisPoint(fap.getFrequencyAmplitudePoint(counter).getFrequency());
		y1 = translateAmplitudeToYAxisPoint(fap.getFrequencyAmplitudePoint(counter).getAmplitude());
		g.setColor(Color.blue);
		drawPoint(g,(int)x1, (int)y1);
		x2 = khzPoint_semifinal;
		y2 = translateAmplitudeToYAxisPoint(fap.getFrequencyAmplitudePoint(counter + 1).getAmplitude());
//		g.setColor(Color.blue);
		drawPoint(g,(int)x2, (int)y2);
		g.setPaint(Color.red);
		g.draw(new Line2D.Double(x1, y1, x2, y2));
		
		
		// draw from semi-final point final point
		counter+=1;
		x1 = khzPoint_semifinal;
		y1 = translateAmplitudeToYAxisPoint(fap.getFrequencyAmplitudePoint(counter).getAmplitude());
		x2 = khzPoint_final;
		y2 = translateAmplitudeToYAxisPoint(fap.getFrequencyAmplitudePoint(counter + 1).getAmplitude());
		g.draw(new Line2D.Double(x1, y1, x2, y2));
		g.setColor(Color.blue);
		drawPoint(g,(int)x2, (int)y2);
		
	}
	
	private int translateFrequencyToXAxisPoint(double frequency) {
		int area = 0;
		int percentage = 0;
		if (frequency >= 0 && frequency <= 100) {
			area = khzPoint_100 - khzPoint_0;
			percentage = (int)Math.round(frequency);
			return khzPoint_0 + (int)Math.round((area * (percentage * .01)));
		}
		if (frequency >= 101 && frequency <= 400) {
			area = khzPoint_400 - khzPoint_100;
			percentage = (int)Math.round(((frequency - 100) / 300)*100);
			return khzPoint_100 + (int)Math.round((area * (percentage * .01)));
		}
		if (frequency >= 400 && frequency <= 1000) {
			area = khzPoint_1000 - khzPoint_400;
			percentage = (int)Math.round(((frequency - 400) / 600)*100);
			return khzPoint_400 + (int)Math.round((area * (percentage * .01)));
		}
		if (frequency >= 1001 && frequency <= 6000) {
			area = khzPoint_6000 - khzPoint_1000;
			percentage = (int)Math.round(((frequency - 1000) / 5000)*100);
			return khzPoint_1000 + (int)Math.round((area * (percentage * .01)));
		}
		if (frequency >= 6001 && frequency <= 20000) {
			area = khzPoint_20000 - khzPoint_6000;
			percentage = (int)Math.round(((frequency - 6000) / 14000)*100);
			return khzPoint_6000 + (int)Math.round((area * (percentage * .01)));
		}
		if (frequency >= 20001 && frequency <= 21000) {
			area = khzPoint_semifinal - khzPoint_20000;
			percentage = (int)Math.round(((frequency - 20000) / 1000)*100);
			return khzPoint_20000 + (int)Math.round((area * (percentage * .01)));
		}
		else { //bad request
			return -1;
		}		
	}

	
	private double translateXAxisPointToFrequency(int x) {
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

		// if greater than 20k just allow editing of the semifinal line and final line
		if (x > khzPoint_20000 && x <= khzPoint_semifinal) {
			return 21000;
		}
//		if (x > khzPoint_20000 && x <= khzPoint_semifinal) {
//			area = khzPoint_semifinal - khzPoint_20000;
//			int selectedPoint = x - khzPoint_20000;
//			percentage = (double)selectedPoint / (double)area;
//			return (double)Math.round(percentage * (21000 - 20000) + 20000);
//		}

		if (x > khzPoint_semifinal && x <= khzPoint_final) {
			return 22050;
		}
		
		else { //bad request
			return -1;
		}		
	}
	
	
	private int translateAmplitudeToYAxisPoint(double amplitude) {
		int area = graphYEnd - graphYStart;
		double percentage = (amplitude / 30.0) * 100 ;
		if (percentage < 0.0)
			percentage*= -1;
		return graphYStart + (int)Math.round((area * (percentage *.01)));
	}
	
	private double translateYAxisPointToAmplitude(int y) {
		int area = graphYEnd - graphYStart;
		int selectedPoint = y - graphYStart;
		double percentage = (double)selectedPoint / (double)area;
		double result = (percentage * 30) * (-1);
		//round result to nearest .5
		double f = 0.5;
		double rounded = f * Math.round(result/f);
		return rounded;
	}

	
	private void computeMajorXAxisFrequencyPoints() {
		graphXAvailableSpace = this.getWidth() - spaceForAmplitudeLabels - rightPadding;
		khzPoint_0 = graphXStart;
		khzPoint_100 = (int) ((graphXAvailableSpace * .2) + graphXStart); //20%
		khzPoint_400 = (int) ((graphXAvailableSpace * .4) + graphXStart); //40%
		khzPoint_1000 = (int) ((graphXAvailableSpace * .6) + graphXStart); //60%
		khzPoint_6000 = (int) ((graphXAvailableSpace * .75) + graphXStart); //75%
		khzPoint_20000 = (int) ((graphXAvailableSpace * .90) + graphXStart); //90%
		khzPoint_semifinal = (int) ((graphXAvailableSpace * .95) + graphXStart); //95%  
		khzPoint_final = this.getWidth() - rightPadding; 
	}

	
	private void drawFrequencyLines(Graphics2D g) {
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
		for (int counter = 1; counter <=4 ; counter++) {
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

		//draw lines between 0 and 100
		spacing = (int) Math.round((khzPoint_400 - khzPoint_100) / 3.0); 
		for (int counter = 1; counter <=2 ; counter++) {
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
		for (int counter = 1; counter <=2 ; counter++) {
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
		for (int counter = 1; counter <=4 ; counter++) {
			g.draw(new Line2D.Double(khzPoint_1000 + (spacing * counter), graphYStart, khzPoint_1000 + (spacing * counter), graphYEnd));			
		}

		//draw lines between 6000 and 20000
		spacing = (int) Math.round((khzPoint_20000 - khzPoint_6000) / 7.0); 
		for (int counter = 1; counter <=6 ; counter++) {
			g.draw(new Line2D.Double(khzPoint_6000 + (spacing * counter), graphYStart, khzPoint_6000 + (spacing * counter), graphYEnd));			
		}
}

	private void drawMajorFrequencyLabels(Graphics2D g) {
		g.setPaint(Color.black);
		int yPoint = graphYStart - metrics.getHeight();
		String label = "0Hz";
		g.drawString(label, khzPoint_0 - (metrics.stringWidth(label) / 2), yPoint);
		label = "100Hz";
		g.drawString(label, khzPoint_100 - (metrics.stringWidth(label) / 2), yPoint);
		label = "400Hz";
		g.drawString(label, khzPoint_400 - (metrics.stringWidth(label) / 2), yPoint);
		label = "1kHz";
		g.drawString(label, khzPoint_1000 - (metrics.stringWidth(label) / 2), yPoint);
		label = "6kHz";
		g.drawString(label, khzPoint_6000 - (metrics.stringWidth(label) / 2), yPoint);
		label = "20kHz";
		g.drawString(label, khzPoint_20000 - (metrics.stringWidth(label) / 2), yPoint);
		
	}
	
	
	private void drawAmplitudeLines(Graphics2D g) {
		int availableHeight = this.getHeight() - topPadding - spaceForFrequencyLabels;
		int spacing = availableHeight / amplitudeLabels.length;
		int start = topPadding + metrics.getHeight() + spaceForFrequencyLabels;
		graphYStart = start;
	    graphYEnd = (spacing * (amplitudeLabels.length -1)) + start;

		g.setPaint(Color.black);
		
		for (int counter = 0; counter < amplitudeLabels.length; counter++) {
			g.draw(new Line2D.Double(graphXStart,(spacing * counter) + start, graphXEnd,(spacing * counter) + start));
		}
		
		//draw odd dB lines
		g.setPaint(Color.gray);
		for (int counter = 0; counter < amplitudeLabels.length - 1; counter ++) {
			g.draw(new Line2D.Double(graphXStart,(spacing * counter) + start + (int)(spacing * .5), graphXEnd,(spacing * counter) + start + (int)(spacing * .5)));			
		}
		
	}

	private void drawFrameBorder(Graphics2D g) {
		g.setPaint(Color.black);
		g.draw(new Line2D.Double(0, 0, this.getWidth() - 1, 0));
		g.draw(new Line2D.Double(this.getWidth() - 1 , 0, this.getWidth() -1 , this.getHeight() -1));
		g.draw(new Line2D.Double(this.getWidth() - 1 , this.getHeight() -1, 0 , this.getHeight() -1));
		g.draw(new Line2D.Double(0, this.getHeight() -1, 0 , 0));
	}

	private void drawAmplitudeLabels(Graphics2D g) {
		g.setPaint(Color.black);
		int stringHeight = metrics.getHeight();
		int stringWidth;
		int availableHeight = this.getHeight() - topPadding - spaceForFrequencyLabels;
		int spacing = availableHeight / amplitudeLabels.length;
		int start = topPadding + (int)(stringHeight *1.25 ) + spaceForFrequencyLabels;
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
			public void mouseClicked(java.awt.event.MouseEvent e) {
				JFrame topFrame = (JFrame)SwingUtilities.getAncestorOfClass(JFrame.class, TargetDesigner.this);
                boolean cancelHighFrequencyPointCreation = false;
				int x1 = e.getX();
				int y1 = e.getY();
				// check to see if click is within the drawable graph 
				if ((x1  >= graphXStart && x1 <= graphXEnd) && (y1  >= graphYStart && y1 <= graphYEnd)) {
					double amplitude = translateYAxisPointToAmplitude(y1);
					double frequency = translateXAxisPointToFrequency(x1);
					if (frequency > -1) {
						//check to see if there is already a nearby point - if not, draw the point
						int similarIndex = fap.findSimilarPoint(frequency,amplitude);
						if (similarIndex == -1) {
							fap.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(frequency,amplitude));
							currentInstance.repaint();
						}
						else { // if there is a nearby point, delete it or edit it off middle button.
							if (SwingUtilities.isMiddleMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
                                cancelHighFrequencyPointCreation = true;
								// create dialog box to edit point
								FrequencyAmplitudeDialog fad = new FrequencyAmplitudeDialog(topFrame, fap.getFrequencyAmplitudePoint(similarIndex + 1));
								FrequencyAmplitudePoint point = fad.run();
								if (point != null ) {
									fap.editFrequencyAmplitudePoint(similarIndex, point.getFrequency(), point.getAmplitude());
									currentInstance.repaint();
								}
							}
							else {
								fap.removePoint(similarIndex);
								currentInstance.repaint();
							}
						}
						// but if frequency is 21000 or 22050 draw if
						// after deleting it because we only want one of each of those
						if (!cancelHighFrequencyPointCreation && (frequency == 21000 || frequency == 22050)) {
							fap.addFrequencyAmplitudePoint(new FrequencyAmplitudePoint(frequency,amplitude));
							currentInstance.repaint();							
						}
						options.setPoints(fap);
					}
				}
			}
		});
	}

}
