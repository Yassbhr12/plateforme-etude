import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { getSessions, getSessionsByWeek } from '../api/sessionService';
import { getObjectifsByWeek } from '../api/objectifService';
import { getNotifications } from '../api/notificationService';
import { getMyAdminGroupes } from '../api/groupeService';
import StatCard from '../components/ui/StatCard';
import StatusBadge from '../components/ui/StatusBadge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { Clock, CalendarClock, Target, Users, Bell, ArrowRight, BookOpen } from 'lucide-react';
import { Link } from 'react-router-dom';
import './Dashboard.css';

export default function Dashboard() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [sessions, setSessions] = useState([]);
  const [weekSessions, setWeekSessions] = useState([]);
  const [objectifs, setObjectifs] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [groupes, setGroupes] = useState([]);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      const [sessionsData, weekData, objData, notifData, groupeData] = await Promise.allSettled([
        getSessions(),
        getSessionsByWeek(),
        getObjectifsByWeek(),
        getNotifications(),
        getMyAdminGroupes(),
      ]);

      setSessions(sessionsData.status === 'fulfilled' ? sessionsData.value : []);
      setWeekSessions(weekData.status === 'fulfilled' ? weekData.value : []);
      setObjectifs(objData.status === 'fulfilled' ? objData.value : []);
      setNotifications(notifData.status === 'fulfilled' ? notifData.value : []);
      setGroupes(groupeData.status === 'fulfilled' ? groupeData.value : []);
    } catch (err) {
      console.error('Dashboard load error:', err);
    } finally {
      setLoading(false);
    }
  };

  const completedSessions = sessions.filter((s) => s.statut === 'TERMINEE');
  const totalHours = completedSessions.reduce((sum, s) => sum + (s.dureeMax || 0), 0) / 60;
  const plannedSessions = weekSessions.filter((s) => s.statut === 'PLANIFIEE');
  const completedObjectifs = objectifs.filter((o) => {
    const done = completedSessions
      .filter((s) => s.matiereId === o.matiereId)
      .reduce((sum, s) => sum + (s.dureeMax || 0), 0) / 60;
    return done >= o.heuresCibles;
  });
  const unreadNotifs = notifications.filter((n) => !n.lue);

  if (loading) {
    return (
      <div className="page" style={{ display: 'flex', justifyContent: 'center', paddingTop: '80px' }}>
        <LoadingSpinner size="lg" text="Chargement du dashboard..." />
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Bonjour, {user?.prenom || 'Étudiant'}</h1>
        <p>Voici un aperçu de votre activité d'étude</p>
      </div>

      {/* Stats Cards */}
      <div className="grid-stats">
        <StatCard
          icon={<Clock />}
          label="Heures étudiées"
          value={`${totalHours.toFixed(1)}h`}
          color="primary"
        />
        <StatCard
          icon={<CalendarClock />}
          label="Sessions prévues"
          value={plannedSessions.length}
          color="accent"
        />
        <StatCard
          icon={<Target />}
          label="Objectifs atteints"
          value={`${completedObjectifs.length}/${objectifs.length}`}
          color="success"
        />
        <StatCard
          icon={<Users />}
          label="Groupes actifs"
          value={groupes.length}
          color="warning"
        />
      </div>

      <div className="dashboard__grid">
        {/* Upcoming Sessions */}
        <div className="card">
          <div className="card-header">
            <h3>Prochaines sessions</h3>
            <Link to="/sessions" className="btn btn-ghost btn-sm">
              Voir tout <ArrowRight />
            </Link>
          </div>
          <div className="card-body" style={{ padding: 0 }}>
            {plannedSessions.length === 0 ? (
              <div style={{ padding: '32px', textAlign: 'center', color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                Aucune session planifiée cette semaine
              </div>
            ) : (
              <div className="table-container" style={{ border: 'none', borderRadius: 0 }}>
                <table className="table">
                  <thead>
                    <tr>
                      <th>Session</th>
                      <th>Matière</th>
                      <th>Date</th>
                      <th>Durée</th>
                      <th>Statut</th>
                    </tr>
                  </thead>
                  <tbody>
                    {plannedSessions.slice(0, 5).map((s) => (
                      <tr key={s.id}>
                        <td className="font-medium">{s.titre}</td>
                        <td>
                          <span className="badge badge-primary">{s.matiereNom}</span>
                        </td>
                        <td className="text-sm text-secondary">
                          {new Date(s.dateDebut).toLocaleDateString('fr-FR', { weekday: 'short', day: 'numeric', month: 'short' })}
                        </td>
                        <td className="text-sm">{s.dureeMax} min</td>
                        <td><StatusBadge status={s.statut} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {/* Weekly Progress */}
        <div className="card">
          <div className="card-header">
            <h3>Progression hebdomadaire</h3>
            <Link to="/objectifs" className="btn btn-ghost btn-sm">
              Voir tout <ArrowRight />
            </Link>
          </div>
          <div className="card-body">
            {objectifs.length === 0 ? (
              <div style={{ textAlign: 'center', color: 'var(--text-secondary)', fontSize: '0.875rem', padding: '16px 0' }}>
                Aucun objectif défini cette semaine
              </div>
            ) : (
              <div className="dashboard__progress-list">
                {objectifs.map((obj) => {
                  const done = completedSessions
                    .filter((s) => s.matiereId === obj.matiereId)
                    .reduce((sum, s) => sum + (s.dureeMax || 0), 0) / 60;
                  const pct = Math.min(100, Math.round((done / obj.heuresCibles) * 100));
                  return (
                    <div key={obj.id} className="dashboard__progress-item">
                      <div className="dashboard__progress-info">
                        <div className="flex items-center gap-2">
                          <BookOpen style={{ width: 16, height: 16, color: 'var(--primary)' }} />
                          <span className="font-medium text-sm">{obj.matiereNom}</span>
                        </div>
                        <span className="text-sm text-secondary">{done.toFixed(1)}h / {obj.heuresCibles}h</span>
                      </div>
                      <div className="dashboard__progress-bar">
                        <div
                          className="dashboard__progress-fill"
                          style={{ width: `${pct}%`, background: pct >= 100 ? 'var(--success)' : 'var(--primary)' }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Notifications */}
      <div className="card mt-6">
        <div className="card-header">
          <h3>
            Notifications récentes
            {unreadNotifs.length > 0 && (
              <span className="badge badge-primary" style={{ marginLeft: 8 }}>{unreadNotifs.length}</span>
            )}
          </h3>
          <Link to="/notifications" className="btn btn-ghost btn-sm">
            Voir tout <ArrowRight />
          </Link>
        </div>
        <div className="card-body" style={{ padding: 0 }}>
          {notifications.length === 0 ? (
            <div style={{ padding: '32px', textAlign: 'center', color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
              Aucune notification
            </div>
          ) : (
            <div className="dashboard__notif-list">
              {notifications.slice(0, 5).map((n) => (
                <div key={n.id} className={`dashboard__notif-item ${!n.lue ? 'unread' : ''}`}>
                  <div className="dashboard__notif-icon">
                    <Bell />
                  </div>
                  <div className="dashboard__notif-content">
                    <p className="dashboard__notif-msg">{n.message}</p>
                    <span className="dashboard__notif-time">
                      {new Date(n.dateEnvoi).toLocaleDateString('fr-FR', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <StatusBadge status={n.type} />
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
