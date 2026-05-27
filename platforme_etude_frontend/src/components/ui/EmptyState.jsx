import { Inbox } from 'lucide-react';
import './EmptyState.css';

export default function EmptyState({ icon, title, description, action }) {
  return (
    <div className="empty-state">
      <div className="empty-state__icon">
        {icon || <Inbox />}
      </div>
      <h3 className="empty-state__title">{title || 'Aucun élément'}</h3>
      {description && <p className="empty-state__desc">{description}</p>}
      {action && <div className="empty-state__action">{action}</div>}
    </div>
  );
}
