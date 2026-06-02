import api from './axios';

/**
 * Get the current user's profile.
 * GET /api/me
 */
export const getProfile = async () => (await api.get('/me')).data;

/**
 * Update the current user's profile.
 * PUT /api/me
 * Body: { nom, prenom }
 */
export const updateProfile = async (data) => (await api.put('/me', data)).data;
