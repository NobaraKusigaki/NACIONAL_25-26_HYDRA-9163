import asyncio
import json
import argparse
import time
import math
from networktables import NetworkTables
import websockets

# =========================
# CONFIG
# =========================

DEFAULT_ROBORIO = "10.91.63.2"
WS_HOST = "0.0.0.0"
DEFAULT_WS_PORT = 5810
WS_PATH = "/nt/dashboard"

ENABLE_MOCK_STRESS = True   # <<< DESLIGUE EM PRODUÇÃO

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
    "limelight-back": [
        "piece_tx",
        "ta",
        "piece_distance",
        "has_target",
        "bbox",
        "hw"
    ],
    "limelight-front": [
        "hw"
    ],
    "Modes": [
        "AimLockLime4",
        "AimLockLime2",
        "AlignLime2"
    ]
}

POLL_INTERVAL = 0.15
clients = set()

# =========================
# NETWORKTABLES
# =========================

def connect_nt(roborio_host):
    print(f"Inicializando NetworkTables -> server={roborio_host}")
    NetworkTables.initialize(server=roborio_host)

    waited = 0.0
    while not NetworkTables.isConnected():
        time.sleep(0.1)
        waited += 0.1
        if waited > 10.0:
            print("⚠️ NT não confirmou conexão ainda (seguindo mesmo assim)")
            break

    if NetworkTables.isConnected():
        print("✅ Conectado ao NetworkTables (NT3)")
    else:
        print("❌ NT ainda não conectado")

def get_table(name):
    return NetworkTables.getTable(name)

def read_any(table, key):
    try:
        arr = table.getNumberArray(key, None)
        if arr is not None:
            return list(arr)
    except:
        pass

    try:
        v = table.getNumber(key, None)
        if v is not None:
            return v
    except:
        pass

    try:
        v = table.getString(key, None)
        if v is not None:
            return v
    except:
        pass

    try:
        v = table.getBoolean(key, None)
        if v is not None:
            return bool(v)
    except:
        pass

    return None

# =========================
# MOCK ROBOT STRESS
# =========================

async def mock_robot_stress():
    table = get_table("RobotStress")
    t0 = time.time()

    while True:
        t = time.time() - t0

        battery = max(10.2, 12.6 - t * 0.01)
        total_current = 60 + 20 * abs(math.sin(t / 2))
        drive_current = 30 + 15 * abs(math.sin(t))
        speed_scale = 1.0 if battery > 11.2 else 0.7

        stress_score = int((total_current / 120) * 100)

        if stress_score < 40:
            level = "LOW"
        elif stress_score < 65:
            level = "MEDIUM"
        elif stress_score < 85:
            level = "HIGH"
        else:
            level = "CRITICAL"

        table.getEntry("batteryVoltage").setDouble(battery)
        table.getEntry("totalCurrent").setDouble(total_current)
        table.getEntry("drivetrainCurrent").setDouble(drive_current)
        table.getEntry("stressScore").setDouble(stress_score)
        table.getEntry("stressLevel").setString(level)
        table.getEntry("speedScale").setDouble(speed_scale)
        table.getEntry("chassisSpeed").setDouble(3.0 * speed_scale)

        await asyncio.sleep(0.5)

# =========================
# WS CORE
# =========================

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
                    msg = json.dumps({"topic": topic, "value": val})

                    dead = []
                    for ws in clients:
                        try:
                            await ws.send(msg)
                        except:
                            dead.append(ws)

                    for ws in dead:
                        clients.discard(ws)

                    print("📣", topic, val)

        await asyncio.sleep(POLL_INTERVAL)

async def handle_ws(ws):
    print("🟢 Browser conectado:", ws.remote_address)
    clients.add(ws)

    try:
        for table_name, keys in TABLES_AND_KEYS.items():
            table = get_table(table_name)
            for key in keys:
                topic = f"/{table_name}/{key}"
                val = read_any(table, key)
                await ws.send(json.dumps({"topic": topic, "value": val}))

        async for _ in ws:
            pass

    except Exception as e:
        print("WS erro:", e)
    finally:
        clients.discard(ws)
        print("🔴 Browser desconectou")

# =========================
# MAIN
# =========================

async def main_async(roborio, port):
    connect_nt(roborio)

    server = await websockets.serve(handle_ws, WS_HOST, port)
    print(f"WebSocket server ouvindo em ws://localhost:{port}{WS_PATH}")

    tasks = [
        asyncio.create_task(poll_and_broadcast())
    ]

    if ENABLE_MOCK_STRESS:
        print("🧪 MOCK RobotStress ATIVADO")
        tasks.append(asyncio.create_task(mock_robot_stress()))

    await asyncio.gather(*tasks)
    await server.wait_closed()

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--roborio", default=DEFAULT_ROBORIO)
    parser.add_argument("--port", type=int, default=DEFAULT_WS_PORT)
    args = parser.parse_args()

    try:
        asyncio.run(main_async(args.roborio, args.port))
    except KeyboardInterrupt:
        print("Encerrando...")

if __name__ == "__main__":
    main()
