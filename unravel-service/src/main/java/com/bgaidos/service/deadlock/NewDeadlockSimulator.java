package com.bgaidos.service.deadlock;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class NewDeadlockSimulator {

	private static final long LOCK_TIMEOUT_MS = 100;
	private static final long BACKOFF_MS = 20;
	private static final int MAX_RETRIES = 5;

	// NOTE: Can disable fairness for better performance if needed
	private final Lock lock1 = new ReentrantLock(true);
	private final Lock lock2 = new ReentrantLock(true);

	@SneakyThrows
	public void method1() {
		int retries = 0;

		while (retries < MAX_RETRIES) {
			retries++;

			if (lock1.tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
				Thread.sleep(20);
				try {
					if (lock2.tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
						try {
							log.debug("Method1: Acquired lock1 and lock2");
							return;
						} finally {
							lock2.unlock();
						}
					}
				} finally {
					lock1.unlock();
				}
			}

			// Backoff before retrying with jitter to reduce contention
			Thread.sleep(BACKOFF_MS + ThreadLocalRandom.current().nextInt(10));
		}
	}

	@SneakyThrows
	public void method2() {
		int retries = 0;

		while (retries < MAX_RETRIES) {
			retries++;

			if (lock1.tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
				Thread.sleep(20);
				try {
					if (lock2.tryLock(LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
						try {
							log.debug("Method2: Acquired lock2 and lock1");
							return;
						} finally {
							lock2.unlock();
						}
					}
				} finally {
					lock1.unlock();
				}
			}

			// Backoff before retrying with jitter to reduce contention
			Thread.sleep(BACKOFF_MS + ThreadLocalRandom.current().nextInt(10));
		}
	}
}
