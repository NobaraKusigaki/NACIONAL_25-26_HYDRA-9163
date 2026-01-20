import asyncio
import json
import argparse
import time
from networktables import NetworkTables
import websockets

# ===============================
# CONFIGURAÇÕES
# ===============================
DEFAULT_ROBORIO = "10.91.63.2"
WS_HOST = "0.0.0.0"
DEFAULT_WS_PORT = 5810
WS_PATH = "/nt/dashboard"

POLL_INTERVAL = 0.15
clients = set()

# ===============================
# TABELAS E CHAVES MONITORADAS
# ===============================
TABLES_AND_KEYS = {
    "RobotStress": [
        "batteryVoltage",
        "totalCurrent",
        "drivetrainCurrent",
        "stressScore",
        "stressLevel",
        "speedScale",
        "chassisSpeed"
    ],
    "StreamDeck/MotorTest": [
        "enable",
        "direction",
        "status"
    ],
    # -------------------------------
    # LIMELIGHT BACK (IA - BALL)
    # -------------------------------
    "limelight-back": [
        "piece_tx",        # erro angular (graus)
        "ta",              # área do alvo
        "piece_distance",  # opcional
        "has_target",      # bool
        "bbox",            # bounding box p/ UI
        "hw"               # health/watchdog
    ],
    # -------------------------------
    # LIMELIGHT FRONT (APRILTAG)
    # -------------------------------
    "limelight-front": [
        "tx",
        "tv",
        "ta",
        "hw"
    ],
    # -------------------------------
    # MODOS DO ROBÔ (UI / CONTROLE)
    # -------------------------------
    "Modes": [
        "AimLockLime4",    # 0 OFF | 1 TAG
        "AimLockLime2",    # 0 OFF | 1 TAG
        "AlignLime2"       # 0 OFF | 1 ON | 2 AUTO
    ]
}

# ===============================
# NETWORKTABLES
# ===============================
def connect_nt(roborio_host):
    print(f"🔌 Inicializando NetworkTables -> server={roborio_host}")
    NetworkTables.initialize(server=roborio_host)

    waited = 0.0
    while not NetworkTables.isConnected():
        time.sleep(0.1)
        waited += 0.1
        if waited > 10.0:
            print("⚠️ Timeout NT, continuando mesmo assim...")
            break

    if NetworkTables.isConnected():
        print("✅ Conectado ao NetworkTables (NT3)")
    else:
        print("⚠️ NT ainda não confirmou conexão")

def get_table(table_name):
    return NetworkTables.getTable(table_name)

# ===============================
# LEITURA GENÉRICA (TIPO-AWARE)
# ===============================
def read_any(table, key):
    try:
        arr = table.getNumberArray(key, None)
        if arr is not None:
            return list(arr)
    except Exception:
        pass

    try:
        v = table.getNumber(key, None)
        if v is not None:
            return v
    except Exception:
        pass

    try:
        v = table.getBoolean(key, None)
        if v is not None:
            return bool(v)
    except Exception:
        pass

    try:
        v = table.getString(key, None)
        if v is not None:
            return v
    except Exception:
        pass

    return None

# ===============================
# POLLING + BROADCAST
# ===============================
async def poll_and_broadcast():
    last_values = {}

    while True:
        for table_name, keys in TABLES_AND_KEYS.items():
            table = get_table(table_name)

            for key in keys:
                topic = f"/{table_name}/{key}"
                val = read_any(table, key)

                if topic not in last_values or last_values[topic] != val:
                    last_values[topic] = val
                    payload = {"topic": topic, "value": val}
                    msg = json.dumps(payload)

                    dead = []
                    for ws in clients:
                        try:
                            await ws.send(msg)
                        except Exception:
                            dead.append(ws)

                    for ws in dead:
                        clients.discard(ws)

                    print("📣", topic, val)

        await asyncio.sleep(POLL_INTERVAL)

# ===============================
# WEBSOCKET HANDLER
# ===============================
async def handle_ws(ws):
    print("🟢 Browser conectado:", ws.remote_address)
    clients.add(ws)

    try:
        # Snapshot inicial
        for table_name, keys in TABLES_AND_KEYS.items():
            table = get_table(table_name)
            for key in keys:
                topic = f"/{table_name}/{key}"
                val = read_any(table, key)
                await ws.send(json.dumps({"topic": topic, "value": val}))

        async for message in ws:
            obj = json.loads(message)

            if obj.get("action") == "put":
                table = get_table(obj["table"])
                key = obj["key"]
                value = obj["value"]

                if isinstance(value, list):
                    table.putNumberArray(key, value)
                elif isinstance(value, bool):
                    table.putBoolean(key, value)
                elif isinstance(value, (int, float)):
                    table.putNumber(key, value)
                else:
                    table.putString(key, str(value))

                print("📡 PUT:", obj)

    except Exception as e:
        print("❌ WS erro:", e)
    finally:
        clients.discard(ws)
        print("🔴 Browser desconectou")

# ===============================
# MAIN
# ===============================
async def main_async(roborio, port):
    connect_nt(roborio)

    server = await websockets.serve(handle_ws, WS_HOST, port)
    print(f"🌐 WebSocket ouvindo em ws://localhost:{port}{WS_PATH}")

    poll_task = asyncio.create_task(poll_and_broadcast())
    await server.wait_closed()
    await poll_task

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--roborio", default=DEFAULT_ROBORIO)
    parser.add_argument("--port", type=int, default=DEFAULT_WS_PORT)
    args = parser.parse_args()

    try:
        asyncio.run(main_async(args.roborio, args.port))
    except KeyboardInterrupt:
        print("⛔ Encerrando...")

if __name__ == "__main__":
    main()