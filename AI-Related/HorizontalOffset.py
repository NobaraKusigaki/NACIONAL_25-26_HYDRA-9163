import cv2
import threading
import time
from ultralytics import YOLO
from flask import Flask, jsonify, Response
from flask_cors import CORS

# =========================
# CONFIGURAÇÕES
# =========================
CAMERA_INDEX = "http://limelight.local:5800"
CONF_THRESHOLD = 0.5
MODEL_PATH = "yolov8n.pt"

TX_DEADBAND = 10
TX_MEDIUM = 60
TX_STRONG = 120

# =========================
# VARIÁVEIS COMPARTILHADAS
# =========================
latest_frame = None
latest_tx = None
latest_bbox = None

frame_lock = threading.Lock()
tx_lock = threading.Lock()
running = True

# =========================
# MODELO
# =========================
model = YOLO(MODEL_PATH)

# =========================
# FLASK
# =========================
app = Flask(__name__)
CORS(app)

# rota que retorna tx e bbox
@app.route("/data")
def get_data():
    with tx_lock:
        return jsonify({
            "tx": latest_tx,
            "bbox": latest_bbox  # None ou [x1, y1, x2, y2]
        })

# rota de streaming de vídeo (opcional)
def generate_stream():
    while True:
        with frame_lock:
            if latest_frame is None:
                continue
            frame = latest_frame.copy()
        _, jpeg = cv2.imencode(".jpg", frame)
        yield (
            b"--frame\r\n"
            b"Content-Type: image/jpeg\r\n\r\n" +
            jpeg.tobytes() +
            b"\r\n"
        )

@app.route("/stream")
def stream():
    return Response(
        generate_stream(),
        mimetype="multipart/x-mixed-replace; boundary=frame"
    )

def start_server():
    app.run(host="0.0.0.0", port=5801, debug=False, use_reloader=False)

# =========================
# THREAD: CÂMERA
# =========================
def camera_thread():
    global latest_frame, running

    cap = cv2.VideoCapture(CAMERA_INDEX)
    if not cap.isOpened():
        print("Erro ao abrir a câmera")
        running = False
        return

    while running:
        ret, frame = cap.read()
        if not ret:
            continue

        with frame_lock:
            latest_frame = frame.copy()

    cap.release()

# =========================
# THREAD: INFERÊNCIA
# =========================
def inference_thread():
    global latest_tx, latest_bbox, running

    while running:
        with frame_lock:
            if latest_frame is None:
                continue
            frame = latest_frame.copy()

        h, w, _ = frame.shape
        center_x = w // 2

        results = model(frame, verbose=False)

        tx_value = None
        bbox = None

        for r in results:
            for box in r.boxes:
                conf = float(box.conf[0])
                if conf < CONF_THRESHOLD:
                    continue

                x1, y1, x2, y2 = map(int, box.xyxy[0])
                box_center_x = (x1 + x2) // 2

                tx_value = box_center_x - center_x
                bbox = [x1, y1, x2, y2]
                break

        with tx_lock:
            latest_tx = tx_value
            latest_bbox = bbox

        time.sleep(0.01)

# =========================
# DESENHO DA SETA
# =========================
def draw_arrow(frame, tx):
    if tx is None:
        return

    h, w, _ = frame.shape
    y = h - 30

    if abs(tx) <= TX_DEADBAND:
        return

    if abs(tx) >= TX_STRONG:
        level = 3
    elif abs(tx) >= TX_MEDIUM:
        level = 2
    else:
        level = 1

    if tx > 0:
        arrow = "<" * level
        x = int(w * 0.75)
    else:
        arrow = ">" * level
        x = int(w * 0.10)

    cv2.putText(
        frame,
        arrow,
        (x, y),
        cv2.FONT_HERSHEY_SIMPLEX,
        1.8,
        (0, 0, 255),
        4
    )

# =========================
# MAIN
# =========================
def main():
    global running, latest_frame

    threading.Thread(target=start_server, daemon=True).start()
    threading.Thread(target=camera_thread, daemon=True).start()
    threading.Thread(target=inference_thread, daemon=True).start()

    while running:
        with frame_lock:
            if latest_frame is None:
                continue
            frame = latest_frame.copy()

        with tx_lock:
            tx = latest_tx
            bbox = latest_bbox

        # desenho da bounding box (apenas no Python)
        if bbox:
            x1, y1, x2, y2 = bbox
            cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)

        draw_arrow(frame, tx)

        with frame_lock:
            latest_frame = frame.copy()

        time.sleep(0.01)

# =========================
# START
# =========================
if __name__ == "__main__":
    main()