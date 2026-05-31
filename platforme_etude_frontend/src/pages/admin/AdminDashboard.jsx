import { useState, useEffect } from 'react';
import { getUsers, getAllSessions, getAllGroupes, getAllObjectifs, getAllNotifications } from '../../api/adminService';
import StatCard from '../../components/ui/StatCard';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import {
  Users, UserCheck, UserX, CalendarClock, CheckCircle2, UsersRound,
  Target, Bell, ArrowRight, TrendingUp
} from 'lucide-react';
import { Link } from 'react-router-dom';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts';
import './AdminDashboard.css';

const PIE_COLORS_STATUS = ['#2563eb', '#16a34a', '#6b7280'];
const PIE_COLORS_ACTIVE = ['#16a34a', '#dc2626'];

export default function AdminDashboard() {
  const [loading, setLoading] = useState(true);
  const [users, setUsers] = useState([]);
  const [sessions, setSessions] = useState([]);
  const [groupes, setGroupes] = useState([]);
  const [objectifs, setObjectifs] = useState([]);
  const [notifications, setNotifications] = useState([]);

  async function loadData() {
    try {
      const [u, s, g, o, n] = await Promise.allSettled([
        getUsers(),
        getAllSessions(),
        getAllGroupes(),
        getAllObjectifs(),
        getAllNotifications(),
      ]);
      setUsers(u.status === 'fulfilled' ? u.value : []);
      setSessions(s.status === 'fulfilled' ? s.value : []);
      setGroupes(g.status === 'fulfilled' ? g.value : []);
      setObjectifs(o.status === 'fulfilled' ? o.value : []);
      setNotifications(n.status === 'fulfilled' ? n.value : []);
    } catch (err) {
      console.error('Admin dashboard load error:', err);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => loadData(), 0);
    return () => window.clearTimeout(timer);
  }, []);

  // Compute statistics
  const activeUsers = users.filter((u) => u.actif);
  const inactiveUsers = users.filter((u) => !u.actif);
  const plannedSessions = sessions.filter((s) => s.statut === 'PLANIFIEE');
  const completedSessions = sessions.filter((s) => s.statut === 'TERMINEE');
  const cancelledSessions = sessions.filter((s) => s.statut === 'ANNULEE');

  // Chart data: user activity pie
  const userActivityData = [
    { name: 'Actifs', value: activeUsers.length },
    { name: 'Inactifs', value: inactiveUsers.length },
  ];

  // Chart data: sessions by status pie
  const sessionStatusData = [
    { name: 'Planifiées', value: plannedSessions.length },
    { name: 'Terminées', value: completedSessions.length },
    { name: 'Annulées', value: cancelledSessions.length },
  ].filter((d) => d.value > 0);

  // Chart data: top groups by member count
  const topGroupes = [...groupes]
    .sort((a, b) => (b.users?.length || 0) - (a.users?.length || 0))
    .slice(0, 6)
    .map((g) => ({
      name: g.nom?.length > 15 ? g.nom.substring(0, 15) + '...' : g.nom,
      membres: g.users?.length || 0,
    }));

  // Chart data: sessions per month (from real data)
  const sessionsPerMonth = (() => {
    const months = {};
    const monthNames = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc'];
    sessions.forEach((s) => {
      if (s.dateDebut) {
        const d = new Date(s.dateDebut);
        const key = `${d.getFullYear()}-${d.getMonth()}`;
        const label = `${monthNames[d.getMonth()]} ${d.getFullYear()}`;
        if (!months[key]) months[key] = { name: label, sessions: 0, sortKey: d.getTime() };
        months[key].sessions++;
      }
    });
    return Object.values(months).sort((a, b) => a.sortKey - b.sortKey).slice(-6);
  })();

  // Recent users (last 5)
  const recentUsers = [...users].slice(-5).reverse();

  // Recent sessions (last 5)
  const recentSessions = [...sessions].slice(-5).reverse();

  // Recent activity (from notifications, last 8)
  const recentActivity = [...notifications].slice(-8).reverse();

  if (loading) {
    return (
      <div className="page" style={{ display: 'flex', justifyContent: 'center', paddingTop: '80px' }}>
        <LoadingSpinner size="lg" text="Chargement du dashboard admin..." />
      </div>
    );
  }

  return (
    <div className="page admin-dashboard">
      {/* ── Stat Cards ── */}
      <div className="admin-dashboard__stats">
        <StatCard icon={<Users />} label="Total utilisateurs" value={users.length} color="primary" />
        <StatCard icon={<UserCheck />} label="Utilisateurs actifs" value={activeUsers.length} color="success" />
        <StatCard icon={<UserX />} label="Utilisateurs inactifs" value={inactiveUsers.length} color="warning" />
        <StatCard icon={<CalendarClock />} label="Sessions planifiées" value={plannedSessions.length} color="primary" />
        <StatCard icon={<CheckCircle2 />} label="Sessions terminées" value={completedSessions.length} color="success" />
        <StatCard icon={<UsersRound />} label="Groupes créés" value={groupes.length} color="accent" />
        <StatCard icon={<Target />} label="Objectifs créés" value={objectifs.length} color="warning" />
        <StatCard icon={<Bell />} label="Notifications envoyées" value={notifications.length} color="primary" />
      </div>

      {/* ── Charts Row 1 ── */}
      <div className="admin-dashboard__charts-row">
        <div className="card admin-dashboard__chart-card">
          <div className="card-header">
            <h3>Sessions créées par mois</h3>
            <TrendingUp style={{ width: 18, height: 18, color: 'var(--text-tertiary)' }} />
          </div>
          <div className="card-body">
            {sessionsPerMonth.length > 0 ? (
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={sessionsPerMonth} barSize={32}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis dataKey="name" tick={{ fontSize: 12, fill: '#6b7280' }} />
                  <YAxis tick={{ fontSize: 12, fill: '#6b7280' }} allowDecimals={false} />
                  <Tooltip
                    contentStyle={{
                      background: '#fff',
                      border: '1px solid #e5e7eb',
                      borderRadius: 8,
                      fontSize: 13,
                      boxShadow: '0 4px 6px -1px rgba(0,0,0,0.07)',
                    }}
                  />
                  <Bar dataKey="sessions" fill="#2563eb" radius={[6, 6, 0, 0]} name="Sessions" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="admin-dashboard__empty-chart">Aucune donnée disponible</div>
            )}
          </div>
        </div>

        <div className="card admin-dashboard__chart-card">
          <div className="card-header">
            <h3>Répartition des utilisateurs</h3>
          </div>
          <div className="card-body">
            {users.length > 0 ? (
              <ResponsiveContainer width="100%" height={280}>
                <PieChart>
                  <Pie
                    data={userActivityData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {userActivityData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={PIE_COLORS_ACTIVE[index]} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{
                      background: '#fff',
                      border: '1px solid #e5e7eb',
                      borderRadius: 8,
                      fontSize: 13,
                    }}
                  />
                  <Legend
                    verticalAlign="bottom"
                    iconType="circle"
                    wrapperStyle={{ fontSize: 13 }}
                  />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="admin-dashboard__empty-chart">Aucune donnée disponible</div>
            )}
          </div>
        </div>
      </div>

      {/* ── Charts Row 2 ── */}
      <div className="admin-dashboard__charts-row">
        <div className="card admin-dashboard__chart-card">
          <div className="card-header">
            <h3>Sessions par statut</h3>
          </div>
          <div className="card-body">
            {sessionStatusData.length > 0 ? (
              <ResponsiveContainer width="100%" height={280}>
                <PieChart>
                  <Pie
                    data={sessionStatusData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {sessionStatusData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={PIE_COLORS_STATUS[index % PIE_COLORS_STATUS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{
                      background: '#fff',
                      border: '1px solid #e5e7eb',
                      borderRadius: 8,
                      fontSize: 13,
                    }}
                  />
                  <Legend
                    verticalAlign="bottom"
                    iconType="circle"
                    wrapperStyle={{ fontSize: 13 }}
                  />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="admin-dashboard__empty-chart">Aucune donnée disponible</div>
            )}
          </div>
        </div>

        <div className="card admin-dashboard__chart-card">
          <div className="card-header">
            <h3>Groupes les plus actifs</h3>
          </div>
          <div className="card-body">
            {topGroupes.length > 0 ? (
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={topGroupes} barSize={28} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis type="number" tick={{ fontSize: 12, fill: '#6b7280' }} allowDecimals={false} />
                  <YAxis dataKey="name" type="category" tick={{ fontSize: 11, fill: '#6b7280' }} width={120} />
                  <Tooltip
                    contentStyle={{
                      background: '#fff',
                      border: '1px solid #e5e7eb',
                      borderRadius: 8,
                      fontSize: 13,
                    }}
                  />
                  <Bar dataKey="membres" fill="#7c3aed" radius={[0, 6, 6, 0]} name="Membres" />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="admin-dashboard__empty-chart">Aucune donnée disponible</div>
            )}
          </div>
        </div>
      </div>

      {/* ── Tables ── */}
      <div className="admin-dashboard__tables-row">
        {/* Recent Users */}
        <div className="card">
          <div className="card-header">
            <h3>Derniers utilisateurs inscrits</h3>
            <Link to="/admin/users" className="btn btn-ghost btn-sm">
              Voir tout <ArrowRight />
            </Link>
          </div>
          <div className="card-body" style={{ padding: 0 }}>
            {recentUsers.length === 0 ? (
              <div className="admin-dashboard__empty-table">Aucun utilisateur</div>
            ) : (
              <div className="table-container" style={{ border: 'none', borderRadius: 0 }}>
                <table className="table">
                  <thead>
                    <tr>
                      <th>Nom complet</th>
                      <th>Email</th>
                      <th>Rôle</th>
                      <th>Statut</th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentUsers.map((u) => (
                      <tr key={u.id}>
                        <td className="font-medium">{u.nom} {u.prenom}</td>
                        <td className="text-sm text-secondary">{u.email}</td>
                        <td>
                          <span className={`badge ${u.role === 'ROLE_ADMIN' ? 'badge-primary' : 'badge-accent'}`}>
                            {u.role === 'ROLE_ADMIN' ? 'Admin' : 'Étudiant'}
                          </span>
                        </td>
                        <td>
                          <span className={`badge ${u.actif ? 'badge-success' : 'badge-neutral'}`}>
                            {u.actif ? 'Actif' : 'Inactif'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {/* Recent Sessions */}
        <div className="card">
          <div className="card-header">
            <h3>Dernières sessions créées</h3>
          </div>
          <div className="card-body" style={{ padding: 0 }}>
            {recentSessions.length === 0 ? (
              <div className="admin-dashboard__empty-table">Aucune session</div>
            ) : (
              <div className="table-container" style={{ border: 'none', borderRadius: 0 }}>
                <table className="table">
                  <thead>
                    <tr>
                      <th>Titre</th>
                      <th>Matière</th>
                      <th>Utilisateur</th>
                      <th>Statut</th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentSessions.map((s) => (
                      <tr key={s.id}>
                        <td className="font-medium">{s.titre}</td>
                        <td><span className="badge badge-primary">{s.matiereNom}</span></td>
                        <td className="text-sm text-secondary">{s.userNom}</td>
                        <td><StatusBadge status={s.statut} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ── Activity Feed ── */}
      <div className="card admin-dashboard__activity">
        <div className="card-header">
          <h3>Activité récente</h3>
        </div>
        <div className="card-body" style={{ padding: 0 }}>
          {recentActivity.length === 0 ? (
            <div className="admin-dashboard__empty-table">Aucune activité récente</div>
          ) : (
            <div className="table-container" style={{ border: 'none', borderRadius: 0 }}>
              <table className="table">
                <thead>
                  <tr>
                    <th>Utilisateur</th>
                    <th>Message</th>
                    <th>Type</th>
                    <th>Date</th>
                    <th>Statut</th>
                  </tr>
                </thead>
                <tbody>
                  {recentActivity.map((n) => (
                    <tr key={n.id}>
                      <td className="font-medium">{n.userNom || '—'}</td>
                      <td className="text-sm" style={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {n.message}
                      </td>
                      <td><StatusBadge status={n.type} /></td>
                      <td className="text-sm text-secondary">
                        {n.dateEnvoi ? new Date(n.dateEnvoi).toLocaleDateString('fr-FR', {
                          day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit'
                        }) : '—'}
                      </td>
                      <td>
                        <span className={`badge ${n.lue ? 'badge-success' : 'badge-warning'}`}>
                          {n.lue ? 'Lue' : 'Non lue'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
