package com.bgaidos.controller;

import com.bgaidos.api.RemoveDto;
import com.bgaidos.api.SessionDetailsDto;
import com.bgaidos.service.sessions.api.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dummy/sessions")
public class SessionController {

	private final SessionManager sessionManager;

	@PostMapping("/{userId}")
	public SessionDetailsDto addSession(@PathVariable String userId) {
		//TODO: Remove sessionID from response body, add as a cookie or header
		return sessionManager.saveSession(userId);
	}

	@GetMapping("/{userId}")
	public SessionDetailsDto getSessionDetails(@PathVariable String userId) {
		//TODO: Remove sessionID from response body, add as a cookie or header
		return sessionManager.getSessionDetails(userId);
	}

	@DeleteMapping("/{userId}")
	public RemoveDto removeSession(@PathVariable String userId) {
		//TODO: Remove sessionID from response body, add as a cookie or header
		return sessionManager.removeSession(userId);
	}
}
