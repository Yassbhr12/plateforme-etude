import api from './axios';

export const getSessions = async () => {
  const res = await api.get('/me/sessions');
  return res.data;
};

export const getSessionsByWeek = async (date) => {
  const params = date ? { date } : {};
  const res = await api.get('/me/sessions/week', { params });
  return res.data;
};

export const getSessionsByDay = async (date) => {
  const res = await api.get('/me/sessions/day', { params: { date } });
  return res.data;
};

export const createSession = async (data) => {
  const res = await api.post('/me/sessions', data);
  return res.data;
};

export const updateSession = async (id, data) => {
  const res = await api.put(`/me/sessions/${id}`, data);
  return res.data;
};

export const cancelSession = async (id) => {
  const res = await api.patch(`/me/sessions/${id}/cancel`);
  return res.data;
};

export const markSessionDone = async (id) => {
  const res = await api.patch(`/me/sessions/${id}/done`);
  return res.data;
};

export const shareSession = async (id, groupeEtudeId) => {
  const res = await api.patch(`/me/sessions/${id}/share`, null, { params: { groupeEtudeId } });
  return res.data;
};

export const regenerateWeeklyPlan = async (date) => {
  const params = date ? { date } : {};
  const res = await api.post('/me/sessions/week/regenerate', null, { params });
  return res.data;
};

export const deleteSession = async (id) => {
  const res = await api.delete(`/me/sessions/${id}`);
  return res.data;
};
