# from ultralytics import YOLO

# model = YOLO("gamepiece2026.pt")
# model.predict(source="http://10.91.63.2:1181/?action=stream", show=True, conf=0.40)

# from networktables import NetworkTables
# import time

# NetworkTables.initialize(server="10.91.63.2")
# time.sleep(1)

# for name in ["limelight-back", "limelight", "limelight_front", "limelight-front"]:
#     t = NetworkTables.getTable(name)
#     try:
#         print(name, "keys:", t.getKeys())
#     except Exception as e:
#         print(name, "erro:", e)

import os
import cv2
import random
import numpy as np
from tqdm import tqdm

# =====================================================
# CONFIGURAÇÕES (AJUSTE AQUI)
# =====================================================

INPUT_DIR = r"C:\Users\rapha\Documents\FRC\NACIONAL_25-26_HYDRA-9163\GAMEPIECE-26"
OUTPUT_DIR = r"C:\Users\rapha\Documents\FRC\NACIONAL_25-26_HYDRA-9163\augmentation_dataset"

# Quantas imagens augmentadas gerar por imagem original
AUG_PER_IMAGE = 1   # 1 = +100%, 2 = +200%, etc

# Probabilidades
PROB_FISHEYE = 1.0
PROB_ROTATE = 0.5
PROB_SCALE = 0.5

# Rotação (graus)
ROTATE_MIN = -8
ROTATE_MAX = 8

# Scale
SCALE_MIN = 0.9
SCALE_MAX = 1.1

# Intensidade da fisheye (barrel)
K1_RANGE = (-0.30, -0.15)
K2_RANGE = (0.02, 0.08)

# =====================================================
# SETUP
# =====================================================

os.makedirs(OUTPUT_DIR, exist_ok=True)

images = [
    f for f in os.listdir(INPUT_DIR)
    if f.lower().endswith((".jpg", ".png", ".jpeg"))
]

# =====================================================
# FUNÇÕES
# =====================================================

def apply_fisheye(img):
    h, w = img.shape[:2]

    k1 = random.uniform(*K1_RANGE)
    k2 = random.uniform(*K2_RANGE)

    K = np.array([
        [w, 0, w / 2],
        [0, w, h / 2],
        [0, 0, 1]
    ])

    D = np.array([k1, k2, 0.0, 0.0])

    map1, map2 = cv2.fisheye.initUndistortRectifyMap(
        K, D, np.eye(3), K, (w, h), cv2.CV_32FC1
    )

    return cv2.remap(img, map1, map2, interpolation=cv2.INTER_LINEAR)


def apply_affine(img):
    h, w = img.shape[:2]

    angle = random.uniform(ROTATE_MIN, ROTATE_MAX) if random.random() < PROB_ROTATE else 0
    scale = random.uniform(SCALE_MIN, SCALE_MAX) if random.random() < PROB_SCALE else 1.0

    M = cv2.getRotationMatrix2D((w / 2, h / 2), angle, scale)
    return cv2.warpAffine(img, M, (w, h))


# =====================================================
# PIPELINE
# =====================================================

print(f"Encontradas {len(images)} imagens")

for img_name in tqdm(images, desc="Processando"):
    img_path = os.path.join(INPUT_DIR, img_name)
    img = cv2.imread(img_path)

    if img is None:
        continue

    # Copia imagem original
    cv2.imwrite(os.path.join(OUTPUT_DIR, img_name), img)

    # Augmentações
    for i in range(AUG_PER_IMAGE):
        aug = img.copy()

        if random.random() < PROB_FISHEYE:
            aug = apply_fisheye(aug)

        aug = apply_affine(aug)

        out_name = img_name.rsplit(".", 1)[0] + f"_fish_{i}.jpg"
        cv2.imwrite(os.path.join(OUTPUT_DIR, out_name), aug)

print("Augmentation concluída.")
