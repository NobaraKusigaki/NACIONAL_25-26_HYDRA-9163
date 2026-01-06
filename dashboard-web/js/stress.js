const ntSocket = new WebSocket("ws://localhost:5810");

let ntData = {};

ntSocket.onopen = () => {
  console.log("[NT] Conectado ao robô");
};

ntSocket.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  if (!msg.key) return;

  ntData[msg.key] = msg.value;
  updateStressDashboard();
};

ntSocket.onerror = () => {
  console.warn("[NT] Erro na conexão NT");
};

function updateStressDashboard() {
  const voltage = ntData["/RobotStress/batteryVoltage"];
  const totalCurrent = ntData["/RobotStress/totalCurrent"];
  const drivetrainCurrent = ntData["/RobotStress/drivetrainCurrent"];
  const stressScore = ntData["/RobotStress/stressScore"];
  const stressLevel = ntData["/RobotStress/stressLevel"];
  const speedScale = ntData["/RobotStress/speedScale"];
  const chassisSpeed = ntData["/RobotStress/chassisSpeed"];

  if (voltage !== undefined)
    document.getElementById("battery-voltage").textContent =
      voltage.toFixed(2) + " V";

  if (totalCurrent !== undefined)
    document.getElementById("total-current").textContent =
      totalCurrent.toFixed(1) + " A";

  if (drivetrainCurrent !== undefined)
    document.getElementById("drivetrain-current").textContent =
      drivetrainCurrent.toFixed(1) + " A";

  if (stressScore !== undefined)
    document.getElementById("stress-score").textContent =
      stressScore.toFixed(0);

  if (speedScale !== undefined)
    document.getElementById("speed-scale").textContent =
      Math.round(speedScale * 100) + "%";

  if (chassisSpeed !== undefined)
    document.getElementById("chassis-speed").textContent =
      chassisSpeed.toFixed(2) + " m/s";

  if (stressLevel !== undefined)
    updateStressStatus(stressLevel);
}

function updateStressStatus(level) {
  const statusBox = document.getElementById("stress-status");

  statusBox.textContent = level;

  statusBox.classList.remove(
    "status-ok",
    "status-medium",
    "status-high",
    "status-critical"
  );

  switch (level) {
    case "LOW":
      statusBox.classList.add("status-ok");
      break;
    case "MEDIUM":
      statusBox.classList.add("status-medium");
      break;
    case "HIGH":
      statusBox.classList.add("status-high");
      break;
    case "CRITICAL":
      statusBox.classList.add("status-critical");
      break;
  }
}
