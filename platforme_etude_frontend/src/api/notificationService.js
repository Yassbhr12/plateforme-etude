import api from './axios';

export const getNotifications = async () => {
  const res = await api.get('/me/notifications');
  return res.data;
};

export const getNotification = async (id) => {
  const res = await api.get(`/me/notifications/${id}`);
  return res.data;
};

export const markAsRead = async (id) => {
  const res = await api.patch(`/me/notifications/${id}/read`);
  return res.data;
};

export const updateNotification = async (id, data) => {
  const res = await api.put(`/me/notifications/${id}`, data);
  return res.data;
};

export const deleteNotification = async (id) => {
  const res = await api.delete(`/me/notifications/${id}`);
  return res.data;
};
