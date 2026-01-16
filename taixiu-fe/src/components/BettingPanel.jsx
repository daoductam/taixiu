import { useState } from 'react';
import { gameApi } from '../services/api';
import useAuthStore from '../store/authStore';
import useGameStore from '../store/gameStore';
import './BettingPanel.css';

const QUICK_AMOUNTS = [10000, 50000, 100000, 500000, 1000000];

export default function BettingPanel({ game, userBalance }) {
  const [amount, setAmount] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  const { updateBalance } = useAuthStore();
  const { addUserBet, userBets } = useGameStore();
  
  const canBet = game?.status === 'BETTING' && game?.remainingSeconds > 0;
  
  const handleBet = async (betType) => {
    setError('');
    setSuccess('');
    
    const betAmount = parseFloat(amount);
    
    if (!betAmount || betAmount < 1000) {
      setError('Số tiền đặt cược tối thiểu là 1,000');
      return;
    }
    
    if (betAmount > userBalance) {
      setError('Số dư không đủ');
      return;
    }
    
    setIsLoading(true);
    
    try {
      const response = await gameApi.placeBet(betType, betAmount);
      const bet = response.data.data;
      addUserBet(bet);
      updateBalance(userBalance - betAmount);
      setSuccess(`Đặt cược ${formatNumber(betAmount)} vào ${betType === 'TAI' ? 'TÀI' : 'XỈU'} thành công!`);
      setAmount('');
    } catch (err) {
      setError(err.response?.data?.message || 'Đặt cược thất bại');
    } finally {
      setIsLoading(false);
    }
  };
  
  const formatNumber = (num) => {
    return new Intl.NumberFormat('vi-VN').format(num);
  };
  
  return (
    <div className="betting-panel">
      {/* Betting Stats */}
      <div className="betting-stats">
        <div className="stat tai">
          <div className="stat-label">TÀI (11-18)</div>
          <div className="stat-value">{formatNumber(game?.totalBetTai || 0)}</div>
          <div className="stat-count">{game?.countBetTai || 0} người chơi</div>
        </div>
        <div className="stat xiu">
          <div className="stat-label">XỈU (3-10)</div>
          <div className="stat-value">{formatNumber(game?.totalBetXiu || 0)}</div>
          <div className="stat-count">{game?.countBetXiu || 0} người chơi</div>
        </div>
      </div>
      
      {/* Amount Input */}
      <div className="amount-section">
        <input
          type="number"
          className="input amount-input"
          placeholder="Nhập số tiền đặt cược..."
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          disabled={!canBet || isLoading}
          min="1000"
        />
        
        <div className="quick-amounts">
          {QUICK_AMOUNTS.map((val) => (
            <button
              key={val}
              className="quick-amount-btn"
              onClick={() => setAmount(val.toString())}
              disabled={!canBet}
            >
              {val >= 1000000 ? `${val / 1000000}M` : `${val / 1000}K`}
            </button>
          ))}
          <button
            className="quick-amount-btn all-in"
            onClick={() => setAmount(Math.floor(userBalance).toString())}
            disabled={!canBet}
          >
            ALL IN
          </button>
        </div>
      </div>
      
      {/* Messages */}
      {error && <div className="bet-message error">{error}</div>}
      {success && <div className="bet-message success">{success}</div>}
      
      {/* User's Bets This Round */}
      {userBets.length > 0 && (
        <div className="user-bets">
          <span>Cược của bạn:</span>
          {userBets.map((bet, i) => (
            <span key={i} className={`user-bet ${bet.betType.toLowerCase()}`}>
              {bet.betType === 'TAI' ? 'TÀI' : 'XỈU'} {formatNumber(bet.amount)}
            </span>
          ))}
        </div>
      )}
      
      {/* Betting Buttons */}
      <div className="betting-buttons">
        <button
          className="btn btn-tai bet-btn"
          onClick={() => handleBet('TAI')}
          disabled={!canBet || isLoading}
        >
          {isLoading ? '...' : 'ĐẶT TÀI'}
          <span className="bet-hint">11-18</span>
        </button>
        
        <button
          className="btn btn-xiu bet-btn"
          onClick={() => handleBet('XIU')}
          disabled={!canBet || isLoading}
        >
          {isLoading ? '...' : 'ĐẶT XỈU'}
          <span className="bet-hint">3-10</span>
        </button>
      </div>
      
      {!canBet && (
        <div className="bet-closed">
          {game?.status === 'SPINNING' ? '🎲 Đang quay xúc xắc...' : '⏳ Chờ vòng mới...'}
        </div>
      )}
    </div>
  );
}
