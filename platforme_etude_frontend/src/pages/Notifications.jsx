import { useState, useEffect } from 'react';
import { getNotifications, markAsRead, deleteNotification } from '../api/notificationService';
import EmptyState from '../components/ui/EmptyState';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import StatusBadge from '../components/ui/StatusBadge';
import { Bell, Check, Trash2, Eye } from 'lucide-react';
import './Notifications.css';

export default function Notifications() {
  const [notifs, setNotifs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try { setNotifs(await getNotifications()); }
    catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleMarkRead = async (id) => {
    try {
      await markAsRead(id);
      setNotifs(notifs.map((n) => n.id === id ? { ...n, lue: true } : n));
    } catch (err) { console.error(err); }
  };

  const handleDelete = async (id) => {
    try {
      await deleteNotification(id);
      setNotifs(notifs.filter((n) => n.id !== id));
    } catch (err) { console.error(err); }
  };

  const handleMarkAllRead = async () => {
    const unread = notifs.filter((n) => !n.lue);
    for (const n of unread) {
      try { await markAsRead(n.id); } catch {}
    }
    setNotifs(notifs.map((n) => ({ ...n, lue: true })));
  };

  const filtered = filter === 'all' ? notifs : filter === 'unread' ? notifs.filter((n) => !n.lue) : notifs.filter((n) => n.lue);
  const unreadCount = notifs.filter((n) => !n.lue).length;

  if (loading) return <div className="page"><LoadingSpinner size="lg" text="Chargement..." /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1>Notifications</h1>
            <p>{unreadCount > 0 ? `${unreadCount} notification${unreadCount > 1 ? 's' : ''} non lue${unreadCount > 1 ? 's' : ''}` : 'Toutes vos notifications sont lues'}</p>
          </div>
          {unreadCount > 0 && (
            <button className="btn btn-secondary" onClick={handleMarkAllRead}>
              <Check /> Tout marquer comme lu
            </button>
          )}
        </div>
      </div>

      <div className="tabs">
        <button className={`tab ${filter === 'all' ? 'active' : ''}`} onClick={() => setFilter('all')}>Toutes ({notifs.length})</button>
        <button className={`tab ${filter === 'unread' ? 'active' : ''}`} onClick={() => setFilter('unread')}>Non lues ({unreadCount})</button>
        <button className={`tab ${filter === 'read' ? 'active' : ''}`} onClick={() => setFilter('read')}>Lues ({notifs.length - unreadCount})</button>
      </div>

      {filtered.length === 0 ? (
        <EmptyState icon={<Bell />} title="Aucune notification" description="Vous êtes à jour !" />
      ) : (
        <div className="notif-list">
          {filtered.map((n) => (
            <div key={n.id} className={`notif-item card ${!n.lue ? 'notif-item--unread' : ''}`}>
              <div className="card-body">
                <div className="notif-item__row">
                  <div className="notif-item__icon">
                    <Bell />
                  </div>
                  <div className="notif-item__content">
                    <p className="notif-item__msg">{n.message}</p>
                    <div className="notif-item__meta">
                      <StatusBadge status={n.type} />
                      <span className="text-sm text-secondary">
                        {new Date(n.dateEnvoi).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                  </div>
                  <div className="notif-item__actions">
                    {!n.lue && (
                      <button className="btn btn-ghost btn-icon btn-sm" onClick={() => handleMarkRead(n.id)} title="Marquer comme lu">
                        <Eye />
                      </button>
                    )}
                    <button className="btn btn-ghost btn-icon btn-sm" onClick={() => handleDelete(n.id)} title="Supprimer">
                      <Trash2 />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
