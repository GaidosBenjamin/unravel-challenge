package com.bgaidos.service.deadlock;

import lombok.SneakyThrows;

public class DeadlockSimulator {
	private final Object lock1 = new Object();
	private final Object lock2 = new Object();

	@SneakyThrows
	public void method1() {
		synchronized (lock1) {
			Thread.sleep(10);
			synchronized (lock2) {
				System.out.println("Method1: Acquired lock1 and lock2");
			}
		}
	}

	@SneakyThrows
	public void method2() {
		synchronized (lock2) {
			Thread.sleep(10);
			synchronized (lock1) {
				System.out.println("Method2: Acquired lock2 and lock1");
			}
		}
	}
}