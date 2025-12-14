package com.bgaidos.service.processor.model;

public enum Priority {
	CRITICAL(100),
	HIGH(30),
	MEDIUM(10),
	LOW(1);

	final int priorityLevel;

	Priority(int priorityLevel) {
		this.priorityLevel = priorityLevel;
	}
}
