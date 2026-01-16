import time
import threading
import cv2
from ultralytics import YOLO

# IMPORTA FUNCOES DO SEU RIO2WPILIB
from limelight.RIO2WPILIB import (
    init_nt,
    rio2wpi_tx,
    rio2wpi_distance,
    rio2wpi_has_target,
    rio2wpi_bbox,
)

# =========================
# CONFIGURACOES
# =========================

# SOMENTE LIME 2+ (IP fixo)
LIME2_STREAM_URL = "http://10.91.63.200:5800/stream.mjpg"

MODEL_PATH = "yolov8n.pt"
CONF_THRESHOLD = 0.10

INFER_DT = 0.05          # 20 FPS (estavel)
STALE_TIMEOUT_S = 2.0    # invalida apos 2s sem update

REOPEN_COOLDOWN_S = 0.5
FAILS_BEFORE_REOPEN = 8

running = True

# =========================
# ESTADO COMPARTILHADO
# =========================
state_lock = threading.Lock()
latest = {"tx": None, "bbox": None, "_ts": 0.0}

frame_lock = threading.Lock()
latest_frame = None

cap_status = {"opened": False, "fails": 0, "reopens": 0, "last_ok": 0.0}
last_infer = 0.0

# =========================
# NETWORKTABLES (RIO)
# =========================
# Inicializa UMA vez (nao reinicializa a cada envio)
init_nt(server="10.91.63.2", table_name="limelight-back")

# =========================
# CAPTURE (THREAD)
# =========================
def open_capture(url: str) -> cv2.VideoCapture:
    # CAP_FFMPEG costuma ser mais estavel no Windows pra MJPG
    cap = cv2.VideoCapture(url, cv2.CAP_FFMPEG)
    try:
        cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)
    except Exception:
        pass
    return cap

def capture_worker():
    global running, latest_frame

    cap = None
    last_open_try = 0.0

    def reopen():
        nonlocal cap, last_open_try
        now = time.time()
        if now - last_open_try < REOPEN_COOLDOWN_S:
            return
        last_open_try = now

        if cap is not None:
            try:
                cap.release()
            except Exception:
                pass
            cap = None

        cap = open_capture(LIME2_STREAM_URL)
        cap_status["opened"] = bool(cap is not None and cap.isOpened())
        cap_status["fails"] = 0
        cap_status["reopens"] += 1

    reopen()

    while running:
        if cap is None or not cap.isOpened():
            cap_status["opened"] = False
            reopen()
            time.sleep(0.02)
            continue

        ret, fr = cap.read()
        if not ret or fr is None:
            cap_status["fails"] += 1
            if cap_status["fails"] >= FAILS_BEFORE_REOPEN:
                cap_status["opened"] = False
                reopen()
            time.sleep(0.01)
            continue

        cap_status["fails"] = 0
        cap_status["opened"] = True
        cap_status["last_ok"] = time.time()

        with frame_lock:
            latest_frame = fr

        time.sleep(0.001)

    if cap is not None:
        cap.release()

# =========================
# INFERENCIA
# =========================
def infer_one_frame(model: YOLO, frame):
    if frame is None:
        return None, None

    h, w = frame.shape[:2]
    cx = w // 2

    results = model(frame, verbose=False)

    best = None
    best_conf = -1.0

    for r in results:
        for box in r.boxes:
            conf = float(box.conf[0])
            if conf < CONF_THRESHOLD:
                continue
            if conf > best_conf:
                x1, y1, x2, y2 = map(int, box.xyxy[0])
                best = (x1, y1, x2, y2)
                best_conf = conf

    if best is None:
        return None, None

    x1, y1, x2, y2 = best
    tx = ((x1 + x2) // 2) - cx  # px a direita positivo, a esquerda negativo
    return float(tx), [x1, y1, x2, y2]

def update_latest(tx, bbox):
    now = time.time()
    with state_lock:
        latest["tx"] = tx
        latest["bbox"] = bbox
        latest["_ts"] = now

    has_target = (tx is not None) and (bbox is not None)

    # Publica sempre o has_target
    rio2wpi_has_target(has_target)

    # Se tem alvo, publica tx e bbox
    if has_target:
        rio2wpi_tx(tx)
        rio2wpi_bbox(bbox)

        # Distancia: por enquanto NAO calcula -> nao publica
        # Quando tiver, basta descomentar:
        # rio2wpi_distance(distancia)

def publish_stale_if_needed():
    """
    Se ficar stale por muito tempo, publica has_target=False para o roboRIO nao usar dado velho.
    """
    now = time.time()
    with state_lock:
        ts = latest["_ts"]

    if ts == 0.0:
        return

    if (now - ts) > STALE_TIMEOUT_S:
        rio2wpi_has_target(False)

def main_loop():
    global running, last_infer

    model = YOLO(MODEL_PATH)

    threading.Thread(target=capture_worker, daemon=True).start()

    while running:
        now = time.time()

        # publica stale se necessario (barato)
        publish_stale_if_needed()

        if now - last_infer >= INFER_DT:
            img = None
            with frame_lock:
                if latest_frame is not None:
                    img = latest_frame.copy()

            if img is not None:
                last_infer = now
                tx, bbox = infer_one_frame(model, img)
                update_latest(tx, bbox)

        time.sleep(0.001)

# =========================
# START
# =========================
if __name__ == "__main__":
    try:
        main_loop()
    except KeyboardInterrupt:
        running = False
