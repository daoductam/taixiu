import { useState, useEffect } from 'react';
import { FaUsers, FaGift, FaPlus, FaTrash, FaArrowLeft, FaCoins, FaBan, FaCheck } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import { adminApi } from '../services/api';
import useAuthStore from '../store/authStore';
import './AdminPage.css';

export default function AdminPage() {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  
  const [activeTab, setActiveTab] = useState('users');
  const [users, setUsers] = useState([]);
  const [giftCodes, setGiftCodes] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  
  // Add balance form
  const [selectedUser, setSelectedUser] = useState(null);
  const [addAmount, setAddAmount] = useState('');
  
  // Create gift code form
  const [showCreateCode, setShowCreateCode] = useState(false);
  const [newCodeAmount, setNewCodeAmount] = useState('');
  const [newCodeExpiry, setNewCodeExpiry] = useState('');
  
  // Check admin access
  useEffect(() => {
    if (!user || user.role !== 'ADMIN') {
      navigate('/game');
    }
  }, [user, navigate]);
  
  // Load data
  useEffect(() => {
    loadData();
  }, [activeTab]);
  
  const loadData = async () => {
    setIsLoading(true);
    try {
      if (activeTab === 'users') {
        const response = await adminApi.getUsers(0, 50);
        console.log('Users response:', response.data);
        // Handle different response structures
        const data = response.data?.data;
        if (data?.content) {
          setUsers(data.content);
        } else if (Array.isArray(data)) {
          setUsers(data);
        } else if (data) {
          setUsers([data]);
        } else {
          setUsers([]);
        }
      } else {
        const response = await adminApi.getGiftCodes(0, 50);
        console.log('Gift codes response:', response.data);
        const data = response.data?.data;
        if (data?.content) {
          setGiftCodes(data.content);
        } else if (Array.isArray(data)) {
          setGiftCodes(data);
        } else if (data) {
          setGiftCodes([data]);
        } else {
          setGiftCodes([]);
        }
      }
    } catch (error) {
      console.error('Failed to load data:', error);
    } finally {
      setIsLoading(false);
    }
  };
  
  const handleAddBalance = async () => {
    if (!selectedUser || !addAmount) return;
    
    try {
      await adminApi.addBalance(selectedUser.id, parseFloat(addAmount));
      setSelectedUser(null);
      setAddAmount('');
      loadData();
    } catch (error) {
      alert(error.response?.data?.message || 'Failed to add balance');
    }
  };
  
  const handleToggleStatus = async (userId) => {
    try {
      await adminApi.toggleUserStatus(userId);
      loadData();
    } catch (error) {
      alert(error.response?.data?.message || 'Failed to toggle status');
    }
  };
  
  const handleCreateGiftCode = async (e) => {
    e.preventDefault();
    if (!newCodeAmount) return;
    
    try {
      const expiredAt = newCodeExpiry ? new Date(newCodeExpiry).toISOString() : null;
      await adminApi.createGiftCode(parseFloat(newCodeAmount), expiredAt);
      setShowCreateCode(false);
      setNewCodeAmount('');
      setNewCodeExpiry('');
      loadData();
    } catch (error) {
      alert(error.response?.data?.message || 'Failed to create gift code');
    }
  };
  
  const handleDeleteCode = async (id) => {
    if (!confirm('Bạn có chắc muốn xóa mã này?')) return;
    
    try {
      await adminApi.deleteGiftCode(id);
      loadData();
    } catch (error) {
      alert(error.response?.data?.message || 'Failed to delete gift code');
    }
  };
  
  const formatNumber = (num) => new Intl.NumberFormat('vi-VN').format(num || 0);
  const formatDate = (date) => new Date(date).toLocaleString('vi-VN');
  
  if (!user || user.role !== 'ADMIN') return null;
  
  return (
    <div className="admin-page">
      <header className="admin-header">
        <button className="back-btn" onClick={() => navigate('/game')}>
          <FaArrowLeft /> Quay lại Game
        </button>
        <h1>👑 Admin Dashboard</h1>
      </header>
      
      <div className="admin-tabs">
        <button 
          className={`tab ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          <FaUsers /> Quản lý Users
        </button>
        <button 
          className={`tab ${activeTab === 'codes' ? 'active' : ''}`}
          onClick={() => setActiveTab('codes')}
        >
          <FaGift /> Gift Codes
        </button>
      </div>
      
      <main className="admin-content">
        {activeTab === 'users' && (
          <div className="users-section">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Username</th>
                  <th>Email</th>
                  <th>Balance</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>{u.id}</td>
                    <td>{u.username}</td>
                    <td>{u.email}</td>
                    <td className="balance">{formatNumber(u.balance)}</td>
                    <td>
                      <span className={`badge ${u.role.toLowerCase()}`}>{u.role}</span>
                    </td>
                    <td>
                      <span className={`badge ${u.isActive ? 'active' : 'inactive'}`}>
                        {u.isActive ? 'Active' : 'Banned'}
                      </span>
                    </td>
                    <td className="actions">
                      <button 
                        className="action-btn add-balance"
                        onClick={() => setSelectedUser(u)}
                        title="Nạp tiền"
                      >
                        <FaCoins />
                      </button>
                      <button 
                        className={`action-btn ${u.isActive ? 'ban' : 'unban'}`}
                        onClick={() => handleToggleStatus(u.id)}
                        title={u.isActive ? 'Ban' : 'Unban'}
                      >
                        {u.isActive ? <FaBan /> : <FaCheck />}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        
        {activeTab === 'codes' && (
          <div className="codes-section">
            <button 
              className="btn btn-gold create-btn"
              onClick={() => setShowCreateCode(true)}
            >
              <FaPlus /> Tạo Gift Code
            </button>
            
            <table className="data-table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Số tiền</th>
                  <th>Trạng thái</th>
                  <th>Người dùng</th>
                  <th>Hết hạn</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {giftCodes.map((code) => (
                  <tr key={code.id}>
                    <td className="code-cell">{code.code}</td>
                    <td className="balance">{formatNumber(code.amount)}</td>
                    <td>
                      <span className={`badge ${code.isUsed ? 'used' : 'available'}`}>
                        {code.isUsed ? 'Đã dùng' : 'Còn hiệu lực'}
                      </span>
                    </td>
                    <td>{code.usedBy?.username || '-'}</td>
                    <td>{code.expiredAt ? formatDate(code.expiredAt) : 'Không giới hạn'}</td>
                    <td className="actions">
                      {!code.isUsed && (
                        <button 
                          className="action-btn delete"
                          onClick={() => handleDeleteCode(code.id)}
                        >
                          <FaTrash />
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
      
      {/* Add Balance Modal */}
      {selectedUser && (
        <div className="modal-overlay" onClick={() => setSelectedUser(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>Nạp tiền cho {selectedUser.username}</h3>
            <p>Số dư hiện tại: {formatNumber(selectedUser.balance)}</p>
            <input
              type="number"
              className="input"
              placeholder="Số tiền nạp..."
              value={addAmount}
              onChange={(e) => setAddAmount(e.target.value)}
            />
            <div className="modal-actions">
              <button className="btn btn-outline" onClick={() => setSelectedUser(null)}>
                Hủy
              </button>
              <button className="btn btn-gold" onClick={handleAddBalance}>
                Nạp tiền
              </button>
            </div>
          </div>
        </div>
      )}
      
      {/* Create Code Modal */}
      {showCreateCode && (
        <div className="modal-overlay" onClick={() => setShowCreateCode(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>Tạo Gift Code mới</h3>
            <form onSubmit={handleCreateGiftCode}>
              <input
                type="number"
                className="input"
                placeholder="Số tiền..."
                value={newCodeAmount}
                onChange={(e) => setNewCodeAmount(e.target.value)}
                required
              />
              <input
                type="datetime-local"
                className="input"
                placeholder="Ngày hết hạn (tùy chọn)"
                value={newCodeExpiry}
                onChange={(e) => setNewCodeExpiry(e.target.value)}
              />
              <div className="modal-actions">
                <button type="button" className="btn btn-outline" onClick={() => setShowCreateCode(false)}>
                  Hủy
                </button>
                <button type="submit" className="btn btn-gold">
                  Tạo code
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
