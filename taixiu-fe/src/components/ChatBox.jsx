import { useState, useRef, useEffect } from 'react';
import { FaTimes, FaPaperPlane, FaRobot } from 'react-icons/fa';
import useChatStore from '../store/chatStore';
import { chatApi } from '../services/api';
import './ChatBox.css';

export default function ChatBox({ onClose }) {
  const [message, setMessage] = useState('');
  const [isSending, setIsSending] = useState(false);
  const messagesEndRef = useRef(null);
  
  const { messages } = useChatStore();
  
  // Auto scroll to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);
  
  const handleSend = async (e) => {
    e.preventDefault();
    if (!message.trim() || isSending) return;
    
    setIsSending(true);
    try {
      await chatApi.sendMessage(message.trim());
      setMessage('');
    } catch (error) {
      console.error('Failed to send message:', error);
    } finally {
      setIsSending(false);
    }
  };
  
  const formatTime = (dateStr) => {
    const date = new Date(dateStr);
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  };
  
  return (
    <div className="chat-sidebar">
      <div className="chat-header">
        <h3>💬 Chat</h3>
        <button className="close-btn" onClick={onClose}>
          <FaTimes />
        </button>
      </div>
      
      <div className="chat-hint">
        💡 Gõ <code>@bot</code> hoặc <code>@ai</code> để hỏi AI
      </div>
      
      <div className="chat-messages">
        {messages.map((msg, index) => (
          <div 
            key={msg.id || index} 
            className={`chat-message ${msg.isAi ? 'ai' : 'user'}`}
          >
            <div className="message-header">
              <span className="message-username">
                {msg.isAi && <FaRobot className="ai-icon" />}
                {msg.username || 'Anonymous'}
              </span>
              <span className="message-time">{formatTime(msg.createdAt)}</span>
            </div>
            <div className="message-content">{msg.content}</div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
      
      <form className="chat-input-form" onSubmit={handleSend}>
        <input
          type="text"
          className="input chat-input"
          placeholder="Nhập tin nhắn..."
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          disabled={isSending}
        />
        <button 
          type="submit" 
          className="send-btn"
          disabled={!message.trim() || isSending}
        >
          <FaPaperPlane />
        </button>
      </form>
    </div>
  );
}
