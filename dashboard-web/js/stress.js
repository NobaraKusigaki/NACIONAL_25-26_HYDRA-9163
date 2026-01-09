const wsUrl = "ws://localhost:5805/nt/dashboard";
const socket = new WebSocket(wsUrl);

let nt = {};

socket.onopen = () => {
  console.log("🟢 Conectado ao robô (NT3)");
};

socket.onerror = err => {
  console.error("❌ Erro NT", err);
};

socket.onclose = () => {
  console.warn("🔴 NT desconectado");
};

socket.onmessage = event => {
  const msg = JSON.parse(event.data);

  
  if (!msg.topic) return;

  nt[msg.topic] = msg.value;
  updateDashboard();
};

function updateDashboard() {

  setNum("/RobotStress/batteryVoltage", "battery-voltage", " V", 2);
  setNum("/RobotStress/totalCurrent", "total-current", " A", 1);
  setNum("/RobotStress/drivetrainCurrent", "drivetrain-current", " A", 1);
  setNum("/RobotStress/stressScore", "stress-score", "", 0);

  if (nt["/RobotStress/chassisSpeed"] !== undefined) {
    document.getElementById("chassis-speed").innerText =
      nt["/RobotStress/chassisSpeed"].toFixed(2) + " m/s";
  }

  if (nt["/RobotStress/speedScale"] !== undefined) {
    document.getElementById("speed-scale").innerText =
      Math.round(nt["/RobotStress/speedScale"] * 100) + "%";
  }

  if (nt["/RobotStress/stressLevel"]) {
    updateStressStatus(nt["/RobotStress/stressLevel"]);
  }

  handleBatterySpeedWarning();
}

function setNum(topic, id, suffix, decimals) {
  if (nt[topic] === undefined) return;
  document.getElementById(id).innerText =
    nt[topic].toFixed(decimals) + suffix;
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

function handleBatterySpeedWarning() {

  const voltage = nt["/RobotStress/batteryVoltage"];
  const speedScale = nt["/RobotStress/speedScale"];

  const warning = document.getElementById("speed-warning");

  if (voltage === undefined || speedScale === undefined) {
    warning.classList.add("hidden");
    return;
  }

  if (voltage < 11.0 && speedScale < 1.0) {
    warning.classList.remove("hidden");
  } else {
    warning.classList.add("hidden");
  }
}
