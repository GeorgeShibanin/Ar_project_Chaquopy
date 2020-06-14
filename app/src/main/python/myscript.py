import cv2
import numpy as np
from os.path import dirname, join

def func(imge1, imge2):
    imge1 = join(dirname(__file__), imge1)
    imge2 = join(dirname(__file__), imge2)


    img1 = cv2.imread(imge1, 0)
    img2 = cv2.imread(imge2, 0)

    orb = cv2.ORB_create()
    kp_1, desc_1 = orb.detectAndCompute(img1, None)
    kp_2, desc_2 = orb.detectAndCompute(img2, None)


    flann = cv2.FlannBasedMatcher({'algorithm': 0, 'trees' : 5}, {})
    matches = flann.knnMatch(np.asarray(desc_1, np.float32), np.asarray(desc_2, np.float32), 2)

    good_points = []
    ratio = 0.6
    for m, n in matches:
        if m.distance < ratio*n.distance:
            good_points.append(m)

    number_keypoints = 1
    if len(kp_1) <= len(kp_2):
        number_keypoints = len(kp_1)
    else:
        number_keypoints = len(kp_2)

    if((len(good_points) / number_keypoints * 100) > 1):
        return 1
    else:
        return 2