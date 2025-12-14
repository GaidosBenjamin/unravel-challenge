package com.bgaidos.service.processor;

import com.bgaidos.service.processor.model.LogEntry;
import com.bgaidos.service.processor.model.Priority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
class Producer implements Runnable {

	private final LogProcessor processor;
	private final int logsToProduce;

	@Override
	public void run() {
		var priorities = List.of(Priority.LOW, Priority.MEDIUM, Priority.HIGH, Priority.CRITICAL);
		for (int i = 0; i < logsToProduce; i++) {
			var logEntry = LogEntry.builder()
				.offset(i)
				.message("Log " + i)
				.priority(priorities.get(i % 4))
				.creationTime(Instant.now())
				.build();
			processor.produceLog(logEntry);
			log.debug("Produced Log {}", logEntry.toString());
		}
	}
}
