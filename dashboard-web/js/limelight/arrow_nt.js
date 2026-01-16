// LIMELIGHT ARROW CONTROLLER (WebSocket bridge, sem Flask, sem NT4 no browser)

const UPDATE_RATE_MS = 50;

const TX_DEADBAND = 10;
const TX_MEDIUM   = 60;
const TX_STRONG   = 120;

// URL do websocket (ajuste porta/path para o seu bridge)
const WS_URL = "ws://127.0.0.1:5810/nt/dashboard";

// Tópicos que o bridge envia (no formato do seu nt3_ws.py: "/<table>/<key>")
const KEY_HAS_TARGET = "/limelight-back/has_target";
const KEY_TX         = "/limelight-back/piece_tx";
const KEY_BBOX       = "/limelight-back/bbox";

function setupLimelightArrowWS(config) {
  const { arrowId, parentSelector, imgSelector } = config;

  const arrow  = document.getElementById(arrowId);
  const parent = document.querySelector(parentSelector);
  const img    = parent ? parent.querySelector(imgSelector) : null;

  if (!arrow || !parent || !img) {
    console.warn("Limelight não inicializada:", config);
    return;
  }

  // BBOX DIV
  const bboxDiv = document.createElement("div");
  bboxDiv.className = "bbox";
  bboxDiv.style.position = "absolute";
  bboxDiv.style.border = "2px solid red";
  bboxDiv.style.pointerEvents = "none";
  bboxDiv.style.display = "none";
  parent.appendChild(bboxDiv);

  function atualizarSeta(tx) {
    arrow.classList.remove("hidden", "arrow-left", "arrow-right");
    arrow.textContent = "";

    if (tx === null || tx === undefined || Math.abs(tx) <= TX_DEADBAND) {
      arrow.classList.add("hidden");
      return;
    }

    let level = 1;
    if (Math.abs(tx) >= TX_STRONG) level = 3;
    else if (Math.abs(tx) >= TX_MEDIUM) level = 2;

    if (tx > 0) {
      arrow.textContent = ">".repeat(level);
      arrow.classList.add("arrow-left");
    } else {
      arrow.textContent = "<".repeat(level);
      arrow.classList.add("arrow-right");
    }
  }

  function atualizarBBox(bbox) {
    if (!img.complete || !bbox || bbox.length !== 4 || !img.naturalWidth || !img.naturalHeight) {
      bboxDiv.style.display = "none";
      return;
    }

    const [x1, y1, x2, y2] = bbox;
    const rect = img.getBoundingClientRect();

    const scaleX = rect.width / img.naturalWidth;
    const scaleY = rect.height / img.naturalHeight;

    bboxDiv.style.left   = (x1 * scaleX) + "px";
    bboxDiv.style.top    = (y1 * scaleY) + "px";
    bboxDiv.style.width  = ((x2 - x1) * scaleX) + "px";
    bboxDiv.style.height = ((y2 - y1) * scaleY) + "px";

    bboxDiv.style.display = "block";
  }

  // Estado local
  let hasTarget = false;
  let tx = null;
  let bbox = null;

  function render() {
    if (!hasTarget) {
      atualizarSeta(null);
      atualizarBBox(null);
      return;
    }
    atualizarSeta(tx);
    atualizarBBox(bbox);
  }

  // WebSocket
  let ws = null;
  let lastMsgTs = 0;

  function connect() {
    ws = new WebSocket(WS_URL);

    ws.onopen = () => {
      // UI inicia em “sem alvo” até chegar snapshot
      hasTarget = false;
      tx = null;
      bbox = null;
      render();
    };

    ws.onmessage = (ev) => {
      lastMsgTs = Date.now();

      let obj;
      try {
        obj = JSON.parse(ev.data);
      } catch {
        return;
      }

      const topic = obj.topic;
      const value = obj.value;

      if (topic === KEY_HAS_TARGET) {
        hasTarget = Boolean(value);
        render();
        return;
      }

      if (topic === KEY_TX) {
        tx = (value === null || value === undefined) ? null : Number(value);
        render();
        return;
      }

      if (topic === KEY_BBOX) {
        if (Array.isArray(value) && value.length === 4) {
          bbox = value.map(Number);
        } else {
          bbox = null;
        }
        render();
        return;
      }
    };

    ws.onclose = () => {
      hasTarget = false;
      tx = null;
      bbox = null;
      render();
      setTimeout(connect, 500);
    };

    ws.onerror = () => {
      try { ws.close(); } catch {}
    };
  }

  // watchdog: se parar de chegar msg, desliga alvo
  setInterval(() => {
    if (Date.now() - lastMsgTs > 2000) {
      hasTarget = false;
      tx = null;
      bbox = null;
      render();
    }
  }, UPDATE_RATE_MS);

  render();
  connect();
}

setupLimelightArrowWS({
  arrowId: "arrow-lime2",
  parentSelector: "#lime2 .arrow-parent",
  imgSelector: "img",
});
