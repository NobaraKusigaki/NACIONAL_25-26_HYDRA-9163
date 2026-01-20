const WebSocket = require("ws");
const readline = require("readline");

const ws = new WebSocket("ws://127.0.0.1:5810/nt/dashboard");

ws.on("open", () => {
  console.log("✅ Conectado ao NT3 WS Bridge");
  console.log("");
  console.log("INTAKE:");
  console.log("  I = INTAKE");
  console.log("  O = OUTTAKE");
  console.log("  K = IDLE");
  console.log("");
  console.log("SPINDEXER:");
  console.log("  P = SPIN");
  console.log("  L = IDLE");
  console.log("");
  console.log("Ctrl+C para sair");
});

ws.on("error", err => {
  console.error("❌ Erro WS:", err.message);
});

function send(table, value) {
  ws.send(JSON.stringify({
    action: "put",
    table: table,
    key: "command",
    value: value
  }));

  console.log("📤", table, "→", value);
}

readline.emitKeypressEvents(process.stdin);
process.stdin.setRawMode(true);

process.stdin.on("keypress", (str, key) => {
  if (key.ctrl && key.name === "c") process.exit();

  // Intake
  if (key.name === "i") send("StreamDeck/Intake", "INTAKE");
  if (key.name === "o") send("StreamDeck/Intake", "OUTTAKE");
  if (key.name === "k") send("StreamDeck/Intake", "IDLE");

  // Spindexer
  if (key.name === "p") send("StreamDeck/Spindexer", "SPIN");
  if (key.name === "l") send("StreamDeck/Spindexer", "IDLE");
});
