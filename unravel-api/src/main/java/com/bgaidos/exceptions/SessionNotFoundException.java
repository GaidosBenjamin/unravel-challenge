package com.bgaidos.exceptions;

public class SessionNotFoundException extends RuntimeException {

	public SessionNotFoundException(String message) {
		super(message);
	}
}
