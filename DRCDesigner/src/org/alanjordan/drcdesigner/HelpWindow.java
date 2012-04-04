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


import javax.swing.event.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.*;
import java.net.*;
import java.awt.event.*;
import java.awt.*;

public class HelpWindow extends JFrame implements ActionListener {

 	private static final long serialVersionUID = 1L;
	private final int WIDTH = 750;
    private final int HEIGHT = 500;
    private JEditorPane editorpane;
    private URL helpURL;

   public HelpWindow(String title, URL hlpURL) {
    	super(title);
        setIconImage(Toolkit.getDefaultToolkit().getImage(HelpWindow.class.getResource("music_green.png")));
    	helpURL = hlpURL;
    	editorpane = new JEditorPane();
    	editorpane.setEditable(false);
    	try {
    		editorpane.setPage(helpURL);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}

    	editorpane.addHyperlinkListener(new HyperlinkListener() {

    		public void hyperlinkUpdate(HyperlinkEvent ev) {
    			if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
    				JEditorPane pane = (JEditorPane) ev.getSource();
    				if (ev instanceof HTMLFrameHyperlinkEvent) {
    					HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)ev;
    					HTMLDocument doc = (HTMLDocument)pane.getDocument();
    					doc.processHTMLFrameHyperlinkEvent(evt);
    				} else {
    					try {
    						pane.setPage(ev.getURL());
    					} catch (Throwable t) {
    						t.printStackTrace();
    					}
    				}
    			}
    		}
    	});

    	getContentPane().add(new JScrollPane(editorpane));
    	addButtons();
    	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    	calculateLocation();
    	setVisible(true);
    }


    public void actionPerformed(ActionEvent e) {
        String strAction = e.getActionCommand();
        try {
            if (strAction.equals("Close")) {
                processWindowEvent(new WindowEvent(this,
                        WindowEvent.WINDOW_CLOSING));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addButtons() {
        JButton btnclose = new JButton("Close");
        btnclose.addActionListener(this);
        JPanel panebuttons = new JPanel();
        panebuttons.add(btnclose);
        getContentPane().add(panebuttons, BorderLayout.SOUTH);
    }


    private void calculateLocation() {
        Dimension screendim = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(new Dimension(WIDTH, HEIGHT));
        int locationx = (screendim.width - WIDTH) / 2;
        int locationy = (screendim.height - HEIGHT) / 2;
        setLocation(locationx, locationy);
    }
}
