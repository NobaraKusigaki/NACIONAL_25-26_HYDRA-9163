<<<<<<< HEAD
const wsUrl = "ws://localhost:5805/nt/dashboard";
=======
const wsUrl = "ws://localhost:5810/nt/dashboard";
>>>>>>> 308dc7f6fd2a7995ceda62289307afb3cf27b37c
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
<<<<<<< HEAD

  
  if (!msg.topic) return;

  nt[msg.topic] = msg.value;
  updateDashboard();
};

function updateDashboard() {

=======
  if (!msg.topic) return;

  nt[msg.topic] = msg.value;
  requestAnimationFrame(updateDashboard);
};

function updateDashboard() {
>>>>>>> 308dc7f6fd2a7995ceda62289307afb3cf27b37c
  setNum("/RobotStress/batteryVoltage", "battery-voltage", " V", 2);
  setNum("/RobotStress/totalCurrent", "total-current", " A", 1);
  setNum("/RobotStress/drivetrainCurrent", "drivetrain-current", " A", 1);
  setNum("/RobotStress/stressScore", "stress-score", "", 0);

<<<<<<< HEAD
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
=======
  const cs = nt["/RobotStress/chassisSpeed"];
  if (typeof cs === "number" && isFinite(cs)) {
    document.getElementById("chassis-speed").innerText =
      cs.toFixed(2) + " m/s";
  }

  const ss = nt["/RobotStress/speedScale"];
  if (typeof ss === "number" && isFinite(ss)) {
    document.getElementById("speed-scale").innerText =
      Math.round(ss * 100) + "%";
  }

  const level = nt["/RobotStress/stressLevel"];
  if (level !== undefined && level !== null) {
    updateStressStatus(level);
>>>>>>> 308dc7f6fd2a7995ceda62289307afb3cf27b37c
  }

  handleBatterySpeedWarning();
}

function setNum(topic, id, suffix, decimals) {
<<<<<<< HEAD
  if (nt[topic] === undefined) return;
  document.getElementById(id).innerText =
    nt[topic].toFixed(decimals) + suffix;
=======
  const v = nt[topic];
  if (typeof v !== "number" || !isFinite(v)) return;

  document.getElementById(id).innerText =
    v.toFixed(decimals) + suffix;
>>>>>>> 308dc7f6fd2a7995ceda62289307afb3cf27b37c
}

function updateStressStatus(level) {
  const box = document.getElementById("stress-status");
<<<<<<< HEAD

=======
>>>>>>> 308dc7f6fd2a7995ceda62289307afb3cf27b37c
  box.textContent = level;
  box.className = "";

  if (level === "LOW") box.classList.add("status-ok");
  if (level === "MEDIUM") box.classList.add("status-medium");
  if (level === "HIGH") box.classList.add("status-high");
  if (level === "CRITICAL") box.classList.add("status-critical");
}

function handleBatterySpeedWarning() {
<<<<<<< HEAD

  const voltage = nt["/RobotStress/batteryVoltage"];
  const speedScale = nt["/RobotStress/speedScale"];

  const warning = document.getElementById("speed-warning");

  if (voltage === undefined || speedScale === undefined) {
=======
  const voltage = nt["/RobotStress/batteryVoltage"];
  const speedScale = nt["/RobotStress/speedScale"];
  const warning = document.getElementById("speed-warning");

  if (typeof voltage !== "number" || typeof speedScale !== "number") {
>>>>>>> 308dc7f6fd2a7995ceda62289307afb3cf27b37c
    warning.classList.add("hidden");
    return;
  }

  if (voltage < 11.0 && speedScale < 1.0) {
    warning.classList.remove("hidden");
  } else {
    warning.classList.add("hidden");
  }
}
