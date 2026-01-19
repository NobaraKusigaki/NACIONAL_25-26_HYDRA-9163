
import asyncio
import json
import argparse
import time
from networktables import NetworkTables
import websockets

DEFAULT_ROBORIO = "10.91.63.2" 
WS_HOST = "0.0.0.0"
DEFAULT_WS_PORT = 5810
WS_PATH = "/nt/dashboard"


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
    "StreamDeck/Intake": [
        "command"
    ]
}

POLL_INTERVAL = 0.15 

clients = set()

def connect_nt(roborio_host):
    """Inicializa a conexão com NetworkTables (NT3)."""
    print(f"Inicializando NetworkTables -> server={roborio_host}")
    NetworkTables.initialize(server=roborio_host)
    waited = 0.0
    while not NetworkTables.isConnected():
        time.sleep(0.1)
        waited += 0.1
        if waited > 10.0:
            print("Aguardando conexão NT... (continuando mesmo assim)")
            break
    if NetworkTables.isConnected():
        print("✅ Conectado ao NetworkTables (NT3)")
    else:
        print("⚠️ NetworkTables não confirmou conexão ainda; continue e veja se os valores aparecem depois.")

def get_table(table_name):
    return NetworkTables.getTable(table_name)

async def poll_and_broadcast():
    """Lê as chaves periodicamente e envia para clients se foram alteradas."""
    last_values = {}
    while True:
        for table_name, keys in TABLES_AND_KEYS.items():
            table = get_table(table_name)
            for key in keys:
                topic = f"/{table_name}/{key}"
                val = None
                try:
                    val = table.getNumber(key, None)
                except Exception:
                    val = None
                if val is None:
                    try:
                        val = table.getString(key, None)
                    except Exception:
                        val = None
                if val is None:
                    try:
                        val_bool = table.getBoolean(key, None)
                        if val_bool is not None:
                            val = bool(val_bool)
                        else:
                            val = None
                    except Exception:
                        val = None

                if topic not in last_values or last_values[topic] != val:
                    last_values[topic] = val
                    payload = {"topic": topic, "value": val}
                    msg = json.dumps(payload)
                    to_remove = []
                    for ws in clients:
                        try:
                            await ws.send(msg)
                        except Exception:
                            to_remove.append(ws)
                    for ws in to_remove:
                        clients.discard(ws)
                    print("📣 Enviado:", topic, val)
        await asyncio.sleep(POLL_INTERVAL)

async def handle_ws(ws):
    print("🟢 Browser conectado:", ws.remote_address)
    clients.add(ws)

    try:
        # snapshot inicial
        for table_name, keys in TABLES_AND_KEYS.items():
            table = get_table(table_name)
            for key in keys:
                topic = f"/{table_name}/{key}"
                val = None

                try:
                    val = table.getNumber(key, None)
                except:
                    pass
                if val is None:
                    try:
                        val = table.getString(key, None)
                    except:
                        pass
                if val is None:
                    try:
                        val = table.getBoolean(key, None)
                    except:
                        pass

                await ws.send(json.dumps({
                    "topic": topic,
                    "value": val
                }))

        async for message in ws:
            obj = json.loads(message)
            if obj.get("action") == "put":
                table = get_table(obj["table"])
                value = obj["value"]

                if isinstance(value, bool):
                    table.putBoolean(obj["key"], value)
                elif isinstance(value, (int, float)):
                    table.putNumber(obj["key"], value)
                else:
                    table.putString(obj["key"], str(value))

                print("📡 PUT:", obj)

    except Exception as e:
        print("WS erro:", e)
    finally:
        clients.discard(ws)
        print("🔴 Browser desconectou")

async def main_async(roborio, port):
    connect_nt(roborio)
    server = await websockets.serve(handle_ws, WS_HOST, port)
    print(f"WebSocket server ouvindo em ws://localhost:{port}{WS_PATH}")
    poll_task = asyncio.create_task(poll_and_broadcast())

    await server.wait_closed()
    await poll_task

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--roborio", default=DEFAULT_ROBORIO, help="IP ou hostname do roboRIO")
    parser.add_argument("--port", type=int, default=DEFAULT_WS_PORT, help="Porta WebSocket")
    args = parser.parse_args()

    try:
        asyncio.run(main_async(args.roborio, args.port))
    except KeyboardInterrupt:
        print("Encerrando...")

if __name__ == "__main__":
    main()