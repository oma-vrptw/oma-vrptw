package com.mdvrp;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class DrawPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Instance instance;
	public Route[][] routes;
	public Cost currentCost;
	public Cost feasibleCost;
	public Cost bestCost;
	public int iterations;
	public int bestIndex;
	public int feasibleIndex;
	public boolean pause;
	public double alpha;		// α
	public double beta;			// β
	public double gamma;		// γ
	private Font myFont = new Font("Lucida Sans Typewriter Regular", Font.PLAIN, 12);
	
	public DrawPanel(Instance instance) {	                     // set up graphics window
		super();
		this.instance = instance;
		this.iterations = 0;
		this.bestIndex = 0;
		pause = false;
		//this.possitiveCoord = true;
		this.feasibleCost = new Cost();
		this.feasibleCost.total = Double.POSITIVE_INFINITY;
		this.bestCost = new Cost();
		this.bestCost.total = Double.POSITIVE_INFINITY;
		setBackground(Color.WHITE);
	}

	public void paintComponent(Graphics g2d) { // draw graphics in the panel
		int m = 30; 					// distance from the margin of the panel
		int nW = getWidth() - 2*m;      // width of window in pixels - the margin from the borders
		int nH = getHeight() - 4*m;		// height of window in pixels - the margin from the borders
		int xOffset = m; // used to keep distance from the border
		int yOffset = 3*m; // used to keep distance from the border
		double nX = nW/(instance.maxX - instance.minX); // normalize to the window dimentions
		double nY = nH/(instance.maxY - instance.minY); 
		
		super.paintComponent(g2d);            // call superclass to make panel display correctly
		Graphics2D g = (Graphics2D) g2d;
		g.setFont(myFont);
		g.drawString(String.format("%4s t %2d p %1.0e  Total  |  Travel  |  CapViol |  DurViol |   TWViol | It Found | start client: %3d", instance.getParameters().getInputFileName(), instance.getParameters().getTabuTenure(),instance.getParameters().getPrecision(), instance.getParameters().getStartClient()), 10, 10);
		g.drawString(String.format("Feasible Cost: %10.2f | %8.2f | %8.2f | %8.2f | %8.2f | %8d |", feasibleCost.total, feasibleCost.travelTime, feasibleCost.loadViol, feasibleCost.durationViol, feasibleCost.twViol, feasibleIndex), 10, 25);
		g.drawString(String.format("Best Cost:     %10.2f | %8.2f | %8.2f | %8.2f | %8.2f | %8d |", bestCost.total, bestCost.travelTime, bestCost.loadViol, bestCost.durationViol, bestCost.twViol, bestIndex), 10, 40);
		g.drawString(String.format("Current Cost:  %10.2f | %8.2f | %8.2f | %8.2f | %8.2f | %8d |", currentCost.total, currentCost.travelTime, currentCost.loadViol, currentCost.durationViol, currentCost.twViol, iterations), 10, 55);
		g.drawString(String.format("alpha, beta, gamma                   | %8.5f | %8.5f | %8.5f |", alpha, beta, gamma), 10, 70);


		for (int i = 0; i < routes.length; ++i) {
			for (int j = 0; j < routes[i].length; ++j) {
				// for each route set a different color
				Route route = routes[i][j];					
				// for each depot get it's coordinates and draw it's circle
				int dx1 = (int)(nX * (route.getDepot().getXCoordinate() - instance.minX) + xOffset);
				int dy1 = (int)(nY * (route.getDepot().getYCoordinate() - instance.minY) + yOffset);
				g.setColor(Color.BLACK);
				drawCircleValue(g, dx1, dy1, 15, route.getDepotNr());
				int x1 = dx1;
				int y1 = dy1;
				// set the color for the route
				g.setColor(getColor(i*instance.getVehiclesNr() + j));
				
				for (int k = 0; k < route.getCustomersLength(); ++k) {
					// for each customer get it's coordinates and draw it's circle
					int x2 = (int)(nX * (route.getCustomer(k).getXCoordinate() - instance.minX) + xOffset);
					int y2 = (int)(nY * (route.getCustomer(k).getYCoordinate() - instance.minY) + yOffset);
					g.setColor(getColor(i*instance.getVehiclesNr() + j));
					drawCircleValue(g, x2, y2, 15, route.getCustomer(k).getNumber());
					// draw the line between the customer and the one before or depot
					g.drawLine(x1, y1, x2, y2);
					x1 = x2;
					y1 = y2;
					}
				// draw the line between the last customer and the depot
				g.drawLine(x1, y1, dx1, dy1);
				}// end for vehicles
			}// end for depots
		if(true){//instance.getParameters().isDebug()){
			if(!pause){
		    	synchronized(this){
		    		this.notify();
		    	}
			}
		}
	}
	
	public Color getColor(int k) {
		switch (k) {
		case 0:
			return new Color(0,0,205);    // Blue
		case 1:
			return new Color(64,224,208); // Turquoise
		case 2:
			return new Color(128,128,0 ); //olive
		case 3:
			return new Color(255,0,255 ); // Fuchsia
		case 4: 
			return new Color(0,128,128 ); // Teal
		case 5:
			return new Color(250,205,0 ); // Yellow
		case 6:
			return new Color(220,20,60 ); // Cimson
		case 7:
			return new Color(154,205,50); // YellowGreen
		case 8:
			return new Color(138,43,226); // blueViolet
		case 9:
			return new Color(128,0,0 );   // Maroon
		default:
			return Color.black;
		}
	}
	
	/**
	 * Function to draw a circle with center coordinates and radius
	 * @param g
	 * @param xCenter
	 * @param yCenter
	 * @param r
	 */
	public void drawCircle(Graphics g, int xCenter, int yCenter, int r) {
		g.drawOval(xCenter-r, yCenter-r, r*2, r*2);
	}
	
	/**
	 * Function which draw a circle based on center coordinates
	 * and radius, having inside a value
	 * @param g
	 * @param xCenter
	 * @param yCenter
	 * @param r
	 * @param value
	 */
	public void drawCircleValue(Graphics g, int xCenter, int yCenter, int r, int value) {
		Color origColor = g.getColor();
		g.drawOval((int)xCenter-r, (int)yCenter-r, r*2, r*2);
		g.setColor(Color.BLACK);
		g.drawString(Integer.toString(value), (int)xCenter-r/2, (int)yCenter+r/3);
		g.setColor(origColor);
	}

	public void initializeGraphics(DrawPanel panel) {
		//DrawPanel panel = new DrawPanel();                          // window for drawing
		JFrame application = new JFrame();                            // the program itself
//		ImageIcon leftButtonIcon = createImageIcon("images/right.gif");
		panel.setLayout(null);
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // set frame to exit
																	  // when it is closed
		application.add(panel);           


		application.setSize(760, 760);         // window is x pixels wide, y high
		application.setVisible(true);          // set the graphichs visible
	}
}
