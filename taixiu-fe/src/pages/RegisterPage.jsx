import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaUser, FaEnvelope, FaLock, FaDice } from 'react-icons/fa';
import useAuthStore from '../store/authStore';
import './Auth.css';

export default function RegisterPage() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [localError, setLocalError] = useState('');
  
  const { register, isLoading, error, clearError } = useAuthStore();
  const navigate = useNavigate();
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLocalError('');
    clearError();
    
    if (password !== confirmPassword) {
      setLocalError('Mật khẩu không khớp');
      return;
    }
    
    if (password.length < 6) {
      setLocalError('Mật khẩu phải có ít nhất 6 ký tự');
      return;
    }
    
    const result = await register(username, email, password);
    if (result.success) {
      navigate('/game');
    }
  };
  
  return (
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-header">
          <FaDice className="auth-logo" />
          <h1 className="gold-text">TÀI XỈU</h1>
          <p>Tạo tài khoản mới</p>
        </div>
        
        <form className="auth-form" onSubmit={handleSubmit}>
          {(error || localError) && (
            <div className="auth-error">
              {error || localError}
            </div>
          )}
          
          <div className="input-group">
            <FaUser className="input-icon" />
            <input
              type="text"
              className="input"
              placeholder="Tên đăng nhập"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          
          <div className="input-group">
            <FaEnvelope className="input-icon" />
            <input
              type="email"
              className="input"
              placeholder="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          
          <div className="input-group">
            <FaLock className="input-icon" />
            <input
              type="password"
              className="input"
              placeholder="Mật khẩu"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          
          <div className="input-group">
            <FaLock className="input-icon" />
            <input
              type="password"
              className="input"
              placeholder="Xác nhận mật khẩu"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </div>
          
          <button type="submit" className="btn btn-gold auth-btn" disabled={isLoading}>
            {isLoading ? 'Đang xử lý...' : 'Đăng ký'}
          </button>
        </form>
        
        <p className="auth-link">
          Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
        </p>
        
        <div className="auth-bonus">
          🎁 Đăng ký nhận ngay <span className="gold-text">10,000</span> tiền ảo!
        </div>
      </div>
    </div>
  );
}
