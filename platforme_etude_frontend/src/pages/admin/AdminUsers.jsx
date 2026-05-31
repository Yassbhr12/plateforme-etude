import { createElement, useState, useEffect, useMemo } from 'react';
import { deleteUser, getUsers, toggleUserStatus, updateUserRole } from '../../api/adminService';
import { useAuth } from '../../context/AuthContext';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import {
  Search, Eye, ToggleLeft, ToggleRight, Users, X, Trash2,
  ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight,
  UserCheck, UserX, ShieldCheck, GraduationCap, Database
} from 'lucide-react';
import './AdminUsers.css';

const FILTERS = [
  { key: 'all', label: 'Tous', icon: Users },
  { key: 'active', label: 'Actifs', icon: UserCheck },
  { key: 'inactive', label: 'Inactifs', icon: UserX },
  { key: 'admin', label: 'Admins', icon: ShieldCheck },
  { key: 'student', label: 'Étudiants', icon: GraduationCap },
];

const PER_PAGE = 10;

export default function AdminUsers() {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('all');
  const [currentPage, setCurrentPage] = useState(1);
  const [selectedUser, setSelectedUser] = useState(null);
  const [toggleTarget, setToggleTarget] = useState(null);
  const [roleTarget, setRoleTarget] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function loadUsers() {
    setLoading(true);
    setError('');
    try {
      const data = await getUsers();
      setUsers(data);
    } catch (err) {
      console.error('Failed to load users:', err);
      setError('Impossible de charger les utilisateurs.');
      setUsers([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => loadUsers(), 0);
    return () => window.clearTimeout(timer);
  }, []);

  // Filter & search
  const filteredUsers = useMemo(() => {
    let result = [...users];

    // Apply filter
    switch (filter) {
      case 'active':
        result = result.filter((u) => u.actif);
        break;
      case 'inactive':
        result = result.filter((u) => !u.actif);
        break;
      case 'admin':
        result = result.filter((u) => u.role === 'ROLE_ADMIN');
        break;
      case 'student':
        result = result.filter((u) => u.role === 'ROLE_USER');
        break;
    }

    // Apply search
    if (search.trim()) {
      const q = search.toLowerCase();
      result = result.filter(
        (u) =>
          u.nom?.toLowerCase().includes(q) ||
          u.prenom?.toLowerCase().includes(q) ||
          u.email?.toLowerCase().includes(q)
      );
    }

    return result;
  }, [users, filter, search]);

  // Pagination
  const totalPages = Math.max(1, Math.ceil(filteredUsers.length / PER_PAGE));
  const paginatedUsers = filteredUsers.slice((currentPage - 1) * PER_PAGE, currentPage * PER_PAGE);

  const handleToggleStatus = async () => {
    if (!toggleTarget) return;
    setSaving(true);
    setError('');
    try {
      const updated = await toggleUserStatus(toggleTarget.id);
      setUsers((prev) =>
        prev.map((u) => (u.id === toggleTarget.id ? updated : u))
      );
      setToggleTarget(null);
    } catch (err) {
      console.error('Failed to toggle user status:', err);
      setError(err.response?.data?.message || err.response?.data?.error || "Le statut n'a pas pu etre modifie.");
    } finally {
      setSaving(false);
    }
  };

  const handleToggleRole = async () => {
    if (!roleTarget) return;
    setSaving(true);
    setError('');
    try {
      const nextRole = roleTarget.role === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN';
      const updated = await updateUserRole(roleTarget.id, nextRole);
      setUsers((prev) =>
        prev.map((u) => (u.id === roleTarget.id ? updated : u))
      );
      setRoleTarget(null);
    } catch (err) {
      console.error('Failed to update user role:', err);
      setError(err.response?.data?.message || err.response?.data?.error || "Le role n'a pas pu etre modifie.");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setSaving(true);
    setError('');
    try {
      await deleteUser(deleteTarget.id);
      setUsers((prev) => prev.filter((u) => u.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err) {
      console.error('Failed to delete user:', err);
      setError(err.response?.data?.message || err.response?.data?.error || "L'utilisateur n'a pas pu etre supprime.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="page" style={{ display: 'flex', justifyContent: 'center', paddingTop: '80px' }}>
        <LoadingSpinner size="lg" text="Chargement des utilisateurs..." />
      </div>
    );
  }

  return (
    <div className="page admin-users">
      {error && <div className="alert alert-error">{error}</div>}

      {/* ── Header ── */}
      <div className="admin-users__header">
        <div>
          <h1>Gestion des utilisateurs</h1>
          <p className="text-secondary">
            {users.length} utilisateur{users.length !== 1 ? 's' : ''} enregistré{users.length !== 1 ? 's' : ''} sur la plateforme
          </p>
        </div>
      </div>

      {/* ── Search & Filters ── */}
      <div className="admin-users__toolbar">
        <div className="admin-users__search">
          <Search className="admin-users__search-icon" />
          <input
            type="text"
            placeholder="Rechercher par nom, prénom ou email..."
            className="admin-users__search-input"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setCurrentPage(1);
            }}
          />
          {search && (
            <button className="admin-users__search-clear" onClick={() => setSearch('')}>
              <X />
            </button>
          )}
        </div>

        <div className="admin-users__filters">
          {FILTERS.map(({ key, label, icon }) => (
            <button
              key={key}
              className={`admin-users__filter-btn ${filter === key ? 'active' : ''}`}
              onClick={() => {
                setFilter(key);
                setCurrentPage(1);
              }}
            >
              {createElement(icon)}
              <span>{label}</span>
              {key === 'all' && <span className="admin-users__filter-count">{users.length}</span>}
              {key === 'active' && <span className="admin-users__filter-count">{users.filter((u) => u.actif).length}</span>}
              {key === 'inactive' && <span className="admin-users__filter-count">{users.filter((u) => !u.actif).length}</span>}
              {key === 'admin' && <span className="admin-users__filter-count">{users.filter((u) => u.role === 'ROLE_ADMIN').length}</span>}
              {key === 'student' && <span className="admin-users__filter-count">{users.filter((u) => u.role === 'ROLE_USER').length}</span>}
            </button>
          ))}
        </div>
      </div>

      {/* ── Table ── */}
      {filteredUsers.length === 0 ? (
        <EmptyState
          icon={<Database />}
          title="Aucun utilisateur trouvé"
          description="Ajustez vos filtres ou votre recherche"
        />
      ) : (
        <>
          <div className="card admin-users__table-card">
            <div className="table-container" style={{ border: 'none', borderRadius: 0 }}>
              <table className="table">
                <thead>
                  <tr>
                    <th>Utilisateur</th>
                    <th>Email</th>
                    <th>Rôle</th>
                    <th>Statut</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedUsers.map((u) => (
                    <tr key={u.id}>
                      <td>
                        <div className="admin-users__user-cell">
                          <div className="admin-users__user-avatar">
                            {u.prenom?.[0]}{u.nom?.[0]}
                          </div>
                          <div>
                            <span className="font-medium">{u.nom} {u.prenom}</span>
                            <span className="admin-users__user-id text-sm text-secondary">ID: {u.id}</span>
                          </div>
                        </div>
                      </td>
                      <td className="text-sm">{u.email}</td>
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
                      <td>
                        <div className="admin-users__actions">
                          <button
                            className="btn btn-ghost btn-icon btn-sm"
                            title="Voir les détails"
                            onClick={() => setSelectedUser(u)}
                          >
                            <Eye />
                          </button>
                          <button
                            className="btn btn-ghost btn-icon btn-sm"
                            title={u.actif ? 'Désactiver le compte' : 'Activer le compte'}
                            onClick={() => setToggleTarget(u)}
                            disabled={u.id === currentUser?.id}
                          >
                            {u.actif ? <ToggleRight style={{ color: 'var(--success)' }} /> : <ToggleLeft style={{ color: 'var(--text-tertiary)' }} />}
                          </button>
                          <button
                            className="btn btn-ghost btn-icon btn-sm"
                            title={u.role === 'ROLE_ADMIN' ? 'Retirer le role admin' : 'Promouvoir admin'}
                            onClick={() => setRoleTarget(u)}
                            disabled={u.id === currentUser?.id}
                          >
                            <ShieldCheck style={{ color: u.role === 'ROLE_ADMIN' ? 'var(--primary)' : 'var(--text-tertiary)' }} />
                          </button>
                          <button
                            className="btn btn-ghost btn-icon btn-sm"
                            title="Supprimer"
                            onClick={() => setDeleteTarget(u)}
                            disabled={u.id === currentUser?.id}
                          >
                            <Trash2 />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* ── Pagination ── */}
          <div className="admin-users__pagination">
            <span className="admin-users__pagination-info text-sm text-secondary">
              Affichage de {(currentPage - 1) * PER_PAGE + 1} à {Math.min(currentPage * PER_PAGE, filteredUsers.length)} sur {filteredUsers.length} utilisateur{filteredUsers.length !== 1 ? 's' : ''}
            </span>
            <div className="admin-users__pagination-buttons">
              <button
                className="btn btn-ghost btn-icon btn-sm"
                disabled={currentPage === 1}
                onClick={() => setCurrentPage(1)}
                title="Première page"
              >
                <ChevronsLeft />
              </button>
              <button
                className="btn btn-ghost btn-icon btn-sm"
                disabled={currentPage === 1}
                onClick={() => setCurrentPage((p) => p - 1)}
                title="Page précédente"
              >
                <ChevronLeft />
              </button>
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                let page;
                if (totalPages <= 5) {
                  page = i + 1;
                } else if (currentPage <= 3) {
                  page = i + 1;
                } else if (currentPage >= totalPages - 2) {
                  page = totalPages - 4 + i;
                } else {
                  page = currentPage - 2 + i;
                }
                return (
                  <button
                    key={page}
                    className={`btn btn-sm admin-users__page-btn ${page === currentPage ? 'active' : 'btn-ghost'}`}
                    onClick={() => setCurrentPage(page)}
                  >
                    {page}
                  </button>
                );
              })}
              <button
                className="btn btn-ghost btn-icon btn-sm"
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage((p) => p + 1)}
                title="Page suivante"
              >
                <ChevronRight />
              </button>
              <button
                className="btn btn-ghost btn-icon btn-sm"
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage(totalPages)}
                title="Dernière page"
              >
                <ChevronsRight />
              </button>
            </div>
          </div>
        </>
      )}

      {/* ── User Detail Modal ── */}
      {selectedUser && (
        <div className="overlay" onClick={() => setSelectedUser(null)}>
          <div className="admin-users__modal" onClick={(e) => e.stopPropagation()}>
            <div className="admin-users__modal-header">
              <h3>Détails de l'utilisateur</h3>
              <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setSelectedUser(null)}>
                <X />
              </button>
            </div>
            <div className="admin-users__modal-body">
              <div className="admin-users__modal-avatar">
                {selectedUser.prenom?.[0]}{selectedUser.nom?.[0]}
              </div>
              <h4>{selectedUser.nom} {selectedUser.prenom}</h4>

              <div className="admin-users__modal-badges">
                <span className={`badge ${selectedUser.role === 'ROLE_ADMIN' ? 'badge-primary' : 'badge-accent'}`}>
                  {selectedUser.role === 'ROLE_ADMIN' ? 'Administrateur' : 'Étudiant'}
                </span>
                <span className={`badge ${selectedUser.actif ? 'badge-success' : 'badge-neutral'}`}>
                  {selectedUser.actif ? 'Actif' : 'Inactif'}
                </span>
              </div>

              <div className="admin-users__modal-details">
                <div className="admin-users__modal-field">
                  <span className="admin-users__modal-label">ID</span>
                  <span>{selectedUser.id}</span>
                </div>
                <div className="admin-users__modal-field">
                  <span className="admin-users__modal-label">Email</span>
                  <span>{selectedUser.email}</span>
                </div>
                <div className="admin-users__modal-field">
                  <span className="admin-users__modal-label">Nom</span>
                  <span>{selectedUser.nom}</span>
                </div>
                <div className="admin-users__modal-field">
                  <span className="admin-users__modal-label">Prénom</span>
                  <span>{selectedUser.prenom}</span>
                </div>
                <div className="admin-users__modal-field">
                  <span className="admin-users__modal-label">Rôle</span>
                  <span>{selectedUser.role === 'ROLE_ADMIN' ? 'Administrateur' : 'Étudiant'}</span>
                </div>
                <div className="admin-users__modal-field">
                  <span className="admin-users__modal-label">Statut</span>
                  <span>{selectedUser.actif ? 'Actif' : 'Inactif'}</span>
                </div>
              </div>

              <div className="admin-users__modal-actions">
                <button
                  className={`btn ${selectedUser.actif ? 'btn-secondary' : 'btn-primary'} btn-sm`}
                  onClick={() => {
                    setToggleTarget(selectedUser);
                    setSelectedUser(null);
                  }}
                  disabled={selectedUser.id === currentUser?.id}
                >
                  {selectedUser.actif ? <ToggleLeft /> : <ToggleRight />}
                  {selectedUser.actif ? 'Désactiver le compte' : 'Activer le compte'}
                </button>
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={() => {
                    setRoleTarget(selectedUser);
                    setSelectedUser(null);
                  }}
                  disabled={selectedUser.id === currentUser?.id}
                >
                  <ShieldCheck />
                  {selectedUser.role === 'ROLE_ADMIN' ? 'Retirer admin' : 'Promouvoir admin'}
                </button>
                <button
                  className="btn btn-danger btn-sm"
                  onClick={() => {
                    setDeleteTarget(selectedUser);
                    setSelectedUser(null);
                  }}
                  disabled={selectedUser.id === currentUser?.id}
                >
                  <Trash2 />
                  Supprimer
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ── Toggle Confirm ── */}
      <ConfirmDialog
        isOpen={!!toggleTarget}
        onClose={() => setToggleTarget(null)}
        onConfirm={handleToggleStatus}
        title={toggleTarget?.actif ? 'Désactiver le compte' : 'Activer le compte'}
        message={
          toggleTarget?.actif
            ? `Êtes-vous sûr de vouloir désactiver le compte de ${toggleTarget?.nom} ${toggleTarget?.prenom} ? L'utilisateur ne pourra plus se connecter.`
            : `Êtes-vous sûr de vouloir réactiver le compte de ${toggleTarget?.nom} ${toggleTarget?.prenom} ?`
        }
        loading={saving}
      />

      <ConfirmDialog
        isOpen={!!roleTarget}
        onClose={() => setRoleTarget(null)}
        onConfirm={handleToggleRole}
        title={roleTarget?.role === 'ROLE_ADMIN' ? 'Retirer le role admin' : 'Promouvoir admin'}
        message={
          roleTarget?.role === 'ROLE_ADMIN'
            ? `Retirer les droits admin de ${roleTarget?.nom} ${roleTarget?.prenom} ?`
            : `Donner les droits admin a ${roleTarget?.nom} ${roleTarget?.prenom} ?`
        }
        loading={saving}
      />

      <ConfirmDialog
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Supprimer l'utilisateur"
        message={`Supprimer le compte de ${deleteTarget?.nom} ${deleteTarget?.prenom} ?`}
        loading={saving}
      />
    </div>
  );
}
