from ultralytics import YOLO
from RIO2WPILIB import *

# Funções para rodar a IA
model = YOLO("gamepiece25.pt")
limelight_url = "http://10.91.63.30:5800"
results = model.predict(source=0, show=True, stream=True, verbose=False)

# Variáveis para calcular a distância
focal_length = 256 # Em pixels
coral_width = 0.114 # Em metros
algae_width = 0.0192

for r in results:
    boxes = r.boxes
    for box in boxes:
        cls = int(box.cls[0])

        # Calculo da distância do coral
        if r.names[cls] == "coral":
            x1, y1, x2, y2 = box.xyxy[0]
            width_pixels = x2 - x1

            # Calcula distância
            distance_c = (coral_width * focal_length) / width_pixels

            print(f"🎯 {r.names[cls]} a aproximadamente {distance_c:.2f} m de distância")
            rio2wpi_coral(distance_c)

        if r.names[cls] == "algae":
            x1, y1, x2, y2 = box.xyxy[0]
            width_pixels = x2 - x1

            # Calcula distância
            distance_a = (algae_width * focal_length) / width_pixels

            print(f"🎯 {r.names[cls]} a aproximadamente {distance_a:.2f} m de distância")
            rio2wpi_algae(distance_a)