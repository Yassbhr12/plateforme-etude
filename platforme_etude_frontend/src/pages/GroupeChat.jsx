import { useState, useEffect, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getGroupeMessages, sendGroupeMessage } from '../api/messageService';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { Send, ArrowLeft, AlertCircle } from 'lucide-react';
import './GroupeChat.css';

export default function GroupeChat() {
  const { id } = useParams();
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newMessage, setNewMessage] = useState('');
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const messagesEndRef = useRef(null);

  useEffect(() => { loadMessages(); }, [id]);

  useEffect(() => {
    const interval = setInterval(loadMessages, 10000);
    return () => clearInterval(interval);
  }, [id]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const loadMessages = async () => {
    try {
      const data = await getGroupeMessages(id);
      setMessages(data);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || err.response?.data?.error || 'Impossible de charger les messages');
    }
    finally { setLoading(false); }
  };

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
      setError(err.response?.data?.message || err.response?.data?.error || 'Impossible d envoyer le message');
    }
    finally { setSending(false); }
  };

  if (loading) return <div className="page"><LoadingSpinner size="lg" text="Chargement du chat..." /></div>;

  return (
    <div className="chat-page">
      <div className="chat-header">
        <Link to="/groupes" className="btn btn-ghost btn-icon btn-sm"><ArrowLeft /></Link>
        <h2>Chat du groupe</h2>
      </div>

      {error && <div className="alert alert-error"><AlertCircle /><span>{error}</span></div>}

      <div className="chat-messages">
        {messages.length === 0 ? (
          <div className="chat-empty">Aucun message. Soyez le premier a écrire !</div>
        ) : (
          messages.map((msg) => {
            const isOwn = msg.userId === user?.id;
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
