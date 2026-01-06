// NT4 direto do robô (igual AdvantageScope)
const socket = new WebSocket("ws://localhost:5805/nt/dashboard");

socket.onopen = () => {
  console.log("🟢 Conectado ao robô (NT3)");
};

socket.onmessage = event => {
  const msg = JSON.parse(event.data);
  console.log("RAW NT:", msg);
// msg = { topic, value }
if (!msg.topic || msg.value === undefined) return;

  console.log("📡", msg.topic, msg.value);

  updateValue(msg.topic, msg.value);
};

socket.onerror = err => {
  console.error("❌ Erro NT", err);
};

socket.onclose = () => {
  console.warn("🔴 NT desconectado");
};

// ==========================
// Atualização da dashboard
// ==========================
function updateValue(topic, value) {
  switch (topic) {

    case "/RobotStress/batteryVoltage":
      set("battery-voltage", value, " V", 2);
      break;

    case "/RobotStress/totalCurrent":
      set("total-current", value, " A", 1);
      break;

    case "/RobotStress/drivetrainCurrent":
      set("drivetrain-current", value, " A", 1);
      break;

    case "/RobotStress/stressScore":
      set("stress-score", value, "", 0);
      break;

    case "/RobotStress/stressLevel":
      updateStressStatus(value);
      break;

    case "/RobotStress/speedScale":
      document.getElementById("speed-scale").innerText =
        Math.round(value * 100) + "%";
      break;

    case "/RobotStress/chassisSpeed":
      document.getElementById("chassis-speed").innerText =
        value.toFixed(2) + " m/s";
      break;
  }
}

function set(id, value, suffix, decimals) {
  document.getElementById(id).innerText =
    value.toFixed(decimals) + suffix;
}

function updateStressStatus(level) {
  const box = document.getElementById("stress-status");

  box.textContent = level;
  box.className = "";

  if (level === "LOW") box.classList.add("status-ok");
  if (level === "MEDIUM") box.classList.add("status-medium");
  if (level === "HIGH") box.classList.add("status-high");
  if (level === "CRITICAL") box.classList.add("status-critical");
}
