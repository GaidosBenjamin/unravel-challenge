package com.bgaidos.service.sessions.stress;

import com.bgaidos.service.sessions.redis.OldMemoryManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
public class OldMemoryManagerStressTest {

	@Test
	@DisplayName("1.0 - Stress Test for Session Store, for heap investigation")
	public void stressTest() {
		int counter = 0;
		try {
			while (true) {
				OldMemoryManager.addSessionData(UUID.randomUUID().toString());
				counter++;
			}
		} catch (OutOfMemoryError oom) {
			System.out.println("OOM occurred after adding: " + counter + " entries");
			assertTrue(counter > 0, "Stress test should allocate data until OOM");
		}
	}
}
