import cv2
import numpy as np
from os.path import dirname, join
import base64
import io
import os
import threading
from PIL import Image

def thread_function(orb, imge2, my_lst):
    imge2 = join(dirname(__file__), imge2)
    img2 = cv2.imread(imge2)
    kp_2, desc_2 = orb.detectAndCompute(img2, None)
    my_lst.append(kp_2)
    my_lst.append(desc_2)

def func(input_frame, imge2):
    my_list = []
    orb = cv2.ORB_create()
    x = threading.Thread(target=thread_function, args=(orb, imge2, my_list))
    x.start()

    frame = Image.open(io.BytesIO(bytes(input_frame)))
    frame = np.array(frame)
    img1 = frame[:, :, ::-1].copy()


    kp_1, desc_1 = orb.detectAndCompute(img1, None)

    x.join()

    kp_2 = my_list[0]
    desc_2 = my_list[1]

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

    if((len(good_points) / number_keypoints * 100) > 0.45):
        return 1
    else:
        return 2