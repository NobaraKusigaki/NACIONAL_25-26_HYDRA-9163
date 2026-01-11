const arrow = document.getElementById("arrow");
const arrowParent = document.querySelector(".arrow-parent");
const video = document.getElementById("video");

// ===============================
// CONFIG
// ===============================
const DATA_URL = "http://127.0.0.1:5801/data";
const UPDATE_RATE_MS = 50;

const TX_DEADBAND = 10;
const TX_MEDIUM = 60;
const TX_STRONG = 120;

// ===============================
// BOUNDING BOX
// ===============================
let bboxDiv = document.getElementById("bbox");
if (!bboxDiv) {
  bboxDiv = document.createElement("div");
  bboxDiv.id = "bbox";
  bboxDiv.style.position = "absolute";
  bboxDiv.style.border = "2px solid red";
  bboxDiv.style.pointerEvents = "none";
  arrowParent.appendChild(bboxDiv);
}

// ===============================
// SETA
// ===============================
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

// ===============================
// BOUNDING BOX (ESCALADA)
// ===============================
function atualizarBBox(bbox) {
  const img = document.querySelector(".arrow-parent img");
  if (!img || !img.complete || !bbox || bbox.length !== 4) {
    bboxDiv.style.display = "none";
    return;
  }

  const [x1, y1, x2, y2] = bbox;

  const rect = img.getBoundingClientRect();

  const scaleX = rect.width / img.naturalWidth;
  const scaleY = rect.height / img.naturalHeight;

  bboxDiv.style.left = (x1 * scaleX) + "px";
  bboxDiv.style.top = (y1 * scaleY) + "px";
  bboxDiv.style.width = ((x2 - x1) * scaleX) + "px";
  bboxDiv.style.height = ((y2 - y1) * scaleY) + "px";

  bboxDiv.style.display = "block";
}


// ===============================
// FETCH
// ===============================
async function buscarDados() {
  try {
    const response = await fetch(DATA_URL);
    const data = await response.json();

    atualizarSeta(data.tx);
    atualizarBBox(data.bbox);

  } catch (err) {
    console.error("Erro ao buscar dados da IA:", err);
  }
}

// ===============================
// LOOP
// ===============================
setInterval(buscarDados, UPDATE_RATE_MS);
