const WebSocket = require("ws");
const readline = require("readline");

const ws = new WebSocket("ws://127.0.0.1:5810/nt/dashboard");

ws.on("open", () => {
  console.log("✅ Conectado ao NT3 WS Bridge");
  console.log("F = FORWARD | R = REVERSE | S = STOP");
});

ws.on("error", err => {
  console.error("❌ Erro WS:", err.message);
});

function sendCommand(cmd) {
  ws.send(JSON.stringify({
    action: "put",
    table: "StreamDeck",
    key: "motorCommand",
    value: cmd
  }));
  console.log("📤 Enviado:", cmd);
}

readline.emitKeypressEvents(process.stdin);
process.stdin.setRawMode(true);

process.stdin.on("keypress", (str, key) => {
  if (key.ctrl && key.name === "c") process.exit();

  if (key.name === "f") sendCommand("FORWARD");
  if (key.name === "r") sendCommand("REVERSE");
  if (key.name === "s") sendCommand("STOP");
});
