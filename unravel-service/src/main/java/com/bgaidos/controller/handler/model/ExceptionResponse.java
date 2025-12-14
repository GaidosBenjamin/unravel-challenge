package com.bgaidos.controller.handler.model;

public record ExceptionResponse(
	String error,
	String message,
	int status
) {
}
