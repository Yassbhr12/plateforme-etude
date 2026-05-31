import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import AdminSidebar from './AdminSidebar';
import AdminHeader from './AdminHeader';
import './AdminLayout.css';

export default function AdminLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <div className={`admin-layout ${collapsed ? 'admin-layout--collapsed' : ''}`}>
      <AdminSidebar
        collapsed={collapsed}
        mobileOpen={mobileMenuOpen}
        onNavigate={() => setMobileMenuOpen(false)}
        onToggle={() => setCollapsed(!collapsed)}
      />
      {mobileMenuOpen && (
        <div className="admin-layout__backdrop" onClick={() => setMobileMenuOpen(false)} />
      )}
      <div className="admin-layout__main">
        <AdminHeader onMenuToggle={() => setMobileMenuOpen(!mobileMenuOpen)} />
        <main className="admin-layout__content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
