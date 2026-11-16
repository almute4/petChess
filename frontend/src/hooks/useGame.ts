import { useEffect, useRef, useState } from "react";
import { Client } from "@stomp/stompjs";
import { getErrorMessage, refreshTokens, tokenStorage } from "../api/client";
import { getGame, type GameState, type MoveInput } from "../api/games";

export function useGame(id: number) {
  const [game, setGame] = useState<GameState | null>(null);
  const [connected, setConnected] = useState(false);
  const [pending, setPending] = useState(false);
  const [error, setError] = useState("");
  const clientRef = useRef<Client | null>(null);
  const sending = useRef(false);

  useEffect(() => {
    let active = true;
    let generation = 0;
    let refreshing = false;
    let refreshAgain = false;
    setGame(null);
    setError("");
    setConnected(false);
    sending.current = false;
    setPending(false);

    // Serialize reloads so an older HTTP response cannot overwrite newer state.
    const reload = async () => {
      refreshAgain = true;
      if (refreshing) return;
      refreshing = true;
      try {
        while (active && refreshAgain) {
          refreshAgain = false;
          const currentGeneration = generation;
          const state = await getGame(id);
          if (active && currentGeneration === generation) {
            setGame(state);
            setConnected(client.connected);
            sending.current = false;
            setPending(false);
          }
        }
      } catch (cause) {
        if (active) setError(getErrorMessage(cause));
      } finally {
        refreshing = false;
      }
    };

    const client = new Client({
      brokerURL: `${window.location.protocol === "https:" ? "wss:" : "ws:"}//${window.location.host}/ws`,
      reconnectDelay: 3000,
      connectionTimeout: 10000,
      beforeConnect: async () => {
        try {
          if (!await refreshTokens()) {
            if (active) {
              setError("Session expired. Please sign in again.");
              tokenStorage.clear();
            }
            void client.deactivate();
            return;
          }
        } catch {
          // A network outage must preserve the session and allow STOMP to retry.
          if (active) setError("Cannot reach server. Reconnecting…");
        }
        client.connectHeaders = { Authorization: `Bearer ${tokenStorage.getAccess()}` };
      },
      onConnect: () => {
        if (!active) return;
        generation++;
        client.subscribe(`/topic/games/${id}`, () => { void reload(); });
        client.subscribe("/user/queue/errors", (message) => {
          if (!active) return;
          setError(JSON.parse(message.body).message ?? "Move rejected");
          sending.current = false;
          setPending(false);
        });
        client.subscribe("/user/queue/game-state", () => { void reload(); });
        setError("");
        // The reply confirms preceding subscriptions are active before the HTTP snapshot.
        client.publish({ destination: `/app/games/${id}/sync`, body: "{}" });
      },
      onWebSocketClose: () => {
        generation++;
        if (active) {
          setConnected(false);
          sending.current = false;
          setPending(false);
        }
      },
      onStompError: (frame) => {
        if (active) setError(frame.headers.message ?? "Connection rejected");
      },
    });
    clientRef.current = client;
    void reload();
    client.activate();
    return () => {
      active = false;
      clientRef.current = null;
      void client.deactivate();
    };
  }, [id]);

  const sendMove = (move: MoveInput) => {
    if (!connected || !clientRef.current?.connected || sending.current) return;
    sending.current = true;
    setPending(true);
    setError("");
    clientRef.current.publish({
      destination: `/app/games/${id}/move`,
      body: JSON.stringify(move),
    });
  };

  return { game, connected, pending, error, setError, sendMove };
}
