const WebSocket = require("ws");
const ws = new WebSocket("ws://127.0.0.1:5810");

function sendToggle() {
  ws.send(JSON.stringify({ action: "press" }));
  console.log("📤 TOGGLE");
}

process.stdin.setRawMode(true);
process.stdin.on("data", d => {
  if (d.toString() === "t") sendToggle();
});
