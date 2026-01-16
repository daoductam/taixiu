import { create } from 'zustand';

export const useChatStore = create((set, get) => ({
  messages: [],
  isLoading: false,
  
  setMessages: (messages) => set({ messages }),
  
  addMessage: (message) => {
    const { messages } = get();
    // Keep only last 100 messages
    const updatedMessages = [...messages, message].slice(-100);
    set({ messages: updatedMessages });
  },
  
  setLoading: (isLoading) => set({ isLoading }),
}));

export default useChatStore;
