import './GameHistory.css';

export default function GameHistory({ history }) {
  if (!history || history.length === 0) {
    return (
      <div className="game-history">
        <div className="history-label">Lịch sử</div>
        <div className="history-empty">Chưa có kết quả</div>
      </div>
    );
  }
  
  return (
    <div className="game-history">
      <div className="history-label">Lịch sử 10 ván gần nhất</div>
      <div className="history-items">
        {history.slice(0, 10).map((game, index) => (
          <div 
            key={game.id || index} 
            className={`history-item ${game.result?.toLowerCase()}`}
            title={`Phiên ${game.sessionCode}: ${game.total} điểm`}
          >
            <span className="history-total">{game.total}</span>
            <span className="history-result">
              {game.result === 'TAI' ? 'T' : 'X'}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
