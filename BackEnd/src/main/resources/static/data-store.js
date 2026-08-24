/**
 * InfoLink Data Store
 * Centralized API communication and session management for the InfoLink frontend.
 * All pages should use these functions for API calls and session state.
 */

const InfoLinkStore = (() => {

  const SESSION_KEY  = 'infolink_session_meta';
  const REQUESTS_KEY = 'infolink_access_requests';

  // Try to detect client IP once per session
  let _clientIP = sessionStorage.getItem('infolink_client_ip') || '';
  if (!_clientIP) {
    fetch('https://api.ipify.org?format=json')
      .then(r => r.json())
      .then(d => { _clientIP = d.ip; sessionStorage.setItem('infolink_client_ip', _clientIP); })
      .catch(() => { _clientIP = 'N/A'; });
  }

  // ─── ACCESS REQUESTS (localStorage for pending requests) ───
  function getRequests() {
    return JSON.parse(localStorage.getItem(REQUESTS_KEY) || '[]');
  }

  function saveRequests(requests) {
    localStorage.setItem(REQUESTS_KEY, JSON.stringify(requests));
  }

  function addRequest(req) {
    const requests = getRequests();
    req.id        = Date.now();
    req.status    = 'pending';
    req.createdAt = new Date().toISOString();
    req.resolvedAt = null;
    requests.unshift(req);
    saveRequests(requests);
    return req;
  }

  function resolveRequest(id, action) {
    // action: 'approved' | 'denied'
    const requests = getRequests();
    const r = requests.find(r => r.id === id);
    if (!r) return;
    r.status     = action;
    r.resolvedAt = new Date().toISOString();
    saveRequests(requests);
  }

  function getPendingRequestCount() {
    return getRequests().filter(r => r.status === 'pending').length;
  }

  // ─── SESSION ───
  function getCurrentUser() {
    const groupsRaw = sessionStorage.getItem('infolink_groups') || '';
    const groups = groupsRaw ? groupsRaw.split(',').filter(Boolean) : [];
    return {
      username: sessionStorage.getItem('infolink_user') || '',
      fullname: sessionStorage.getItem('infolink_fullname') || '',
      role:     sessionStorage.getItem('infolink_role') || '',
      group:    sessionStorage.getItem('infolink_group') || '',
      groupID:  sessionStorage.getItem('infolink_group_id') || '',
      groups,
    };
  }

  function setSession(user) {
    const normalizedRole = (user.role || '').toLowerCase();
    const role = normalizedRole === 'admin' || normalizedRole === 'sysadmin' ? normalizedRole : 'user';
    sessionStorage.setItem('infolink_role',      role);
    sessionStorage.setItem('infolink_user',      user.username);
    sessionStorage.setItem('infolink_fullname',  user.name || user.fullname || user.username);
    sessionStorage.setItem('infolink_group',     user.group || '');
    if (user.groupID != null) sessionStorage.setItem('infolink_group_id', user.groupID);
    // Support multi-group: store comma-separated list
    const groups = Array.isArray(user.groups) ? user.groups : (user.group ? [user.group] : []);
    sessionStorage.setItem('infolink_groups',    groups.join(','));
    // Record login time
    sessionStorage.setItem('infolink_login_time', new Date().toISOString());
  }

  function getLoginTime() {
    const t = sessionStorage.getItem('infolink_login_time');
    if (!t) return '—';
    const d = new Date(t);
    return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`;
  }

  function clearSession() {
    sessionStorage.clear();
  }

  function isLoggedIn() {
    return !!sessionStorage.getItem('infolink_role');
  }

  function isAdmin() {
    const role = sessionStorage.getItem('infolink_role');
    return role === 'admin' || role === 'sysadmin';
  }

  // ─── API COMMUNICATION ───
  async function apiFetch(path, options = {}, retry = true) {
    const headers = new Headers(options.headers || {});
    const accessToken = sessionStorage.getItem('infolink_access_token');
    if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`);
    if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
    const response = await fetch(path, { ...options, headers });
    const refreshToken = sessionStorage.getItem('infolink_refresh_token');
    if (response.status === 401 && retry && refreshToken) {
      const refreshResponse = await fetch('/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken })
      });
      if (refreshResponse.ok) {
        const tokens = await refreshResponse.json();
        sessionStorage.setItem('infolink_access_token', tokens.accessToken);
        sessionStorage.setItem('infolink_refresh_token', tokens.refreshToken);
        return apiFetch(path, options, false);
      }
    }
    return response;
  }

  async function apiRequest(path, options = {}) {
    const response = await apiFetch(path, options);
    const text = await response.text();
    let body = null;
    try { body = text ? JSON.parse(text) : null; } catch (_) { body = text; }
    if (!response.ok) {
      let message;
      if (body && typeof body === 'object') {
        if (body.timestamp || body.status || body.error) {
          // Spring Boot default error response format
          message = body.message || body.error || `Request failed (${response.status}).`;
        } else {
          // Validation errors map or other object
          message = Object.values(body).join(' ');
        }
      } else {
        message = body || `Request failed (${response.status}).`;
      }
      throw new Error(message);
    }
    return body;
  }

  function showToast(message, type = 'error') {
    let toast = document.getElementById('infolink-toast');
    if (!toast) {
      toast = document.createElement('div');
      toast.id = 'infolink-toast';
      toast.style.cssText = 'position:fixed;right:24px;bottom:24px;z-index:1000;max-width:380px;padding:14px 18px;border:1px solid var(--border);border-radius:10px;background:var(--bg-card);box-shadow:0 8px 28px rgba(0,0,0,.25);font:600 .85rem Inter,sans-serif;transition:opacity .2s;';
      document.body.appendChild(toast);
    }
    toast.style.color = type === 'success' ? 'var(--success)' : 'var(--danger)';
    toast.textContent = message;
    toast.style.opacity = '1';
    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => { toast.style.opacity = '0'; }, 4200);
  }

  async function login(username, password) {
    const response = await fetch('/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    if (!response.ok) {
      const text = await response.text();
      let message = 'Invalid username or password.';
      try {
        const error = JSON.parse(text);
        message = error.message || error.error || message;
      } catch (_) {
        if (text) message = text;
      }
      throw new Error(message);
    }
    const tokens = await response.json();
    sessionStorage.setItem('infolink_access_token', tokens.accessToken);
    sessionStorage.setItem('infolink_refresh_token', tokens.refreshToken);
    const profileResponse = await apiFetch('/users/profile');
    if (!profileResponse.ok) throw new Error('Unable to load user profile.');
    const profile = await profileResponse.json();
    setSession({
      username: profile.username,
      name: profile.fullName,
      role: profile.role,
      group: profile.groupName,
      groupID: profile.groupID
    });
  }

  async function changePassword(currentPassword, newPassword, confirmPassword) {
    await apiRequest('/users/change-password', {
      method: 'POST',
      body: JSON.stringify({ currentPassword, newPassword, confirmPassword })
    });
  }

  // Initialize on load
  function init() {
    // Ensure requests array exists
    if (!localStorage.getItem(REQUESTS_KEY)) {
      localStorage.setItem(REQUESTS_KEY, JSON.stringify([]));
    }
  }
  init();

  return {
    // Access Requests
    getRequests,
    saveRequests,
    addRequest,
    resolveRequest,
    getPendingRequestCount,
    // Session
    getCurrentUser,
    setSession,
    getLoginTime,
    clearSession,
    isLoggedIn,
    isAdmin,
    apiFetch,
    apiRequest,
    showToast,
    login,
    changePassword,
  };
})();