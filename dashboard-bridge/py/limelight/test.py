from ultralytics import YOLO

model = YOLO("best2.pt")
model.predict(source="http://10.91.63.200:5800", show=True, conf=0.6)