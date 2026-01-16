// ==========================
// ESTADO GLOBAL (simula NT)
// ==========================
const state = {
    lime4: {
      alinhador: 0, // 0 OFF | 1 ON | 2 AUTO
      yaw: 0,       // 0 OFF | 1 ON | 2 BALL
      teste1: 0    // 0 OFF | 1 ON | 2 AUTO
    },
    lime2: {
      alinhador: 0,
      yaw: 0,
      teste1: 0
    }
  };
  
  // ==========================
  // RENDERIZAÇÃO VISUAL
  // ==========================
  function renderMode(id, value) {
    const container = document.getElementById(id);
    if (!container) return;
  
    const box = container.querySelector(".offset-box");
    const modes = container.querySelectorAll(".offset-modes span");
  
    let color = "#444";
  
    switch (value) {
      case 0: color = "#444"; break; // OFF
      case 1: color = "#00ff88"; break; // ON
      case 2: color = "#ffaa00"; break; // AUTO / BALL
    }
  
    // caixa segue o estado
    box.style.background = color;
  
    // reseta todos os modos
    modes.forEach(m => {
      m.style.color = "#888";
      m.style.fontWeight = "normal";
    });
  
    // pinta apenas o modo ativo
    const active = container.querySelector(
      `.offset-modes span[data-mode="${value}"]`
    );
  
    if (active) {
      active.style.color = color;
      active.style.fontWeight = "bold";
    }
  }
  
  // ==========================
  // RENDERIZAÇÃO GLOBAL
  // ==========================
  function renderAll() {
    // Lime 4
    renderMode("alinhador-lime4", state.lime4.alinhador);
    renderMode("yaw-lime4", state.lime4.yaw);
    renderMode("teste1-lime4", state.lime4.teste1);
  
    // Lime 2+
    renderMode("alinhador-lime2", state.lime2.alinhador);
    renderMode("yaw-lime2", state.lime2.yaw);
    renderMode("teste1-lime2", state.lime2.teste1);
  }
  
  // ==========================
  // CONTROLE MANUAL (TECLADO)
  // ==========================
  // 1–4 → Lime 4
  // 5–8 → Lime 2+
  document.addEventListener("keydown", e => {
    switch (e.key) {
      // Lime 4
      case "1": state.lime4.alinhador = (state.lime4.alinhador + 1) % 3; break;
      case "2": state.lime4.yaw       = (state.lime4.yaw + 1) % 3; break;
      case "3": state.lime4.teste1    = (state.lime4.teste1 + 1) % 3; break;
  
      // Lime 2+
      case "5": state.lime2.alinhador = (state.lime2.alinhador + 1) % 3; break;
      case "6": state.lime2.yaw       = (state.lime2.yaw + 1) % 3; break;
      case "7": state.lime2.teste1    = (state.lime2.teste1 + 1) % 3; break;
  
      default: return;
    }
  
    renderAll();
  });
  
  // ==========================
  // INIT
  // ==========================
  renderAll();
  