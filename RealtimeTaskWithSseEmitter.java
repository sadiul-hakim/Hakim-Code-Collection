// SSE (Server-Sent Events) gives that real-time WebSocket-like feel but with a simpler approach—great for one-way updates
/** 
 * ============================== 
 * SSE (SseEmitter) vs WebSocket 
 * ============================== 
 * 
 * 1. Connection Type:
 *    - SSE: One-way communication (server to client only).
 *    - WebSocket: Bi-directional communication (both client and server can send messages).
 * 
 * 2. Transport:
 *    - SSE: Uses HTTP and keeps the connection open via EventSource.
 *    - WebSocket: Uses a persistent TCP connection over ws:// or wss://.
 * 
 * 3. Use Case:
 *    - SSE: Best for real-time updates like notifications, stock prices, or live scores.
 *    - WebSocket: Ideal for interactive applications like chat, multiplayer games, or collaborative tools.
 * 
 * 4. Performance:
 *    - SSE: Lightweight and easy to implement but limited to text-based data.
 *    - WebSocket: More efficient for high-frequency updates and supports binary data.
 * 
 * 5. Scalability:
 *    - SSE: Uses a new HTTP connection per client, which can be resource-intensive.
 *    - WebSocket: More efficient as a single connection handles multiple messages.
 * 
 * 6. Reconnection:
 *    - SSE: Automatically reconnects on failure but only works over HTTP/HTTPS.
 *    - WebSocket: Requires manual reconnection handling but works over TCP.
 * 
 * 7. Browser Support:
 *    - SSE: Works in most modern browsers but limited to HTTP connections.
 *    - WebSocket: Supported universally, including mobile and desktop.
 * 
 * 8. Security:
 *    - SSE: Uses standard HTTP security (CORS, CSRF, etc.).
 *    - WebSocket: Requires additional security handling (authentication, origin checks, etc.).
 * 
 * When to Use What? 
 * -----------------
 * - Use SSE if you only need real-time server-to-client updates (easier to implement).
 * - Use WebSocket if you need full-duplex communication (e.g., live chat, games).
 */


import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/typing-race")
public class RealtimeTaskWithSseEmitter {

    private final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();
    private final Map<String, Integer> progressMap = new ConcurrentHashMap<>();

    @GetMapping("/subscribe/{playerId}")
    public SseEmitter subscribe(@PathVariable String playerId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        clients.put(playerId, emitter);
        emitter.onCompletion(() -> clients.remove(playerId));
        emitter.onTimeout(() -> clients.remove(playerId));
        return emitter;
    }

    @PostMapping("/progress/{playerId}/{progress}")
    public void updateProgress(@PathVariable String playerId, @PathVariable int progress) {
        progressMap.put(playerId, progress);
        broadcastProgress();
    }

    private void broadcastProgress() {
        for (Map.Entry<String, SseEmitter> entry : clients.entrySet()) {
            try {
                entry.getValue().send(progressMap);
            } catch (IOException e) {
                entry.getValue().complete();
                clients.remove(entry.getKey());
            }
        }
    }
}


/*

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Typing Race</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            text-align: center;
            background-color: #f4f4f4;
            padding: 20px;
        }

        h1 {
            color: #333;
        }

        input {
            padding: 10px;
            font-size: 16px;
            border: 2px solid #007bff;
            border-radius: 5px;
            width: 200px;
        }

        button {
            padding: 10px 20px;
            font-size: 16px;
            background-color: #007bff;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            transition: background 0.3s;
        }

        button:hover {
            background-color: #0056b3;
        }

        #progress {
            margin-top: 20px;
            padding: 10px;
            background: white;
            border-radius: 5px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            display: inline-block;
            min-width: 200px;
        }
    </style>
    <script>
        let playerId = "player" + Math.floor(Math.random() * 1000);
        let eventSource = new EventSource(`/typing-race/subscribe/${playerId}`);

        eventSource.onmessage = function(event) {
            let progress = JSON.parse(event.data);
            let progressDisplay = document.getElementById("progress");
            progressDisplay.innerHTML = "";
            for (let player in progress) {
                progressDisplay.innerHTML += `<p>${player}: ${progress[player]}%</p>`;
            }
        };

        function updateProgress() {
            let typedPercentage = document.getElementById("progressInput").value;
            fetch(`/typing-race/progress/${playerId}/${typedPercentage}`, { method: "POST" });
        }
    </script>
</head>
<body>
<h1>Typing Race</h1>
<p>Type and update your progress:</p>
<input type="number" id="progressInput" placeholder="Enter progress %" />
<button onclick="updateProgress()">Update Progress</button>
<h2>Live Progress:</h2>
<div id="progress"></div>
</body>
</html>


*/
