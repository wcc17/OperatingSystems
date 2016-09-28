package com.operatingsystems.assignment3;

import java.util.ArrayList;
import java.util.List;

public class Assignment3 {
	
	//for debugging customers
	static int idCount = 1;
	
	//id is mostly meaningless, but makes it easier to keep up with order of customers and handling a customer in general
	public static class Customer {
		public int id;
		
		public Customer(int id) {
			this.id = id;
		}
	}
	
	public static class Barber extends Thread {
		public int id;
		public boolean isBusy = true;
		int count = 0;
		
		@Override
		public synchronized void run() {
			while(true) {
				count++;
				//creating a delay for haircuts without using Thread.sleep or writing logic for a timer
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
		
		//convience method for checking if barber is currently waiting
		public boolean isWaiting() {
			return this.getState().equals(Thread.State.WAITING);
		}
		
		//convenience method for checking if barber hasn't been started yet
		public boolean isNew() {
			return this.getState().equals(Thread.State.NEW);
		}
	}
	
	public static class BarberShopMonitor extends Thread {
		public Barber barber1 = new Barber();
		public Barber barber2 = new Barber();
		
		List<Customer> customers = new ArrayList<Customer>();
		
		
		int count = 0;
		
		@Override 
		public synchronized void run() {
			barber1.id = 1;
			barber2.id = 2;
			
			while(true) {
				try {
					//constantly handling customers or putting customerMonitor or barbers to sleep
					handleCustomers();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void handleCustomers() throws InterruptedException {
			
			//if iether barber isn't doing anything right now
			if(barber1.isWaiting() || barber2.isWaiting()) {
				if(customers.size() > 0) {
					
					Customer customer = null;
					while(customer == null) {
						//NOTE: customer being null here was really weird behavior that would only happen after like 1000000 customers had been processed
						//I could never really figure out why, so I just made sure it wouldn't be null here. This isn't a good or long lasting solution but it works for now
						
						
						//pull the customer thats been waiting the longest from the list
						customer = customers.remove(0);
					}

					//only one barber at a time can be assigned a customer
					if(barber1.isWaiting() || barber1.isNew()) {
						//wake barber 1 if hes not busy and give him a customer to work on
						System.out.println("customer number: " + customer.id + " got up from waiting and got a haircut from barber1");
						wakeBarber(barber1, customer);
					} else if(barber2.isWaiting() || barber2.isNew()) {
						//wake barber 2 if hes not busy and give him a customer to work on
						System.out.println("customer number: " + customer.id + " got up from waiting and got a haircut from barber2");
						wakeBarber(barber2, customer);
					}
					
				}
				
			}
			
			//if we have at least one seat open in the shop, wake the customer monitor if it was asleep to start bringing in more customers
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
			} else {
				if ((barber1.isBusy && !barber1.isNew()) && (barber2.isBusy && !barber2.isNew())) {
					//both barbers are busy but we can sit the customer in a chair
					System.out.println("customer number: " + customer.id + " is waiting for a haircut");
					customers.add(customer);
				} else if(customers.size() < 1) {
					//if there are no customers waiting: 
					if(barber1.isWaiting() || barber1.isNew()) {
						//nobody was waiting so a customer walked right in
						System.out.println("customer number: " + customer.id + " walked right in and got a haircut from barber1");
						wakeBarber(barber1, customer);
					} else if(barber2.isWaiting() || barber2.isNew()) {
						//nobody was waiting so a customer walked right in
						System.out.println("customer number: " + customer.id + " walked right in and got a haircut from barber2");
						wakeBarber(barber2, customer);
					}
				}
			}
				
		}
		
		public void wakeBarber(Barber barber, Customer customer) {
			//wake barber because he's not doing anything
			if(barber.isWaiting()) {
				synchronized(barber) {
					System.out.println("waking barber " + barber.id + " for customer number " + customer.id);
					barber.isBusy = true;
					barber.notify();
				}
			} else if(barber.isNew()){
				barber.start();
			}
		}
	}
	
	//this only exists to throw customers at the barber shop whenever its awake
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
		barberShopMonitor = new BarberShopMonitor();
		customerMonitor = new CustomerMonitor();
		
		//start the barber shop monitor and the process that generates customers for the barber shop
		barberShopMonitor.start();
		customerMonitor.start();
	}

}