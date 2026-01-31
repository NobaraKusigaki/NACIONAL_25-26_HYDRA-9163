// send_sd_command.js
const WebSocket = require("ws");

/*
Uso:
node send_sd_command.js intake INTAKE
node send_sd_command.js intake OUTTAKE
node send_sd_command.js intake IDLE

node send_sd_command.js intakeAngle TOGGLE

node send_sd_command.js spindexer SPIN
node send_sd_command.js spindexer IDLE
*/

const SYSTEM = process.argv[2];   // intake | intakeAngle | spindexer
const COMMAND = process.argv[3];  // comando

if (!SYSTEM || !COMMAND) {
  console.log("Uso:");
  console.log(" node send_sd_command.js intake INTAKE|OUTTAKE|IDLE");
  console.log(" node send_sd_command.js intakeAngle TOGGLE");
  console.log(" node send_sd_command.js spindexer SPIN|IDLE");
  process.exit(1);
}

let table;
let key = "command";

switch (SYSTEM.toLowerCase()) {

  case "intake":
    table = "StreamDeck/Intake";
    break;

  case "intakeangle":
    table = "StreamDeck/IntakeAngle";
    break;

  case "spindexer":
    table = "StreamDeck/Spindexer";
    break;

  default:
    console.log("Sistema inválido:", SYSTEM);
    process.exit(1);
}

const ws = new WebSocket("ws://localhost:5810/nt/dashboard");

ws.on("open", () => {
  ws.send(JSON.stringify({
    action: "put",
    table: table,
    key: key,
    value: COMMAND
  }));

  console.log("📡 Enviado:", table, COMMAND);
  setTimeout(() => ws.close(), 200);
});
