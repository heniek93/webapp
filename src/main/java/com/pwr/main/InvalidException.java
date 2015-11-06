package com.pwr.main;

public class InvalidException extends Exception {

	private static final long serialVersionUID = -4896676045767962953L;

	public InvalidException() {
		super();
	}
	
	public InvalidException(String message) {
		super(message);
	}
}
