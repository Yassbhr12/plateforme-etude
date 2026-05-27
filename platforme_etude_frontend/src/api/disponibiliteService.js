import api from './axios';

export const getDisponibilites = async () => {
  const res = await api.get('/me/disponibilites');
  return res.data;
};

export const createDisponibilite = async (data) => {
  const res = await api.post('/me/disponibilites', data);
  return res.data;
};

export const updateDisponibilite = async (id, data) => {
  const res = await api.put(`/me/disponibilites/${id}`, data);
  return res.data;
};

export const deleteDisponibilite = async (id) => {
  const res = await api.delete(`/me/disponibilites/${id}`);
  return res.data;
};
