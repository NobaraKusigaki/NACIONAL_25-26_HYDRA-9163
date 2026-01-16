import time
import threading
import cv2
from ultralytics import YOLO
from flask import Flask, jsonify
from flask_cors import CORS

# =========================
# CONFIGURAÇÕES
# =========================

# SOMENTE LIME 2+ (IP fixo)
LIME2_STREAM_URL = "http://10.91.63.200:5800/stream.mjpg"

MODEL_PATH = "gamepiece26.pt"
CONF_THRESHOLD = 0.50

FLASK_HOST = "0.0.0.0"
FLASK_PORT = 5801

INFER_DT = 0.05          # 20 FPS (estável)
STALE_TIMEOUT_S = 2.0    # invalida após 2s sem update

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
# FLASK
# =========================
app = Flask(__name__)
CORS(app)

def _valid_payload():
    now = time.time()
    with state_lock:
        if now - latest["_ts"] > STALE_TIMEOUT_S:
            return {"tx": None, "bbox": None}
        return {"tx": latest["tx"], "bbox": latest["bbox"]}

@app.route("/data")
def data_all():
    # mantém compatível com o que você já estava testando
    return jsonify({"lime2": _valid_payload()})

@app.route("/data/lime2")
def data_lime2():
    return jsonify(_valid_payload())

@app.route("/health")
def health():
    with state_lock:
        ts = latest["_ts"]
    now = time.time()
    return jsonify({
        "lime2": {
            "age_s": None if ts == 0.0 else round(now - ts, 3),
            **cap_status
        }
    })

def start_server():
    app.run(
        host=FLASK_HOST,
        port=FLASK_PORT,
        debug=False,
        use_reloader=False,
        threaded=True
    )

# =========================
# CAPTURE (THREAD)
# =========================
def open_capture(url: str) -> cv2.VideoCapture:
    # CAP_FFMPEG costuma ser mais estável no Windows pra MJPG
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
# INFERÊNCIA
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
    tx = ((x1 + x2) // 2) - cx
    return float(tx), [x1, y1, x2, y2]

def update_latest(tx, bbox):
    with state_lock:
        latest["tx"] = tx
        latest["bbox"] = bbox
        latest["_ts"] = time.time()

def main_loop():
    global running, last_infer

    model = YOLO(MODEL_PATH)

    threading.Thread(target=capture_worker, daemon=True).start()

    while running:
        now = time.time()

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
    threading.Thread(target=start_server, daemon=True).start()
    try:
        main_loop()
    except KeyboardInterrupt:
        running = False
