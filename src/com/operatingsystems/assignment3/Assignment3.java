package com.operatingsystems.assignment3;

import java.util.ArrayList;
import java.util.List;

public class Assignment3 {
	
	public final static int HAIR_CUT_TIME = 2000;
	
//	public static class BarberShopMonitor {
//		//two barbers
//		//ten chairs for people waiting (queue)
//		
//		//customer enters and goes to chair if one is empty, otherwise sit in waiting
//		
//		//if all chairs are full, customers leaves
//		
//		//when a haircut is finished, customer leaves, next person in line moves to the barber's chair
//		
//		//suspend execution of the calling process on condition c. the monitor is now available for use by another process

	//for debugging
	static int idCount = 1;
	
	public static class Customer {
		public int id;
		
		public Customer(int id) {
			this.id = id;
		}
	}
	
	public static class Barber extends Thread {
		int count = 0;
		
		@Override
		public synchronized void run() {
			while(true) {
				count++;
				if(count > 10000000) {
					try {
						System.out.println("barber is asleep now");
						count = 0;
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		public boolean isWaiting() {
			return this.getState().equals(Thread.State.WAITING);
		}
		
		public boolean isNew() {
			return this.getState().equals(Thread.State.NEW);
		}
	}
	
	public static class BarberShopMonitor extends Thread {
		public Barber barber1 = new Barber();
		public Barber barber2 = new Barber();
		
		List<Customer> customers = new ArrayList<Customer>();
		
		@Override 
		public synchronized void run() {
			while(true) {
				try {
					handleCustomers();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void handleCustomers() throws InterruptedException {
			if(barber1.isWaiting() || barber2.isWaiting()) {
				
				if(customers.size() > 0) {
					Customer customer = null;
					while(customer == null) {
						customer = customers.remove(0);
					}

					//wake barber 1 if hes not busy
					if(barber1.isWaiting()) {
						System.out.println("customer number: " + customer.id + " got up from waiting and got a haircut from barber1");
						wakeFirstBarber(customer);
					} else if(barber2.isWaiting()) {
						System.out.println("customer number: " + customer.id + " got up from waiting and got a haircut from barber2");
						wakeSecondBarber(customer);
					}
					
				}
				
			}
			
			if(customers.size() < 10) {
				if(customerMonitor.getState() == Thread.State.WAITING) {
					synchronized(customerMonitor) {
						System.out.println("waking up customer monitor");
						customerMonitor.notifyAll();
					}
				}
			}
		}
		
		public void customerEnter(Customer customer) throws InterruptedException {
			System.out.println("Customer has entered the shop");
			
			if(customers.size() >= 10) {
				//both barbers are already busy and the queue is already full
				System.out.println("customer number: " + customer.id + " should leave. putting customer monitor to sleep");
				customerMonitor.wait();
			} else if(!barber1.isWaiting() && !barber2.isWaiting()) {
				//both barbers are busy but we can sit them in a chair
				System.out.println("customer number: " + customer.id + " is waiting for a haircut");
				customers.add(customer);
			} else if(barber1.isWaiting()) {
				System.out.println("customer number: " + customer.id + " walked right in and got a haircut from barber1");
				wakeFirstBarber(customer);
			} else if(barber2.isWaiting()) {
				System.out.println("customer number: " + customer.id + " walked right in and got a haircut from barber2");
				wakeSecondBarber(customer);
			}
				
		}
		
		public void wakeFirstBarber(Customer customer) {
			//wake first barber because he's not doing anything
			
			if(barber1.isWaiting()) {
				synchronized(barber1) {
					System.out.println("waking barber1");
					barber1.notify();
				}
			}
		}
		
		public void wakeSecondBarber(Customer customer) {
			//wake first barber because he's not doing anything
			
			if(barber2.isWaiting()) {
				synchronized(barber2) {
					System.out.println("waking barber2");
					barber2.notify();
				}
			}
		}
	}
	
	public static class CustomerMonitor extends Thread {
		int count = 0;
		
		@Override
		public synchronized void run() {
			while(true) {
				try {
					handleNewCustomer();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		public void handleNewCustomer() throws InterruptedException {
			barberShopMonitor.customerEnter(new Customer(idCount++));
		}
	}
	
	static BarberShopMonitor barberShopMonitor;
	static CustomerMonitor customerMonitor;
	
	public static void main(String[] args) {
		//Create a monitor to manage a barbershop.  
		//Assume we have two barbers, and ten chairs for people waiting for a haircut.  
		//When a customer enters the shop, they immediately sit in a barber's chair if one is empty, otherwise they sit in one of the waiting chairs. 
		//If all the chairs are full, the customer leaves.  
		//When a haircut is finished, the customer leaves and the person who has been waiting the longest then moves to the barber's chair and gets their haircut.
		
		// a monitor is a software module ocnsisitng of one or more pocedures, an init sequence, and local data
		
		//1. the local data vraibles are accessible only by the monitor's procedures and not by any exxternal procedure
		//2. a process enters themonitor by invoking one of its procedures
		//3. only one process may be executing in the monitor at a time; any other processes that have invoked the monitor are blocked, waiting for the monitor to become available
		
		//randomly put customers into the store
		
		barberShopMonitor = new BarberShopMonitor();
		customerMonitor = new CustomerMonitor();
		
		barberShopMonitor.start();
		customerMonitor.start();
		
		barberShopMonitor.barber1.start();
		barberShopMonitor.barber2.start();
	}

}