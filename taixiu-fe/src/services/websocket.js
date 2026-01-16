import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = 'http://localhost:8080/ws';

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = {};
  }

  connect(onConnect, onError) {
    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
      onConnect: () => {
        console.log('WebSocket connected');
        this.connected = true;
        if (onConnect) onConnect();
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        if (onError) onError(frame);
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected');
        this.connected = false;
      },
    });

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.connected = false;
    }
  }

  subscribe(destination, callback) {
    if (!this.connected || !this.client) {
      console.warn('WebSocket not connected, cannot subscribe');
      return null;
    }

    const subscription = this.client.subscribe(destination, (message) => {
      try {
        const data = JSON.parse(message.body);
        callback(data);
      } catch (e) {
        callback(message.body);
      }
    });

    this.subscriptions[destination] = subscription;
    return subscription;
  }

  unsubscribe(destination) {
    if (this.subscriptions[destination]) {
      this.subscriptions[destination].unsubscribe();
      delete this.subscriptions[destination];
    }
  }

  send(destination, body) {
    if (!this.connected || !this.client) {
      console.warn('WebSocket not connected, cannot send');
      return;
    }

    this.client.publish({
      destination,
      body: JSON.stringify(body),
    });
  }

  // Game specific methods
  subscribeToGame(callback) {
    return this.subscribe('/topic/game', callback);
  }

  subscribeToChat(callback) {
    return this.subscribe('/topic/chat', callback);
  }

  sendChatMessage(content) {
    this.send('/app/chat.send', { content });
  }
}

export const wsService = new WebSocketService();
export default wsService;
