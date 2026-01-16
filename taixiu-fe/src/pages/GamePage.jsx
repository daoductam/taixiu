import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaSignOutAlt, FaGift, FaComments, FaTrophy, FaCoins } from 'react-icons/fa';
import useAuthStore from '../store/authStore';
import useGameStore from '../store/gameStore';
import useChatStore from '../store/chatStore';
import { gameApi, chatApi, userApi } from '../services/api';
import wsService from '../services/websocket';
import DiceDisplay from '../components/DiceDisplay';
import BettingPanel from '../components/BettingPanel';
import GameHistory from '../components/GameHistory';
import ChatBox from '../components/ChatBox';
import './GamePage.css';

export default function GamePage() {
  const navigate = useNavigate();
  const { user, logout, updateBalance, refreshUser } = useAuthStore();
  const { currentGame, setCurrentGame, updateFromWebSocket } = useGameStore();
  const { setMessages, addMessage } = useChatStore();
  
  const [showChat, setShowChat] = useState(false);
  const [showGiftCode, setShowGiftCode] = useState(false);
  const [giftCode, setGiftCode] = useState('');
  const [giftMessage, setGiftMessage] = useState('');
  
  // Load initial data
  useEffect(() => {
    const loadData = async () => {
      try {
        const [gameRes, chatRes] = await Promise.all([
          gameApi.getCurrent(),
          chatApi.getMessages()
        ]);
        
        if (gameRes.data.data) {
          setCurrentGame(gameRes.data.data);
        }
        if (chatRes.data.data) {
          setMessages(chatRes.data.data.reverse());
        }
      } catch (error) {
        console.error('Failed to load data:', error);
      }
    };
    
    loadData();
  }, [setCurrentGame, setMessages]);
  
  // WebSocket connection
  useEffect(() => {
    wsService.connect(
      () => {
        wsService.subscribeToGame((data) => {
          updateFromWebSocket(data);
          // Refresh user balance when game completes
          if (data.status === 'COMPLETED') {
            setTimeout(() => refreshUser(), 1000);
          }
        });
        
        wsService.subscribeToChat((message) => {
          addMessage(message);
        });
      },
      (error) => {
        console.error('WebSocket error:', error);
      }
    );
    
    return () => {
      wsService.disconnect();
    };
  }, [updateFromWebSocket, addMessage, refreshUser]);
  
  const handleLogout = () => {
    logout();
    navigate('/login');
  };
  
  const handleRedeemCode = async (e) => {
    e.preventDefault();
    setGiftMessage('');
    
    try {
      const response = await userApi.redeemCode(giftCode);
      const newBalance = response.data.data.balance;
      updateBalance(newBalance);
      setGiftMessage('🎉 Nạp tiền thành công!');
      setGiftCode('');
      setTimeout(() => setShowGiftCode(false), 2000);
    } catch (error) {
      setGiftMessage(error.response?.data?.message || 'Mã không hợp lệ');
    }
  };
  
  const formatBalance = (balance) => {
    return new Intl.NumberFormat('vi-VN').format(balance || 0);
  };
  
  if (!user) {
    navigate('/login');
    return null;
  }
  
  return (
    <div className="game-page">
      {/* Header */}
      <header className="game-header">
        <div className="header-left">
          <h1 className="logo gold-text">🎲 TÀI XỈU</h1>
        </div>
        
        <div className="header-center">
          <div className="user-balance">
            <FaCoins className="coin-icon" />
            <span className="balance-amount">{formatBalance(user.balance)}</span>
          </div>
        </div>
        
        <div className="header-right">
          <button 
            className="header-btn" 
            onClick={() => setShowGiftCode(!showGiftCode)}
            title="Nhập mã nạp tiền"
          >
            <FaGift />
          </button>
          <button 
            className="header-btn" 
            onClick={() => setShowChat(!showChat)}
            title="Chat"
          >
            <FaComments />
          </button>
          <span className="username">{user.username}</span>
          {user.role === 'ADMIN' && (
            <button 
              className="header-btn admin-btn" 
              onClick={() => navigate('/admin')}
              title="Admin Panel"
            >
              👑
            </button>
          )}
          <button className="header-btn logout-btn" onClick={handleLogout}>
            <FaSignOutAlt />
          </button>
        </div>
      </header>
      
      {/* Gift Code Modal */}
      {showGiftCode && (
        <div className="modal-overlay" onClick={() => setShowGiftCode(false)}>
          <div className="modal gift-modal" onClick={(e) => e.stopPropagation()}>
            <h3><FaGift /> Nhập Mã Nạp Tiền</h3>
            <form onSubmit={handleRedeemCode}>
              <input
                type="text"
                className="input"
                placeholder="Nhập mã gift code..."
                value={giftCode}
                onChange={(e) => setGiftCode(e.target.value)}
              />
              {giftMessage && (
                <p className={giftMessage.includes('thành công') ? 'success' : 'error'}>
                  {giftMessage}
                </p>
              )}
              <button type="submit" className="btn btn-gold">
                Xác nhận
              </button>
            </form>
          </div>
        </div>
      )}
      
      {/* Main Game Area */}
      <main className="game-main">
        <div className="game-container">
          {/* Game History */}
          <GameHistory history={currentGame?.history || []} />
          
          {/* Dice Display */}
          <DiceDisplay game={currentGame} />
          
          {/* Betting Panel */}
          <BettingPanel game={currentGame} userBalance={user.balance} />
        </div>
      </main>
      
      {/* Chat Sidebar */}
      {showChat && (
        <ChatBox onClose={() => setShowChat(false)} />
      )}
    </div>
  );
}
