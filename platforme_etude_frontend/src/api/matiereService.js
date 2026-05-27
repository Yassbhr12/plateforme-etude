import api from './axios';

export const getMatieres = async () => {
  const res = await api.get('/me/matieres');
  return res.data;
};

export const createMatiere = async (data) => {
  const res = await api.post('/me/matieres', data);
  return res.data;
};

export const updateMatiere = async (id, data) => {
  const res = await api.put(`/me/matieres/${id}`, data);
  return res.data;
};

export const deleteMatiere = async (id) => {
  const res = await api.delete(`/me/matieres/${id}`);
  return res.data;
};
