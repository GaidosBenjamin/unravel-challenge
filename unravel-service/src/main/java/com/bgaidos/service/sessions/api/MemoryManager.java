package com.bgaidos.service.sessions.api;

import com.bgaidos.exceptions.SessionNotFoundException;

public interface MemoryManager {

	/**
	 * Adds session data to the memory store.
	 *
	 * @param sessionId The unique identifier for the session.
	 * @param data The session data to be stored as a byte array.
	 */
	void addSessionData(String sessionId, byte[] data);

	/**
	 * Removes session data from the memory store.
	 *
	 * @param sessionId The unique identifier for the session to be removed.
	 */
	void removeSessionData(String sessionId);

	/**
	 * Retrieves session data from the memory store.
	 *
	 * @param sessionId The unique identifier for the session to be retrieved.
	 * @return The session data as a byte array, or null if not found.
	 */
	byte[] getSessionData(String sessionId) throws SessionNotFoundException;
}
