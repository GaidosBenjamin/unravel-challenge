package com.bgaidos.controller.handler;

import com.bgaidos.controller.handler.model.ExceptionResponse;
import com.bgaidos.exceptions.SessionConflictException;
import com.bgaidos.exceptions.SessionNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {

	@ExceptionHandler(SessionNotFoundException.class)
	public ResponseEntity<ExceptionResponse> handleSessionNotFoundException(SessionNotFoundException ex) {
		var response = new ExceptionResponse(
			"SESSION_NOT_FOUND",
			ex.getMessage(),
			HttpStatus.NOT_FOUND.value()
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(SessionConflictException.class)
	public ResponseEntity<ExceptionResponse> handleSessionConflictException(SessionConflictException ex) {
		var response = new ExceptionResponse(
			"SESSION_CONFLICT",
			ex.getMessage(),
			HttpStatus.CONFLICT.value()
		);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponse> handleGeneralException(Exception ex) {
		log.error("Unexpected exception occurred", ex);
		var response = new ExceptionResponse(
			"INTERNAL_ERROR",
			"An unexpected error occurred",
			HttpStatus.INTERNAL_SERVER_ERROR.value()
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
