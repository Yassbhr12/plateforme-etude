import api from './axios';

export const getGroupeMessages = async (groupeId) => {
  const res = await api.get(`/groupes/${groupeId}/messages`);
  return res.data;
};

export const sendGroupeMessage = async (groupeId, data) => {
  const res = await api.post(`/groupes/${groupeId}/messages`, data);
  return res.data;
};

export const getMyMessages = async () => {
  const res = await api.get('/me/messages');
  return res.data;
};

export const updateMessage = async (id, data) => {
  const res = await api.put(`/messages/${id}`, data);
  return res.data;
};

export const deleteMessage = async (id) => {
  const res = await api.delete(`/messages/${id}`);
  return res.data;
};
