import React, { createContext, useContext, useEffect, useState, useRef, useCallback } from 'react';

interface WebSocketContextType {
  isConnected: boolean;
  lastMessage: any;
  sendMessage: (msg: any) => void;
  logs: Array<{ id: string; time: string; event: string; payload: any; type: 'info' | 'warn' | 'alert' | 'success' }>;
  clearLogs: () => void;
}

export const WebSocketContext = createContext<WebSocketContextType>({
  isConnected: false,
  lastMessage: null,
  sendMessage: () => {},
  logs: [],
  clearLogs: () => {},
});

export const WebSocketProvider: React.FC<{ children: React.ReactNode; userId?: string }> = ({ children, userId }) => {
  const [isConnected, setIsConnected] = useState(false);
  const [lastMessage, setLastMessage] = useState<any>(null);
  const [logs, setLogs] = useState<Array<{ id: string; time: string; event: string; payload: any; type: 'info' | 'warn' | 'alert' | 'success' }>>([]);
  const socketRef = useRef<WebSocket | null>(null);

  const targetUserId = userId || localStorage.getItem('app_user_id') || 'retail_user';

  const addLog = useCallback((event: string, payload: any, type: 'info' | 'warn' | 'alert' | 'success' = 'info') => {
    const now = new Date();
    const timeStr = now.toTimeString().split(' ')[0] + '.' + String(now.getMilliseconds()).padStart(3, '0');
    const newEntry = {
      id: String(Date.now()) + '-' + Math.random().toString(36).substr(2, 4),
      time: timeStr,
      event,
      payload,
      type
    };
    setLogs((prev) => [newEntry, ...prev.slice(0, 49)]);
  }, []);

  const connect = useCallback(() => {
    if (socketRef.current && (socketRef.current.readyState === WebSocket.OPEN || socketRef.current.readyState === WebSocket.CONNECTING)) {
      return;
    }

    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsHost = window.location.hostname || 'localhost';
    const wsUrl = wsProtocol + '//' + wsHost + ':8080/api/v1/ws/live/' + targetUserId;

    try {
      const ws = new WebSocket(wsUrl);
      socketRef.current = ws;

      ws.onopen = () => {
        setIsConnected(true);
        addLog('WS_CONNECTED', { endpoint: wsUrl, status: 'OPEN' }, 'success');
      };

      ws.onmessage = (event) => {
        try {
          const parsed = JSON.parse(event.data);
          setLastMessage(parsed);
          const evName = parsed.event || parsed.type || 'MESSAGE';
          const isAlert = evName.includes('ALERT') || evName.includes('TAMPER') || evName.includes('CRASH') || evName.includes('CRITICAL');
          const isSuccess = evName.includes('SUCCESS') || evName.includes('SETTLED') || evName.includes('DROP');
          const logType = isAlert ? 'alert' : (isSuccess ? 'success' : 'info');
          addLog(evName, parsed, logType);
        } catch {
          setLastMessage(event.data);
          addLog('RAW_MESSAGE', event.data, 'info');
        }
      };

      ws.onerror = () => {
        setIsConnected(false);
      };

      ws.onclose = () => {
        setIsConnected(false);
        addLog('WS_DISCONNECTED', { endpoint: wsUrl }, 'warn');
        setTimeout(() => {
          connect();
        }, 5000);
      };
    } catch {
      setIsConnected(false);
    }
  }, [targetUserId, addLog]);

  useEffect(() => {
    connect();
    return () => {
      if (socketRef.current) {
        socketRef.current.close();
      }
    };
  }, [connect]);

  const sendMessage = useCallback((msg: any) => {
    if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN) {
      const data = typeof msg === 'string' ? msg : JSON.stringify(msg);
      socketRef.current.send(data);
      addLog('OUTGOING_MESSAGE', msg, 'info');
    }
  }, [addLog]);

  const clearLogs = useCallback(() => {
    setLogs([]);
  }, []);

  return (
    <WebSocketContext.Provider value={{ isConnected, lastMessage, sendMessage, logs, clearLogs }}>
      {children}
    </WebSocketContext.Provider>
  );
};
