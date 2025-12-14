package com.bgaidos.controller;

import com.bgaidos.data.DatabaseManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DatabaseController {

	private final DatabaseManager databaseManager;

	@PostMapping("/database/stress-test")
	public void stressTestDatabase() {
		databaseManager.stressTestConnection();
	}
}
