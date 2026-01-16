import { create } from 'zustand';

export const useGameStore = create((set, get) => ({
  currentGame: null,
  history: [],
  isLoading: false,
  userBets: [],
  
  setCurrentGame: (game) => set({ currentGame: game }),
  
  setHistory: (history) => set({ history }),
  
  addUserBet: (bet) => {
    const { userBets } = get();
    set({ userBets: [...userBets, bet] });
  },
  
  clearUserBets: () => set({ userBets: [] }),
  
  updateFromWebSocket: (data) => {
    set({ currentGame: data });
    
    // If game completed, clear user bets for next round
    if (data.status === 'COMPLETED') {
      setTimeout(() => {
        set({ userBets: [] });
      }, 3000);
    }
  },
}));

export default useGameStore;
