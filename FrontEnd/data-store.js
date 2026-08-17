/**
 * InfoLink Data Store
 * Centralized localStorage manager for users, groups, and logs.
 * All pages should use these functions instead of direct localStorage access.
 */

const InfoLinkStore = (() => {

  const USERS_KEY  = 'infolink_users';
  const LOGS_KEY   = 'infolink_logs';
  const GROUPS_KEY = 'infolink_groups';

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

  function getActiveGroupCount() {
    return getGroups().filter(g => g.isActive).length;
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
    logEntry.id = Date.now();
    logEntry.searchDate = now.toISOString();
    logEntry.displayDate = formatLogDate(now);
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
    return {
      username: sessionStorage.getItem('infolink_user') || '',
      fullname: sessionStorage.getItem('infolink_fullname') || '',
      role:     sessionStorage.getItem('infolink_role') || '',
      group:    sessionStorage.getItem('infolink_group') || '',
    };
  }

  function setSession(user) {
    const role = user.role.toLowerCase() === 'admin' ? 'admin' : 'user';
    sessionStorage.setItem('infolink_role', role);
    sessionStorage.setItem('infolink_user', user.username);
    sessionStorage.setItem('infolink_fullname', user.name || user.fullname || user.username);
    sessionStorage.setItem('infolink_group', user.group || '');
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
    getActiveGroupCount,
    // Logs
    getLogs,
    addLog,
    getTodayLogCount,
    getRecentLogs,
    // Session
    getCurrentUser,
    setSession,
    clearSession,
    isLoggedIn,
    isAdmin,
  };

})();
