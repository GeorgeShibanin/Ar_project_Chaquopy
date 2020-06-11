import cv2
import numpy as np
from PIL import Image
from os.path import dirname, join

def func(imge):
    imge = join(dirname(__file__), imge)
    img = cv2.imread(imge)
    detector = cv2.FastFeatureDetector_create(50)

    kp = detector.detect(img, None)
    pts = np.array([kp[idx].pt for idx in range(0, len(kp))])
    res = (img[int(pts[0][0])][0][1])


    return res