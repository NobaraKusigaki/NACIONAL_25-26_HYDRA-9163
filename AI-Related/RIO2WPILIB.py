from networktables import NetworkTables
import time

# O IP do rádio do roboRIO 2 é = "10.91.63.2"

def rio2wpi_coral(distancia):
    NetworkTables.initialize(server="10.91.63.2")
    vision = NetworkTables.getTable("limelight-front")

    while not NetworkTables.isConnected():
        print("Tentando conectar...")
        time.sleep(0.5)
    print("✅ Conectado")

    vision.putNumber("distancia_coral", distancia)

    print("📡 Valor enviado.")
    print("🔍 Valor lido de volta:", vision.getNumber("distancia", 0), "|", vision.getBoolean("alvo_detectado", False))

    return vision

def rio2wpi_algae(distancia):
    NetworkTables.initialize(server="10.91.63.2")
    vision = NetworkTables.getTable("limelight-front")

    while not NetworkTables.isConnected():
        print("Tentando conectar...")
        time.sleep(0.5)
    print("✅ Conectado")

    vision.putNumber("distancia_algae", distancia)

    print("📡 Valor enviado.")
    print("🔍 Valor lido de volta:", vision.getNumber("distancia", 0), "|", vision.getBoolean("alvo_detectado", False))

    return vision
