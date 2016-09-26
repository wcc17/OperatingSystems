package com.operatingsystems.assignment2;

import java.util.Scanner;

public class Assignment2 {

	private static int billion = 1000000000;
	
//	Create a multithreaded program where one thread responds to the user and takes what the user types and 
//	displays it in uppercase, while the second thread determines the number of prime numbers less than 1 billion.
	
	public static void main(String[] args) {
		Thread primeThread = new Thread() {
			public void run() {
				findPrimeNumbersUntilLimit(billion);
			}
		};
		
		Thread inputThread = new Thread() {
			public void run() {
				capitalizeUserInput();
			}
		};
		
		//the run thread we wrote will be called soon in start()
		primeThread.start();
		inputThread.start();
	}
	
	static void capitalizeUserInput() {
		Scanner in = new Scanner(System.in);
		System.out.println("Type exit to quit program");
		
		while(true) {
			String input = in.nextLine();
			
			if(("exit").equals(input)) {
				break;
			}
			
			System.out.println(input.toUpperCase());
		}
		
		in.close();
	}
	
	static void findPrimeNumbersUntilLimit(int limit) {
		int count = 0;
		
		for(int i = 2; i < limit; i++) {
			if(checkPrime(i)) {
				count++;
			}
		}
		
		System.out.println("Number of prime numbers less than 1 billion: " + limit + count);
	}
	
	//return true if prime
	static boolean checkPrime(int x) {
		//first check if x is multiple of 2. if so, it will not be prime
		//except for the number 2, its prime so we dont want to return false for it
		if(x % 2 == 0 && x != 2) {
			return false;
		}
		
		//since no multiples of 2 are primes (other than 2), we only need to check odd numbers now
		for(int i = 3; i*i <= x; i+=2) {
			if( x % i == 0) {
				return false;
			}
		}
		
		return true;
	}
}
