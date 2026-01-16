// ===============================
// LIMELIGHT ARROW CONTROLLER (SÓ LIME 2+ COM IA)
// ===============================

// CONFIG GERAL
const UPDATE_RATE_MS = 50;

const TX_DEADBAND = 10;
const TX_MEDIUM   = 60;
const TX_STRONG   = 120;

// ===============================
// SETUP POR LIMELIGHT
// ===============================
function setupLimelightArrow(config) {
  const {
    arrowId,
    parentSelector,
    imgSelector,
    dataUrl
  } = config;

  const arrow = document.getElementById(arrowId);
  const parent = document.querySelector(parentSelector);
  const img = parent ? parent.querySelector(imgSelector) : null;

  if (!arrow || !parent || !img) {
    console.warn("Limelight não inicializada:", config);
    return;
  }

  // -------------------------------
  // BOUNDING BOX (por limelight)
  // -------------------------------
  const bboxDiv = document.createElement("div");
  bboxDiv.className = "bbox";
  bboxDiv.style.position = "absolute";
  bboxDiv.style.border = "2px solid red";
  bboxDiv.style.pointerEvents = "none";
  bboxDiv.style.display = "none";
  parent.appendChild(bboxDiv);

  // -------------------------------
  // SETA
  // -------------------------------
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
      arrow.textContent = "<".repeat(level);
      arrow.classList.add("arrow-right");
    } else {
      arrow.textContent = ">".repeat(level);
      arrow.classList.add("arrow-left");
    }
  }

  // -------------------------------
  // BOUNDING BOX (ESCALADA)
  // -------------------------------
  function atualizarBBox(bbox) {
    // img.naturalWidth/Height só existe para <img>
    if (!img.complete || !bbox || bbox.length !== 4 || !img.naturalWidth || !img.naturalHeight) {
      bboxDiv.style.display = "none";
      return;
    }

    const [x1, y1, x2, y2] = bbox;

    // bboxDiv está dentro do parent (position: relative no CSS)
    const rect = img.getBoundingClientRect();

    const scaleX = rect.width / img.naturalWidth;
    const scaleY = rect.height / img.naturalHeight;

    bboxDiv.style.left   = (x1 * scaleX) + "px";
    bboxDiv.style.top    = (y1 * scaleY) + "px";
    bboxDiv.style.width  = ((x2 - x1) * scaleX) + "px";
    bboxDiv.style.height = ((y2 - y1) * scaleY) + "px";

    bboxDiv.style.display = "block";
  }

  // -------------------------------
  // FETCH LOOP
  // -------------------------------
  async function buscarDados() {
    try {
      // cache: "no-store" evita pegar resposta antiga do browser
      const response = await fetch(dataUrl, { cache: "no-store" });
      const data = await response.json();

      atualizarSeta(data.tx);
      atualizarBBox(data.bbox);

    } catch (err) {
      console.error("Erro ao buscar dados:", dataUrl, err);
      // se der erro, esconde overlays (evita “congelar” na tela)
      atualizarSeta(null);
      atualizarBBox(null);
    }
  }

  // primeira chamada imediata + loop
  buscarDados();
  setInterval(buscarDados, UPDATE_RATE_MS);
}

// ===============================
// INICIALIZAÇÃO (SOMENTE LIME 2+)
// ===============================
setupLimelightArrow({
  arrowId: "arrow-lime2",
  parentSelector: "#lime2 .arrow-parent",
  imgSelector: "img",
  dataUrl: "http://127.0.0.1:5801/data/lime2"
});
