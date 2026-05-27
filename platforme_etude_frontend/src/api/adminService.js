import api from './axios';

// Users
export const getUsers = async () => (await api.get('/admin/users')).data;
export const getUser = async (id) => (await api.get(`/admin/users/${id}`)).data;
export const deleteUser = async (id) => (await api.delete(`/admin/users/${id}`)).data;

// Matieres
export const getAllMatieres = async () => (await api.get('/admin/matieres')).data;
export const deleteAdminMatiere = async (id) => (await api.delete(`/admin/matieres/${id}`)).data;

// Disponibilites
export const getAllDisponibilites = async () => (await api.get('/admin/disponibilites')).data;
export const deleteAdminDisponibilite = async (id) => (await api.delete(`/admin/disponibilites/${id}`)).data;

// Objectifs
export const getAllObjectifs = async () => (await api.get('/admin/objectifs')).data;
export const deleteAdminObjectif = async (id) => (await api.delete(`/admin/objectifs/${id}`)).data;

// Groupes
export const getAllGroupes = async () => (await api.get('/admin/groupes')).data;
export const deleteAdminGroupe = async (id) => (await api.delete(`/admin/groupes/${id}`)).data;

// Invitations
export const getAllInvitations = async () => (await api.get('/admin/invitations')).data;
export const deleteAdminInvitation = async (id) => (await api.delete(`/admin/invitations/${id}`)).data;

// Messages
export const getAllMessages = async () => (await api.get('/admin/messages')).data;
export const deleteAdminMessage = async (id) => (await api.delete(`/admin/messages/${id}`)).data;

// Notifications
export const getAllNotifications = async () => (await api.get('/admin/notifications')).data;
export const deleteAdminNotification = async (id) => (await api.delete(`/admin/notifications/${id}`)).data;

// Commentaires
export const getAllCommentaires = async () => (await api.get('/admin/commentaires')).data;
export const deleteAdminCommentaire = async (id) => (await api.delete(`/admin/commentaires/${id}`)).data;

// Sessions
export const getAllSessions = async () => (await api.get('/admin/sessions')).data;
export const deleteAdminSession = async (id) => (await api.delete(`/admin/sessions/${id}`)).data;

// User profile
export const getProfile = async () => (await api.get('/me')).data;
export const updateProfile = async (data) => (await api.put('/me', data)).data;
