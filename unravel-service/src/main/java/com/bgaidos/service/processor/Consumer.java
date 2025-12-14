package com.bgaidos.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class Consumer implements Runnable {

	private final LogProcessor processor;

	@Override
	public void run() {
		try {
			while (true) {
				var logEntry = processor.consumeLog();
				log.debug("Consumed: Log {}", logEntry.toString());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}