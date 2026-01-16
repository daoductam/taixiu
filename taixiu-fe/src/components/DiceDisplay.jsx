import { useState, useEffect } from 'react';
import './DiceDisplay.css';

const DICE_FACES = ['⚀', '⚁', '⚂', '⚃', '⚄', '⚅'];

export default function DiceDisplay({ game }) {
  const [countdown, setCountdown] = useState(null);
  
  // Local countdown effect
  useEffect(() => {
    if (!game) return;
    
    // Initialize countdown from server
    if (game.remainingSeconds !== null && game.remainingSeconds !== undefined) {
      setCountdown(game.remainingSeconds);
    }
  }, [game?.remainingSeconds, game?.sessionCode]);
  
  // Decrement countdown every second
  useEffect(() => {
    if (countdown === null || countdown <= 0) return;
    
    const timer = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    
    return () => clearInterval(timer);
  }, [countdown]);
  
  if (!game) {
    return (
      <div className="dice-container loading">
        <p>Đang tải game...</p>
      </div>
    );
  }
  
  const isSpinning = game.status === 'SPINNING';
  const isCompleted = game.status === 'COMPLETED';
  const isBetting = game.status === 'BETTING';
  
  const dice1 = game.dice1 || 1;
  const dice2 = game.dice2 || 1;
  const dice3 = game.dice3 || 1;
  
  return (
    <div className="dice-container">
      {/* Session Code */}
      <div className="session-code">
        Phiên #{game.sessionCode}
      </div>
      
      {/* Status Banner */}
      <div className={`game-status ${game.status.toLowerCase()}`}>
        {isBetting && (
          <>
            <span className="status-icon">🎰</span>
            <span>Đang đặt cược</span>
            {countdown !== null && (
              <span className={`countdown ${countdown <= 5 ? 'urgent' : ''}`}>{countdown}s</span>
            )}
          </>
        )}
        {isSpinning && (
          <>
            <span className="status-icon animate-spin">🎲</span>
            <span>Đang quay...</span>
          </>
        )}
        {isCompleted && (
          <>
            <span className="status-icon">🎯</span>
            <span>Kết quả: <strong className={game.result === 'TAI' ? 'tai' : 'xiu'}>
              {game.result === 'TAI' ? 'TÀI' : 'XỈU'}
            </strong></span>
          </>
        )}
      </div>
      
      {/* Dice Display */}
      <div className="dice-row">
        <div className={`dice ${isSpinning ? 'rolling' : ''} ${isCompleted ? 'show' : ''}`}>
          {isCompleted ? DICE_FACES[dice1 - 1] : '?'}
        </div>
        <div className={`dice ${isSpinning ? 'rolling' : ''} ${isCompleted ? 'show' : ''}`}>
          {isCompleted ? DICE_FACES[dice2 - 1] : '?'}
        </div>
        <div className={`dice ${isSpinning ? 'rolling' : ''} ${isCompleted ? 'show' : ''}`}>
          {isCompleted ? DICE_FACES[dice3 - 1] : '?'}
        </div>
      </div>
      
      {/* Total */}
      {isCompleted && (
        <div className={`total-display ${game.result === 'TAI' ? 'tai' : 'xiu'}`}>
          <span className="total-label">Tổng:</span>
          <span className="total-value">{game.total}</span>
        </div>
      )}
      
      {/* Timer Bar */}
      {isBetting && countdown !== null && (
        <div className="timer-bar">
          <div 
            className="timer-progress" 
            style={{ width: `${(countdown / 20) * 100}%` }}
          />
        </div>
      )}
    </div>
  );
}
