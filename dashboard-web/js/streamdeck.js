const socket = new WebSocket("ws://localhost:5805/nt/dashboard");

const buttons = document.querySelectorAll(".sd-button");

socket.onopen = () => {
  console.log("🟢 StreamDeck UI conectada ao WS");
};

socket.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  if (!msg.topic) return;

  if (!msg.topic.startsWith("/StreamDeck/")) return;

  const [, , system, key] = msg.topic.split("/");
  if (key !== "state") return;

  updateButton(system, msg.value);
};

function updateButton(system, state) {
  const btn = document.querySelector(`.sd-button[data-system="${system}"]`);
  if (!btn) return;

  btn.classList.remove("not-ready", "preparing", "ready");

  if (state === "NOT_READY") {
    btn.classList.add("not-ready");
    btn.querySelector("small").innerText = "Não preparado";
  } else if (state === "PREPARING") {
    btn.classList.add("preparing");
    btn.querySelector("small").innerText = "Preparando";
  } else if (state === "READY") {
    btn.classList.add("ready");
    btn.querySelector("small").innerText = "Preparado";
  }
}

buttons.forEach(btn => {
  btn.addEventListener("click", () => {
    const system = btn.dataset.system;

    socket.send(JSON.stringify({
      action: "put",
      table: `StreamDeck/${system}`,
      key: "command",
      value: "EXECUTE"
    }));

    console.log(`📤 EXECUTE enviado para ${system}`);
  });
});
