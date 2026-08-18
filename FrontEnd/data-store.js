/**
 * InfoLink Data Store
 * Centralized localStorage manager for users, groups, and logs.
 * All pages should use these functions instead of direct localStorage access.
 */

const InfoLinkStore = (() => {

  const USERS_KEY    = 'infolink_users';
  const LOGS_KEY     = 'infolink_logs';
  const GROUPS_KEY   = 'infolink_groups';
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

  // ─── Default Groups (these represent the SQL Server Groups table) ───
  const DEFAULT_GROUPS = [
    { id: 1, name: 'HR',               description: 'Human Resources department',    collections: ['HR Employees', 'Contracts'],          isActive: true },
    { id: 2, name: 'Sales',            description: 'Sales department',               collections: ['Customers', 'Orders'],                isActive: true },
    { id: 3, name: 'Finance',          description: 'Finance department',             collections: ['Payments', 'Invoices'],               isActive: true },
    { id: 4, name: 'Management',       description: 'Management with full access',    collections: ['All Collections'],                    isActive: true },
    { id: 5, name: 'Contracts',        description: 'Contracts management',           collections: ['Contract Records'],                   isActive: true },
    { id: 6, name: 'Digital Marketing', description: 'Digital Marketing department',  collections: ['Digital Marketing', 'Contracts'],     isActive: true },
  ];

  // ─── Initialize ───
  function init() {
    // Seed groups if they don't exist
    if (!localStorage.getItem(GROUPS_KEY)) {
      localStorage.setItem(GROUPS_KEY, JSON.stringify(DEFAULT_GROUPS));
    }
    // Ensure users array exists
    if (!localStorage.getItem(USERS_KEY)) {
      localStorage.setItem(USERS_KEY, JSON.stringify([]));
    }
    // Ensure logs array exists
    if (!localStorage.getItem(LOGS_KEY)) {
      localStorage.setItem(LOGS_KEY, JSON.stringify([]));
    }
    // Ensure requests array exists
    if (!localStorage.getItem(REQUESTS_KEY)) {
      localStorage.setItem(REQUESTS_KEY, JSON.stringify([]));
    }
  }

  // ─── USERS ───
  function getUsers() {
    return JSON.parse(localStorage.getItem(USERS_KEY) || '[]');
  }

  function saveUsers(users) {
    localStorage.setItem(USERS_KEY, JSON.stringify(users));
  }

  function addUser(userData) {
    const users = getUsers();
    // Check duplicate username
    if (users.some(u => u.username.toLowerCase() === userData.username.toLowerCase())) {
      return { success: false, message: 'Username already exists.' };
    }
    // Build created date
    const now = new Date();
    const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    userData.created = `${months[now.getMonth()]} ${now.getDate()}, ${now.getFullYear()}`;
    userData.id = Date.now(); // Unique ID

    users.push(userData);
    saveUsers(users);
    return { success: true, message: 'User created successfully.' };
  }

  function updateUser(index, userData) {
    const users = getUsers();
    if (index >= 0 && index < users.length) {
      // Preserve id and created date
      userData.id = users[index].id;
      userData.created = users[index].created;
      users[index] = { ...users[index], ...userData };
      saveUsers(users);
      return { success: true };
    }
    return { success: false, message: 'User not found.' };
  }

  function deleteUser(index) {
    const users = getUsers();
    if (index >= 0 && index < users.length) {
      users.splice(index, 1);
      saveUsers(users);
      return { success: true };
    }
    return { success: false };
  }

  function findUserByCredentials(username, password) {
    const users = getUsers();
    return users.find(u =>
      u.username.toLowerCase() === username.toLowerCase() &&
      u.password === password &&
      u.status === 'Active'
    );
  }

  function getUserCount() {
    return getUsers().length;
  }

  function getActiveUserCount() {
    return getUsers().filter(u => u.status === 'Active').length;
  }

  // ─── GROUPS ───
  function getGroups() {
    return JSON.parse(localStorage.getItem(GROUPS_KEY) || '[]');
  }

  function saveGroups(groups) {
    localStorage.setItem(GROUPS_KEY, JSON.stringify(groups));
  }

  function getGroupCollections(groupName) {
    const groups = getGroups();
    const group = groups.find(g => g.name === groupName);
    return group ? group.collections : [];
  }

  // Return merged, deduplicated collections for an array of group names
  function getGroupCollectionsForGroups(groupNames) {
    if (!Array.isArray(groupNames) || !groupNames.length) return [];
    const groups = getGroups();
    const set = new Set();
    groupNames.forEach(name => {
      const g = groups.find(g => g.name === name);
      if (g) g.collections.forEach(c => set.add(c));
    });
    return [...set];
  }

  function getActiveGroupCount() {
    return getGroups().filter(g => g.isActive).length;
  }

  function deleteGroup(id) {
    const groups = getGroups().filter(g => g.id !== id);
    saveGroups(groups);
  }

  // ─── ACCESS REQUESTS ───
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

    // If approved, add the requested group to the user's groups array
    if (action === 'approved' && r.requestedGroup) {
      const users = getUsers();
      const u = users.find(u => u.username === r.username);
      if (u) {
        if (!Array.isArray(u.groups)) u.groups = u.group ? [u.group] : [];
        if (!u.groups.includes(r.requestedGroup)) {
          u.groups.push(r.requestedGroup);
          u.group = u.groups[0]; // keep legacy field in sync
          // Update session if this is the current logged-in user
          const sess = sessionStorage.getItem('infolink_user');
          if (sess && sess.toLowerCase() === u.username.toLowerCase()) {
            sessionStorage.setItem('infolink_groups', u.groups.join(','));
          }
        }
        saveUsers(users);
      }
    }
  }

  function getPendingRequestCount() {
    return getRequests().filter(r => r.status === 'pending').length;
  }

  // ─── LOGS ───
  function getLogs() {
    return JSON.parse(localStorage.getItem(LOGS_KEY) || '[]');
  }

  function saveLogs(logs) {
    localStorage.setItem(LOGS_KEY, JSON.stringify(logs));
  }

  function addLog(logEntry) {
    const logs = getLogs();
    const now = new Date();
    logEntry.id          = Date.now();
    logEntry.searchDate  = now.toISOString();
    logEntry.displayDate = formatLogDate(now);
    logEntry.ipAddress   = _clientIP || sessionStorage.getItem('infolink_client_ip') || 'N/A';
    logs.unshift(logEntry); // Most recent first
    saveLogs(logs);
  }

  function getTodayLogCount() {
    const logs = getLogs();
    const today = new Date().toDateString();
    return logs.filter(l => new Date(l.searchDate).toDateString() === today).length;
  }

  function getRecentLogs(count = 5) {
    return getLogs().slice(0, count);
  }

  function getLogsForUser(username) {
    return getLogs().filter(l => l.user && l.user.toLowerCase() === username.toLowerCase());
  }

  function getTodayLogCountForUser(username) {
    const today = new Date().toDateString();
    return getLogsForUser(username).filter(l => new Date(l.searchDate).toDateString() === today).length;
  }

  // ─── HELPERS ───
  function formatLogDate(date) {
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();
    const hours = date.getHours().toString().padStart(2, '0');
    const mins  = date.getMinutes().toString().padStart(2, '0');
    if (isToday) return `Today, ${hours}:${mins}`;
    const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    return `${months[date.getMonth()]} ${date.getDate()}, ${hours}:${mins}`;
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
      groups,
    };
  }

  function setSession(user) {
    const role = user.role.toLowerCase() === 'admin' ? 'admin' : 'user';
    sessionStorage.setItem('infolink_role',      role);
    sessionStorage.setItem('infolink_user',      user.username);
    sessionStorage.setItem('infolink_fullname',  user.name || user.fullname || user.username);
    sessionStorage.setItem('infolink_group',     user.group || '');
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
    return sessionStorage.getItem('infolink_role') === 'admin';
  }

  // Initialize on load
  init();

  return {
    // Users
    getUsers,
    saveUsers,
    addUser,
    updateUser,
    deleteUser,
    findUserByCredentials,
    getUserCount,
    getActiveUserCount,
    // Groups
    getGroups,
    saveGroups,
    getGroupCollections,
    getGroupCollectionsForGroups,
    getActiveGroupCount,
    deleteGroup,
    // Access Requests
    getRequests,
    saveRequests,
    addRequest,
    resolveRequest,
    getPendingRequestCount,
    // Logs
    getLogs,
    addLog,
    getTodayLogCount,
    getRecentLogs,
    getLogsForUser,
    getTodayLogCountForUser,
    // Session
    getCurrentUser,
    setSession,
    getLoginTime,
    clearSession,
    isLoggedIn,
    isAdmin,
  };

})();
