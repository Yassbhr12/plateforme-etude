import api from './axios';

export const getCommentairesBySession = async (sessionId) => {
  const res = await api.get(`/sessions/${sessionId}/commentaires`);
  return res.data;
};

export const getMyCommentaires = async () => {
  const res = await api.get('/me/commentaires');
  return res.data;
};

export const createCommentaire = async (data) => {
  const res = await api.post('/commentaires', data);
  return res.data;
};

export const updateCommentaire = async (id, data) => {
  const res = await api.put(`/commentaires/${id}`, data);
  return res.data;
};

export const deleteCommentaire = async (id) => {
  const res = await api.delete(`/commentaires/${id}`);
  return res.data;
};
