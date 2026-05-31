import { useEffect, useMemo, useState } from 'react';
import { Activity, BarChart3, Database, MessageCircle, Target, Users } from 'lucide-react';
import {
  getAllCommentaires,
  getAllDisponibilites,
  getAllGroupes,
  getAllInvitations,
  getAllMatieres,
  getAllMessages,
  getAllNotifications,
  getAllObjectifs,
  getAllSessions,
  getUsers,
} from '../../api/adminService';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import StatCard from '../../components/ui/StatCard';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import './AdminDashboard.css';
import './AdminCommon.css';

const COLORS = ['#2563eb', '#16a34a', '#7c3aed', '#d97706', '#dc2626', '#64748b'];
const STAT_KEYS = [
  'users',
  'sessions',
  'groupes',
  'objectifs',
  'notifications',
  'matieres',
  'disponibilites',
  'invitations',
  'messages',
  'commentaires',
];
const STAT_LOADERS = [
  getUsers,
  getAllSessions,
  getAllGroupes,
  getAllObjectifs,
  getAllNotifications,
  getAllMatieres,
  getAllDisponibilites,
  getAllInvitations,
  getAllMessages,
  getAllCommentaires,
];

export default function AdminStats() {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState({
    users: [],
    sessions: [],
    groupes: [],
    objectifs: [],
    notifications: [],
    matieres: [],
    disponibilites: [],
    invitations: [],
    messages: [],
    commentaires: [],
  });

  async function loadStats() {
    setLoading(true);
    const results = await Promise.allSettled(STAT_LOADERS.map((loader) => loader()));
    const next = {};
    results.forEach((result, index) => {
      next[STAT_KEYS[index]] = result.status === 'fulfilled' ? result.value : [];
    });
    setData(next);
    setLoading(false);
  }

  useEffect(() => {
    const timer = window.setTimeout(() => loadStats(), 0);
    return () => window.clearTimeout(timer);
  }, []);

  const entityData = useMemo(
    () => [
      { name: 'Users', value: data.users.length },
      { name: 'Sessions', value: data.sessions.length },
      { name: 'Groupes', value: data.groupes.length },
      { name: 'Objectifs', value: data.objectifs.length },
      { name: 'Messages', value: data.messages.length },
      { name: 'Commentaires', value: data.commentaires.length },
    ],
    [data]
  );

  const sessionStatusData = useMemo(() => {
    const counts = data.sessions.reduce((acc, session) => {
      const key = session.statut || 'INCONNU';
      acc[key] = (acc[key] || 0) + 1;
      return acc;
    }, {});
    return Object.entries(counts).map(([name, value]) => ({ name, value }));
  }, [data.sessions]);

  const userRoleData = useMemo(() => {
    const counts = data.users.reduce((acc, user) => {
      const key = user.role === 'ROLE_ADMIN' ? 'Admins' : 'Etudiants';
      acc[key] = (acc[key] || 0) + 1;
      return acc;
    }, {});
    return Object.entries(counts).map(([name, value]) => ({ name, value }));
  }, [data.users]);

  const engagementData = useMemo(
    () => [
      { name: 'Matieres', total: data.matieres.length },
      { name: 'Disponibilites', total: data.disponibilites.length },
      { name: 'Invitations', total: data.invitations.length },
      { name: 'Notifications', total: data.notifications.length },
      { name: 'Messages', total: data.messages.length },
      { name: 'Commentaires', total: data.commentaires.length },
    ],
    [data]
  );

  if (loading) {
    return (
      <div className="page flex justify-center" style={{ paddingTop: 80 }}>
        <LoadingSpinner size="lg" text="Chargement des statistiques..." />
      </div>
    );
  }

  return (
    <div className="page">
      <div className="admin-kpi-grid">
        <StatCard icon={<Users />} label="Utilisateurs" value={data.users.length} color="primary" />
        <StatCard icon={<Activity />} label="Sessions" value={data.sessions.length} color="success" />
        <StatCard icon={<Target />} label="Objectifs" value={data.objectifs.length} color="warning" />
        <StatCard icon={<MessageCircle />} label="Interactions" value={data.messages.length + data.commentaires.length} color="accent" />
      </div>

      <div className="admin-dashboard__charts-row">
        <div className="card admin-dashboard__chart-card">
          <div className="card-header">
            <h3>Volume par entite</h3>
            <Database style={{ width: 18, height: 18, color: 'var(--text-tertiary)' }} />
          </div>
          <div className="card-body">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={entityData} barSize={34}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="name" tick={{ fontSize: 12, fill: '#6b7280' }} />
                <YAxis tick={{ fontSize: 12, fill: '#6b7280' }} allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="value" fill="#2563eb" radius={[6, 6, 0, 0]} name="Total" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="card admin-dashboard__chart-card">
          <div className="card-header">
            <h3>Roles utilisateurs</h3>
          </div>
          <div className="card-body">
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie data={userRoleData} dataKey="value" cx="50%" cy="50%" innerRadius={64} outerRadius={105} paddingAngle={4}>
                  {userRoleData.map((_, index) => (
                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend iconType="circle" />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div className="admin-dashboard__charts-row">
        <div className="card admin-dashboard__chart-card">
          <div className="card-header">
            <h3>Sessions par statut</h3>
            <BarChart3 style={{ width: 18, height: 18, color: 'var(--text-tertiary)' }} />
          </div>
          <div className="card-body">
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie data={sessionStatusData} dataKey="value" cx="50%" cy="50%" innerRadius={64} outerRadius={105} paddingAngle={4}>
                  {sessionStatusData.map((_, index) => (
                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend iconType="circle" />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="card admin-dashboard__chart-card">
          <div className="card-header">
            <h3>Activite fonctionnelle</h3>
          </div>
          <div className="card-body">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={engagementData} layout="vertical" barSize={28}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis type="number" tick={{ fontSize: 12, fill: '#6b7280' }} allowDecimals={false} />
                <YAxis type="category" dataKey="name" width={110} tick={{ fontSize: 12, fill: '#6b7280' }} />
                <Tooltip />
                <Bar dataKey="total" fill="#7c3aed" radius={[0, 6, 6, 0]} name="Total" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
