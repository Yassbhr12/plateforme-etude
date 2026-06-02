import { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getGroupeMessages, sendGroupeMessage } from '../api/messageService';
import { getMyGroupes } from '../api/groupeService';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import EmptyState from '../components/ui/EmptyState';
import { Send, ArrowLeft, AlertCircle, Users } from 'lucide-react';
import './GroupeChat.css';

export default function GroupeChat() {
  const { id } = useParams();
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [groupeName, setGroupeName] = useState('');
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [newMessage, setNewMessage] = useState('');
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const messagesEndRef = useRef(null);

  const loadMessages = useCallback(async () => {
    try {
      const data = await getGroupeMessages(id);
      setMessages(data);
      setNotFound(false);
    } catch (err) {
      console.error(err);
      if (err.response?.status === 404 || err.response?.status === 403) {
        setNotFound(true);
      } else {
        setError(err.response?.data?.message || err.response?.data?.error || 'Impossible de charger les messages');
      }
    }
    finally { setLoading(false); }
  }, [id]);

  useEffect(() => {
    // Load group name
    const loadGroupName = async () => {
      try {
        const groupes = await getMyGroupes();
        const groupe = groupes.find((g) => String(g.id) === String(id));
        if (groupe) {
          setGroupeName(groupe.nom);
        }
      } catch (err) {
        console.error('Could not load group name:', err);
      }
    };
    loadGroupName();
    loadMessages();
  }, [id, loadMessages]);

  useEffect(() => {
    if (notFound) return;
    const interval = setInterval(loadMessages, 10000);
    return () => clearInterval(interval);
  }, [id, notFound, loadMessages]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!newMessage.trim()) return;
    setSending(true);
    setError('');
    try {
      const msg = await sendGroupeMessage(id, { contenu: newMessage.trim() });
      setMessages([...messages, msg]);
      setNewMessage('');
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || err.response?.data?.error || "Impossible d'envoyer le message");
    }
    finally { setSending(false); }
  };

  if (loading) return <div className="page"><LoadingSpinner size="lg" text="Chargement du chat..." /></div>;

  if (notFound) {
    return (
      <div className="page">
        <EmptyState
          icon={<Users />}
          title="Groupe introuvable"
          description="Ce groupe n'existe pas ou vous n'y avez pas accès."
          action={<Link to="/groupes" className="btn btn-primary"><ArrowLeft /> Retour aux groupes</Link>}
        />
      </div>
    );
  }

  return (
    <div className="chat-page">
      <div className="chat-header">
        <Link to="/groupes" className="btn btn-ghost btn-icon btn-sm"><ArrowLeft /></Link>
        <h2>{groupeName || 'Chat du groupe'}</h2>
      </div>

      {error && <div className="alert alert-error"><AlertCircle /><span>{error}</span></div>}

      <div className="chat-messages">
        {messages.length === 0 ? (
          <div className="chat-empty">Aucun message. Soyez le premier à écrire !</div>
        ) : (
          messages.map((msg) => {
            const isOwn = Number(msg.userId) === Number(user?.id);
            return (
              <div key={msg.id} className={`chat-message ${isOwn ? 'own' : ''}`}>
                {!isOwn && <div className="chat-message__avatar">{msg.userNom?.[0] || '?'}</div>}
                <div className="chat-message__bubble">
                  {!isOwn && <span className="chat-message__author">{msg.userNom}</span>}
                  <p className="chat-message__text">{msg.contenu}</p>
                  <span className="chat-message__time">
                    {new Date(msg.dateEnvoi).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </div>

      <form className="chat-input" onSubmit={handleSend}>
        <input
          type="text"
          className="form-input"
          placeholder="Écrire un message..."
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          disabled={sending}
          maxLength={2000}
        />
        <button type="submit" className="btn btn-primary btn-icon" disabled={sending || !newMessage.trim()}>
          <Send />
        </button>
      </form>
    </div>
  );
}
