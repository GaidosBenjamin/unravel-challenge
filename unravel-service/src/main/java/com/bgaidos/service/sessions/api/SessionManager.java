package com.bgaidos.service.sessions.api;

import com.bgaidos.api.RemoveDto;
import com.bgaidos.api.SessionDetailsDto;
import com.bgaidos.exceptions.SessionConflictException;
import com.bgaidos.exceptions.SessionNotFoundException;

public interface SessionManager {

	/**
	 * Creates a new session for the given user ID
	 * If user already has a session it refreshes the session expiration.
	 *
	 * @param userId The ID of the user to create a session for.
	 * @return A MessageDTO containing the result of the login operation.
	 * @throws SessionConflictException if a session already exists for the given user ID.
	 */
	SessionDetailsDto saveSession(String userId) throws SessionConflictException;

	/**
	 * Terminates the session for the given user ID.
	 *
	 * @param userId The ID of the user to terminate the session for.
	 * @return A MessageDTO containing the result of the logout operation.
	 */
	RemoveDto removeSession(String userId);

	/**
	 * Retrieves the session details for the given user ID, and refreshes the session expiration.
	 *
	 * @param userId The ID of the user to retrieve session details for.
	 * @return A SessionDetailsDTO containing the session information.
	 * @throws SessionNotFoundException if no session is found for the given user ID.
	 */
	SessionDetailsDto getSessionDetails(String userId) throws SessionNotFoundException;
}
