package com.bgaidos.service.deadlock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeadlockSimulatorTest {

	@Test
	@Timeout(5) // Abort test if it runs longer than 5 seconds
	@DisplayName("1.0 - Deadlock Simulation Test")
	public void testDeadlockSimulation() throws InterruptedException {
		var simulator = new DeadlockSimulator();
		var executorService = Executors.newFixedThreadPool(4);
		var runningFutures = new ArrayList<Future<?>>();
		for (int i = 0; i < 2; i++) {
			runningFutures.add(executorService.submit(simulator::method1));
			runningFutures.add(executorService.submit(simulator::method2));
		}
		Thread.sleep(1000);

		var running = runningFutures.stream()
			.filter(future -> !future.isDone())
			.findAny();
		assertTrue(running.isPresent(), "Deadlock should occur with at least one running thread");

		runningFutures.forEach(future -> future.cancel(true));
		executorService.shutdownNow();
	}
}
