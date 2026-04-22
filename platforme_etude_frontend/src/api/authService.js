import api from './axios';

/**
 * Register a new user.
 * POST /api/auth/register
 * Body: { user: { nom, prenom, role }, auth: { email, password } }
 */
export const register = async (userData) => {
  const payload = {
    user: {
      nom: userData.nom,
      prenom: userData.prenom,
      role: 'ROLE_USER',
    },
    auth: {
      email: userData.email,
      password: userData.password,
    },
  };
  const response = await api.post('/auth/register', payload);
  return response.data;
};

/**
 * Login — Step 1: Send credentials, receive 2FA code by email.
 * POST /api/auth/login
 * Body: { email, password }
 */
export const login = async (email, password) => {
  const response = await api.post('/auth/login', { email, password });
  return response.data;
};

/**
 * Login — Step 2: Validate 2FA code, receive tokens.
 * POST /api/auth/login/validation
 * Body: { email, validationCode }
 */
export const validateCode = async (email, validationCode) => {
  const response = await api.post('/auth/login/validation', {
    email,
    validationCode,
  });
  return response.data;
};

/**
 * Refresh access token.
 * POST /api/auth/refresh
 * Body: { refreshToken }
 */
export const refreshToken = async (token) => {
  const response = await api.post('/auth/refresh', {
    refreshToken: token,
  });
  return response.data;
};

/**
 * Logout — revoke all user tokens.
 * POST /api/auth/logout
 * Body: { refreshToken }
 */
export const logout = async () => {
  const token = localStorage.getItem('refreshToken');
  if (token) {
    await api.post('/auth/logout', { refreshToken: token });
  }
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
};
