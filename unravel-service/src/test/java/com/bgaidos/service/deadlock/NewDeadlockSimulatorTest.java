package com.bgaidos.service.deadlock;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NewDeadlockSimulatorTest {

	@Test
	@Timeout(5) // Abort test if it runs longer than 5 seconds
	@DisplayName("1.0 - New Deadlock Simulation Test")
	public void testDeadlockSimulation() throws InterruptedException{
		var simulator = new NewDeadlockSimulator();
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
		assertFalse(running.isPresent(), "Deadlock should not occur!");

		runningFutures.forEach(future -> future.cancel(true));
		executorService.shutdownNow();
	}
}
