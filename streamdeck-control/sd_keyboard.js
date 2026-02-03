//sd_keyboard.js

const WebSocket = require("ws");
const { GlobalKeyboardListener } = require("node-global-key-listener");

const ws = new WebSocket("ws://127.0.0.1:5810");

function send(value) {
  ws.send(JSON.stringify({
    action: "put",
    value
  }));
  console.log("📤", value);
}

const gkl = new GlobalKeyboardListener();

gkl.addListener(event => {
  if (event.state !== "DOWN") return;

  if (event.name === "T") {
    send("TOGGLE");
  }
});
