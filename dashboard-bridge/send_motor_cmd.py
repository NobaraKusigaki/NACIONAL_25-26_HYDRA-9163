from networktables import NetworkTables
import time

NetworkTables.initialize(server="10.91.63.2")
table = NetworkTables.getTable("StreamDeck")

while not NetworkTables.isConnected():
    time.sleep(0.1)

print("Conectado")

while True:
    cmd = input("Digite CW / CCW / STOP: ").strip().upper()
    table.putString("motorTest", cmd)
