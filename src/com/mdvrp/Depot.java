package com.mdvrp;

import java.util.ArrayList;

/**
 * Depot class stores information about one depot which implements the Vertex inferface.
 * It stores the number of the depot, it's capacity, coordinates, it's working time(time windows)
 * @author Banea George
 *
 */
public class Depot {
	private int number;
	private double xCoordinate;
	private double yCoordinate;
	private double capacity;                       // capacity that the depot can support
	private int startTw;                           // beginning of time window (earliest time for start of service),if any
	private int endTw;                             // end of time window (latest time for start of service), if any
	private ArrayList<Customer> assignedCustomers; // the list of customers that are assigned to this depot
	
	
	public Depot() {
		this.startTw           = 0;
		this.endTw             = 0;
		this.assignedCustomers = new ArrayList<>();
	}
	
	/**
	 * Return the formated string of the depot
	 */
	public String toString() {
		StringBuffer print = new StringBuffer();
		print.append("\n");
		print.append("\n" + "--- Depot " + number + " -------------------------------------");
		print.append("\n" + "| x=" + xCoordinate + " y=" + yCoordinate);
		print.append("\n" + "| Capacity=" + capacity);
		print.append("\n" + "| StartTimeWindow=" + startTw + " EndTimeWindow=" + endTw);
		print.append("\n" + "| AssignedCustomers: ");
		for (int i = 0; i < assignedCustomers.size(); ++i) {
			print.append(assignedCustomers.get(i).getNumber() + " ");
		}
		print.append("\n" + "--------------------------------------------------");
		return print.toString();	
	}
	
	/**
	 * 
	 * @return the list of assigned customers to depot in a string
	 */
	public String printAssignedCustomers() {
		StringBuffer print = new StringBuffer();
		print.append("\n" + "AssignedCustomers=");
		for(Customer customer : assignedCustomers) {
			print.append(" " + customer.getNumber());
		}
		print.append("\n");
		return print.toString();
	}
	
	/**
	 * 
	 * @param index
	 * @return the formated string with the angles of assigned customers to depot
	 */
	public String printAssignedCustomersAngles(int index) {
		StringBuffer print = new StringBuffer();
		print.append("\nDepot[" + index + "]---AssignedCustomers-------------\nCustomerNumber\t\tCustomerAngle\n");
		for(Customer customer : assignedCustomers) {
			print.append("\t" + customer.getNumber() + "\t\t\t\t" + customer.getAngleToDepot(index) + "\n");
		}
		print.append("---------------------------------------------------\n");
		return print.toString();
	}
	
	/**
	 * 
	 * @return the number of assigned customers
	 */
	public int getAssignedCustomersNr() {
		return assignedCustomers.size();
	}
	
	/**
	 * @return the capacity
	 */
	public double getCapacity() {
		return capacity;
	}
	/**
	 * @param capacity the capacity to set
	 */
	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}
	/**
	 * @return the xcoordinate
	 */
	public double getXCoordinate() {
		return xCoordinate;
	}
	/**
	 * @param xcoordinate the xcoordinate to set
	 */
	public void setXCoordinate(double xcoordinate) {
		this.xCoordinate = xcoordinate;
	}
	/**
	 * @return the ycoordinate
	 */
	public double getYCoordinate() {
		return yCoordinate;
	}
	/**
	 * @param ycoordinate the ycoordinate to set
	 */
	public void setYCoordinate(double ycoordinate) {
		this.yCoordinate = ycoordinate;
	}

	/**
	 * @return the number of depot
	 */
	public int getNumber() {
		return this.number;
	}

	/**
	 * Assign the number to the depot
	 * @param depotnumber
	 */
	public void setNumber(int depotNumber) {
		this.number = depotNumber;
	}

	/**
	 * Get the start time window
	 * @return the startTw
	 */
	public int getStartTw() {
		return startTw;
	}

	/**
	 * Set the start time window
	 * @param startTW the startTW to set
	 */
	public void setStartTW(int startTW) {
		this.startTw = startTW;
	}

	/**
	 * Get the end time window
	 * @return the endTw
	 */
	public int getEndTw() {
		return endTw;
	}

	/**
	 * Set the end time window
	 * @param endTw
	 */
	public void setEndTW(int endTw) {
		this.endTw = endTw;
	}

	/**
	 * Add customer to assigned customers
	 * @param customer
	 */
	public void addAssignedCustomer(Customer customer) {
		assignedCustomers.add(customer);
	}
	
	/**
	 * Get the assigned customer at index
	 * @param index
	 * @return Customer
	 */
	public Customer getAssignedCustomer(int index) {
		return assignedCustomers.get(index);
	}
	
	/**
	 * Get the list of assigned customers
	 * @return ArrayList<Customer>
	 */
	public ArrayList<Customer> getAssignedcustomers() {
		return assignedCustomers;
	}

	/**
	 * Set the assigned customer list
	 * @param assignedCustomers
	 */
	public void setAssignedcustomers(ArrayList<Customer> assignedCustomers) {
		this.assignedCustomers = assignedCustomers;
	}


}
