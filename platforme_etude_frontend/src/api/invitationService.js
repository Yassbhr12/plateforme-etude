import api from './axios';

export const getReceivedInvitations = async () => {
  const res = await api.get('/me/invitations');
  return res.data;
};

export const getSentInvitations = async () => {
  const res = await api.get('/me/invitations/sent');
  return res.data;
};

export const createInvitation = async (data) => {
  const res = await api.post('/me/invitations', data);
  return res.data;
};

export const acceptInvitation = async (id) => {
  const res = await api.patch(`/me/invitations/${id}/accept`);
  return res.data;
};

export const refuseInvitation = async (id) => {
  const res = await api.patch(`/me/invitations/${id}/refuse`);
  return res.data;
};

export const cancelInvitation = async (id) => {
  const res = await api.patch(`/me/invitations/${id}/cancel`);
  return res.data;
};
