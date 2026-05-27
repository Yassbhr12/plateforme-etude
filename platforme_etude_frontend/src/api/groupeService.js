import api from './axios';

export const getMyAdminGroupes = async () => {
  const res = await api.get('/me/groupes/admin');
  return res.data;
};

export const createGroupe = async (data) => {
  const res = await api.post('/me/groupes', data);
  return res.data;
};

export const updateGroupe = async (id, data) => {
  const res = await api.put(`/me/groupes/${id}`, data);
  return res.data;
};

export const deleteGroupe = async (id) => {
  const res = await api.delete(`/me/groupes/${id}`);
  return res.data;
};
