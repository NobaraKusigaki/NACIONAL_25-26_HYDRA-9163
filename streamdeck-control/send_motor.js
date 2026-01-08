// send_motor.js
const WebSocket = require("ws");

const COMMAND = process.argv[2]; // FORWARD | REVERSE | STOP

if (!COMMAND) {
  console.log("Use: node send_motor.js FORWARD | REVERSE | STOP");
  process.exit(1);
}

const ws = new WebSocket("ws://localhost:5810/nt/dashboard");

ws.on("open", () => {
  ws.send(JSON.stringify({
    action: "put",
    table: "StreamDeck",
    key: "motorCommand",
    value: COMMAND
  }));
  console.log("📡 Enviado:", COMMAND);
  setTimeout(() => ws.close(), 200);
});
