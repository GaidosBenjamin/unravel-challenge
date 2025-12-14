package com.bgaidos.exceptions;

public class SessionConflictException extends RuntimeException {

	public SessionConflictException(String message) {
		super(message);
	}
}
