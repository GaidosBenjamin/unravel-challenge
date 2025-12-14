package com.bgaidos.service.processor.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Builder
public record LogEntry(
	long offset,
	String message,
	Priority priority,
	Instant creationTime
) implements Comparable<LogEntry> {

	/**
	 * Calculates the effective priority of the log entry based on its base priority and waiting time.
	 *
	 * @return the effective priority value
	 */
	public long effectivePriority() {
		long waitingSeconds = Instant.now().getEpochSecond() - creationTime.getEpochSecond();
		return priority.priorityLevel + waitingSeconds;
	}

	@Override
	public int compareTo(LogEntry other) {
		// Higher priority is smaller value
		var priorityDifference = Long.compare(other.effectivePriority(), this.effectivePriority());

		// If priorities are equal, compare by offset to maintain FIFO order
		if (priorityDifference == 0) {
			return Long.compare(this.offset, other.offset);
		}

		return priorityDifference;
	}
}