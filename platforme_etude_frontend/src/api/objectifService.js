import api from './axios';

export const getObjectifs = async () => {
  const res = await api.get('/me/objectifs');
  return res.data;
};

export const getObjectifsByWeek = async (date) => {
  const params = date ? { date } : {};
  const res = await api.get('/me/objectifs/week', { params });
  return res.data;
};

export const getObjectifsByMatiere = async (matiereId) => {
  const res = await api.get(`/me/matieres/${matiereId}/objectifs`);
  return res.data;
};

export const createObjectif = async (data) => {
  const res = await api.post('/me/objectifs', data);
  return res.data;
};

export const updateObjectif = async (id, data) => {
  const res = await api.put(`/me/objectifs/${id}`, data);
  return res.data;
};

export const deleteObjectif = async (id) => {
  const res = await api.delete(`/me/objectifs/${id}`);
  return res.data;
};
