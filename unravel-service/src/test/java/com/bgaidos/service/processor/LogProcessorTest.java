package com.bgaidos.service.processor;

import com.bgaidos.service.processor.model.LogEntry;
import com.bgaidos.service.processor.model.Priority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.DisplayName.class)
public class LogProcessorTest {

    @Test
    @DisplayName("1.0 - Basic Produce and Consume Test")
    public void testBasicProduceAndConsume() throws InterruptedException {
        var logProcessor = new LogProcessor();
        var logEntry = LogEntry.builder()
                .offset(1)
                .message("Test log")
                .priority(Priority.MEDIUM)
                .creationTime(Instant.now())
                .build();

        logProcessor.produceLog(logEntry);
        var consumedLog = logProcessor.consumeLog();

        assertEquals(logEntry, consumedLog);
    }

    @Test
    @DisplayName("2.0 - Producer Functionality Test")
    public void testProducerFunctionality() throws InterruptedException {
        var logProcessor = new LogProcessor();
        var producer = new Producer(logProcessor, 10);

        var executorService = Executors.newSingleThreadExecutor();
        try {
            var producerFuture = executorService.submit(producer);

            var consumedLogs = new ArrayList<LogEntry>();
            for (int i = 0; i < 10; i++) {
                consumedLogs.add(logProcessor.consumeLog());
            }

            assertFalse(consumedLogs.isEmpty());
            assertEquals(10, consumedLogs.size());

            boolean hasLowPriority = consumedLogs.stream().anyMatch(log -> log.priority() == Priority.LOW);
            boolean hasMediumPriority = consumedLogs.stream().anyMatch(log -> log.priority() == Priority.MEDIUM);
            boolean hasHighPriority = consumedLogs.stream().anyMatch(log -> log.priority() == Priority.HIGH);
            boolean hasCriticalPriority = consumedLogs.stream().anyMatch(log -> log.priority() == Priority.CRITICAL);

            assertTrue(hasLowPriority);
            assertTrue(hasMediumPriority);
            assertTrue(hasHighPriority);
            assertTrue(hasCriticalPriority);
            producerFuture.cancel(true);
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    @DisplayName("2.1 - Priority Functionality Test")
    public void testPriorityFunctionality() throws InterruptedException, ExecutionException {
        // Given
        LogProcessor logProcessor = new LogProcessor();

        var producer = new Producer(logProcessor, 10);

        var executorService = Executors.newSingleThreadExecutor();
        try {
            var producerFuture = executorService.submit(producer);
            producerFuture.get();

            var consumedLogs = new ArrayList<LogEntry>();
            for (int i = 0; i < 10; i++) {
                var consumedLogsEntry = logProcessor.consumeLog();
                consumedLogs.add(consumedLogsEntry);
            }

            assertEquals(Priority.CRITICAL, consumedLogs.getFirst().priority());
            assertEquals(Priority.LOW, consumedLogs.getLast().priority());

            producerFuture.cancel(true);
		} finally {
            executorService.shutdownNow();
        }
    }


    @Test
    @DisplayName("3.0 - Consumer Functionality Test")
    public void testConsumerFunctionality() {
        var logProcessor = new LogProcessor();
        var consumer = new Consumer(logProcessor);

        for (int i = 0; i < 5; i++) {
            var logEntry = LogEntry.builder()
                    .offset(i)
                    .message("Test log " + i)
                    .priority(Priority.MEDIUM)
                    .creationTime(Instant.now())
                    .build();
            logProcessor.produceLog(logEntry);
        }

        var executorService = Executors.newSingleThreadExecutor();

        assertFalse(logProcessor.isEmpty(), "LogProcessor should not be empty before consuming logs");
        var consumerFuture = executorService.submit(consumer);

        await().atMost(2, TimeUnit.SECONDS)
            .until(logProcessor::isEmpty);

        assertTrue(logProcessor.isEmpty(), "LogProcessor should be empty after consuming all logs");

        executorService.shutdownNow();
        consumerFuture.cancel(true);

    }

    @Test
    @DisplayName("4.0 - LogEntry Functionality Test")
    public void testLogEntryFunctionality() {
        var now = Instant.now();
        var logEntry1 = LogEntry.builder()
                .offset(1)
                .message("Test log 1")
                .priority(Priority.MEDIUM)
                .creationTime(now)
                .build();
        var logEntry2 = LogEntry.builder()
                .offset(2)
                .message("Test log 2")
                .priority(Priority.MEDIUM)
                .creationTime(now)
                .build();

        assertNotEquals(logEntry1, logEntry2);
        // Since they have the same creation time and priority, they should be ordered by offset
        assertTrue(logEntry1.compareTo(logEntry2) < 0);
    }

    @Test
    @Timeout(10) // Abort if the test takes longer than 10 seconds
    @DisplayName("5.0 - Multiple Producers and Consumers Test")
    public void testMultipleProducersAndConsumers() throws InterruptedException, ExecutionException {
        var logProcessor = new LogProcessor();
        int numProducers = 3;
        int numConsumers = 2;
        int logsPerProducer = 10;

        var latch = new CountDownLatch(numProducers * logsPerProducer);

        var producerExecutor = Executors.newFixedThreadPool(numProducers);
        var producerFutures = new ArrayList<Future<?>>();

        for (int i = 0; i < numProducers; i++) {
            final int producerId = i;
            producerFutures.add(producerExecutor.submit(() -> {
                for (int j = 0; j < logsPerProducer; j++) {
                    var logEntry = LogEntry.builder()
                            .offset(producerId * 1000 + j)
                            .message("Producer " + producerId + " log " + j)
                            .priority(Priority.values()[j % Priority.values().length])
                            .creationTime(Instant.now())
                            .build();
                    logProcessor.produceLog(logEntry);
                }
            }));
        }

        var consumerExecutor = Executors.newFixedThreadPool(numConsumers);
        var consumerFutures = new ArrayList<Future<?>>();

        for (int i = 0; i < numConsumers; i++) {
            consumerFutures.add(consumerExecutor.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        LogEntry logEntry = logProcessor.consumeLog();
                        if (logEntry != null) {
                            latch.countDown();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }));
        }

        for (Future<?> future : producerFutures) {
            future.get();
        }

        boolean allConsumed = latch.await(3, TimeUnit.SECONDS);
        assertTrue(allConsumed, "Not all logs were consumed within the timeout period");

        producerExecutor.shutdownNow();
        consumerExecutor.shutdownNow();

        for (Future<?> future : consumerFutures) {
            future.cancel(true);
        }
    }
}
