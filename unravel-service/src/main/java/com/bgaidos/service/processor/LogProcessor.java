package com.bgaidos.service.processor;

import com.bgaidos.service.processor.model.LogEntry;

import java.util.concurrent.PriorityBlockingQueue;

public class LogProcessor {

	private final PriorityBlockingQueue<LogEntry> logQueue = new PriorityBlockingQueue<>();

	public void produceLog(LogEntry log) {
		logQueue.put(log);
	}

	public LogEntry consumeLog() throws InterruptedException {
		return logQueue.take();
	}

	public boolean isEmpty() {
		return logQueue.isEmpty();
	}
}
