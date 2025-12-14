package com.bgaidos.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.Executors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DatabaseManager {

	private final DataSource dataSource;

	public void stressTestConnection() {
		var executorService = Executors.newFixedThreadPool(200);
		for (int i = 0; i < 10000; i++) {
			executorService.submit(() -> {
				try (var connection = dataSource.getConnection()) {
					connection.prepareStatement("SELECT SLEEP(0.5)").execute();
				} catch (SQLException e) {
					log.error("Connection acquisition failed: {}", e.getMessage());
				}
			});
		}
		executorService.shutdown();
	}
}
