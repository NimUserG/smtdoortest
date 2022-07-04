'''
######################### Summary ##########################
# 파일명 : main.py
# 설명 : 아두이노에서 센서 값을 받아와 카메라를 켜고
         얼굴인식과 모션인식으로 검증을 거친 뒤,
         잠금장치를 제어하기 위해 다시 아두이노에 신호를 보내는 종합적인 코드
# 사용언어 : python
# 최초작성자 : 최나경
# 최종수정자 : 차민서, 최나경 (최종!!!!!!!!!)
# 최초생성일 : 2022.05.04
# 최종수정일 : 2022.05.16
############################################################
'''
# from combine import *

'''[최상위 코드] 아두이노에서 센서 값 받아오기'''

def door_open():
    import RPi.GPIO as GPIO 
    import time
    
    relay_1 = 21
    
    GPIO.setmode(GPIO.BCM)
    
    GPIO.setup(relay_1, GPIO.OUT)
    
    GPIO.output(relay_1, GPIO.HIGH)
    time.sleep(3)
    GPIO.output(relay_1, GPIO.LOW)
    
    GPIO.cleanup()

def from_sensor():
    import RPi.GPIO as GPIO
    import time
    
    GPIO.setmode(GPIO.BCM)

    trig = 2
    echo = 3

    GPIO.setup(trig, GPIO.OUT)
    GPIO.setup(echo, GPIO.IN)
    
    try:
        while True:
            GPIO.output(trig, False)
            time.sleep(0.5)
    
            GPIO.output(trig, True)
            time.sleep(0.00001)
            GPIO.output(trig, False)
    
            while GPIO.input(echo) == 0:
                pulse_start = time.time()
    
            while GPIO.input(echo) == 1:
                pulse_end = time.time()
            
            pulse_duration = pulse_end - pulse_start
            distance = pulse_duration * 17000
            distance = round(distance, 2)
            
            print("거리 : ", distance, "cm")
    
            if distance <= 10:
                return 1
                break
    
    except KeyboardInterrupt:
        GPIO.cleanup()
    
    return 0

'''선언부'''
#import
from pickle import FALSE                    # python 객체 직렬화
import face_recognition                     # 얼굴인식
import numpy as np                          # 배열
import time                                 # 시간
import cv2, dlib                            # OpenCV
from imutils import face_utils              # 얼굴 특징 값 가져오기
from tensorflow.keras.models import load_model          # 케라스(keras)모델 불러오기
from random import *                        # 난수
import threading                            # 쓰레드
import firebase_admin                       # Firebase(1)
from firebase_admin import credentials      # Firebase(2)
from firebase_admin import db               # Firebase(3)
from firebase_admin import firestore # Firebase Storage update(1)
from firebase_admin import storage # Firebase Storage update(2)
from pyfcm import FCMNotification # FCM 알림 보내기 RPI2An
import datetime                             # 날짜/시간
from uuid import uuid4

#디비 업데이트,다운로드 변수와 함수
PROJECT_ID="sample-e0621"
cred = credentials.Certificate('/home/pi/Desktop/serviceAccountKey.json') # .json파일
#firebase_admin.initialize_app(cred, {
 #   'databaseURL' : 'https://sample-e0621-default-rtdb.firebaseio.com/'
#}) # RealTime DB
default_app = firebase_admin.initialize_app(cred, {
    'storageBucket': "sample-e0621.appspot.com",
    'databaseURL':'https://sample-e0621-default-rtdb.firebaseio.com'
}) # Storage
bucket = storage.bucket()

def get_Image():
    bucket=storage.bucket()
    blob=bucket.blob("test/"+"0.jpg")
    blob.download_to_filename("/home/pi/Desktop/face_recognition-master/OriginData/"+"a.jpg")
    blob=bucket.blob("test/"+"1.jpg")
    blob.download_to_filename("/home/pi/Desktop/face_recognition-master/OriginData/"+"b.jpg")
    blob=bucket.blob("test/"+"2.jpg")
    blob.download_to_filename("/home/pi/Desktop/face_recognition-master/OriginData/"+"c.jpg")

def set_realtime(_acc,_name):
    data = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")

    dir = db.reference('Access/'+data)
    dir.update({'승인':str(_acc)})
    dir.update({'이름':str(_name)})

def get_startValue():
    ref_startValue=db.reference('start_value')
    startValue=ref_startValue.get()
    return startValue

##사진찍기 변수와 함수
def fileUpload(file):
    blob = bucket.blob('image_store/'+file)
    new_token = uuid4()
    metadata = {"firebaseStorageDownloadTokens": new_token}
    blob.metadata = metadata

    blob.upload_from_filename(filename='/home/pi/Desktop/image_store/'+file, content_type='image/jpg')

def execute_camera():
    basename = "ej"
    now = datetime.datetime.now().strftime("%Y%m%d_%H%M%S") +'.jpg'
    filename = "_".join([basename, now])

    #사진을 찍어서 저장한다. 파일의 중복되지 않도록 날짜시간을 넣어서 만듬
    cv2.imwrite("/home/pi/Desktop/image_store/" + filename, frame)
    #사진 파일을 파이어베이스에 업로드 한다.
    fileUpload(filename)
    #로컬 하드의 사진을 삭제한다.
    #camera.stop_preview()

#알림 보내기 변수와 함수
# Firebase 콘솔에서 얻어온 서버키
APIKEY = "AAAArs5pUqY:APA91bEOLaD4G9tsXn8zzgfbHO6T6JoLSng4inEfsnO3jtutfRYDZefaMG3tJ9yAjRf2cVApOQ6-4QkycDbD3eRxVlPG9x3v-kZpeGrj--70ZyVt-68O1bOuuR2rrKAO8XXEisg7iI8-"
TOKEN = "dKrGRewiTvmBRqA84XlrJH:APA91bEs7hEWd2Fa3aFcVctCl5KcW5Ldogf9g9WMIYcmXEDPQkcTMSIQc8aDMSn9MMDGivPxPcRaEbBNZ6AuuT916bY9slXHM6mSpwIL77W8VrpgJf8OGkXaQUlivlVEP4qexLV6Prk5"

push_service = FCMNotification(APIKEY)

def sendMessage(title, body):
    data_message = { # 메시지 (data 타입)
        "title" : title,
        "body" : body
    }
    # 토큰값을 이용해 1명에게 푸시알림을 전송함
    result = push_service.single_device_data_message(registration_id=TOKEN, data_message=data_message)
    print(result) # 전송 결과 출력

#모션인식 변수와 함수
IMG_SIZE = (34, 26)
detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor('shape_predictor_68_face_landmarks.dat')
model = load_model('models/2018_12_17_22_58_35.h5')
model.summary()

def crop_eye(img, eye_points):
    x1, y1 = np.amin(eye_points, axis=0)
    x2, y2 = np.amax(eye_points, axis=0)
    cx, cy = (x1 + x2) / 2, (y1 + y2) / 2
    w = (x2 - x1) * 1.2
    h = w * IMG_SIZE[1] / IMG_SIZE[0]
    
    margin_x, margin_y = w / 2, h / 2
    
    min_x, min_y = int(cx - margin_x), int(cy - margin_y)
    max_x, max_y = int(cx + margin_x), int(cy + margin_y)

    eye_rect = np.rint([min_x, min_y, max_x, max_y]).astype(np.int64)
    eye_img = gray[eye_rect[1]:eye_rect[3], eye_rect[0]:eye_rect[2]]
    
    return eye_img, eye_rect

get_Image()

#얼굴인식 변수
'''배열로 바꿔봐야한다, 배열로 바꾸지 말고 그냥 고정해버리면 어떨까?'''
image_a = face_recognition.load_image_file("OriginData/a.jpg")
face_encoding_a = face_recognition.face_encodings(image_a)[0]

image_b = face_recognition.load_image_file("OriginData/b.jpg")
face_encoding_b = face_recognition.face_encodings(image_b)[0]

image_c = face_recognition.load_image_file("OriginData/c.jpg")
face_encoding_c = face_recognition.face_encodings(image_c)[0]

known_image = ["a", "b", "c"]
                                    # 학습한 얼굴들과 이름의 배열
known_face_encodings = [
    face_encoding_a, face_encoding_b,
    face_encoding_c
]
known_face_names = [
    "a", "b",
    "c"
]

face_locations = []
face_encodings = []
face_names = []
process_this_frame = True

gname = ""

'''여기서 부터는 절차 지향적으로 작성하였다.'''
'''실행부'''

while from_sensor():
    '''얼굴인식'''
    rtn = False
    rtn_exit = False
    video_capture = cv2.VideoCapture(0)                             # 카메라 켜기
    cnt_nonface = 0
    cnt_sucface = 0

    while True:
        ret, frame = video_capture.read()                           # 영상 캡쳐
        small_frame = cv2.resize(frame, (0,0), fx=0.25, fy=0.25)    # 1/4로 사이즈 변경 : 빠른 인식을 위해
        rgb_small_frame = small_frame[:, :, ::-1]                   # BGR -> RGB
        
        if process_this_frame:                                      ## 시간 절약을 위한 과정
            # 최근 영상에서 모든 얼굴과 인코딩 찾기
            face_locations = face_recognition.face_locations(rgb_small_frame)
            face_encodings = face_recognition.face_encodings(rgb_small_frame, face_locations)

            face_name = []
            for face_encoding in face_encodings:
                matches = face_recognition.compare_faces(known_face_encodings, face_encoding)
                name = "Unknown"

                # 여러 얼굴로 판단시 첫번째 경우로 적용 혹은 첫번째와 가장 적은 차이점을 보이는 얼굴 사용
                face_distances = face_recognition.face_distance(known_face_encodings, face_encoding)
                best_match_index = np.argmin(face_distances)

                if matches[best_match_index]:
                    name = known_face_names[best_match_index]

                face_name.append(name)
                time.sleep(5)

                ## 인가자 비인가자 구분
                if name != 'Unknown':
                    print(name)
                    print("인가자입니다.")
                    #na = True
                    cnt_sucface += 1
                    if cnt_sucface > 2:
                        rtn = True
                        gname = name #이거 되는게 맞을까?
                        break
                if name == 'Unknown':
                    rtn = False
                    print("비인가자입니다.")
                    cnt_nonface += 1
                    print(cnt_nonface)
                    if (cnt_nonface%10)%2 == 0: # 비인가자 감지가 10번이상 되면
                        set_realtime('n', 'nan') #리얼타임 db에 업로드
                        execute_camera()
                        sendMessage("[경고]", "비정상적인 활동이 감지되었습니다.")
                        break
                    #na = False

            if rtn:
                break
            if rtn_exit:
                break
    
    if rtn_exit:
        continue
        

        process_this_frame = not process_this_frame

        if cv2.waitKey(1) & 0xFF == ord('q'): # 'q'로 나가기
            break

    # video_capture, cv2 종료
    video_capture.release()
    cv2.destroyAllWindows()

    '''모션인식'''
    rtn = False
    video_capture = cv2.VideoCapture(0)                             # 카메라 켜기

    if video_capture.isOpened() == 1:
        while(1):
            ret, img_ori = video_capture.read()

            if not ret:
                break

            img_ori = cv2.resize(img_ori, dsize=(0, 0), fx=0.5, fy=0.5)
            img = img_ori.copy()
            global gray
            gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
            faces = detector(gray)

            for face in faces:
                shapes = predictor(gray, face)
                shapes = face_utils.shape_to_np(shapes)

                eye_img_l, eye_rect_l = crop_eye(gray, eye_points=shapes[36:42])
                eye_img_r, eye_rect_r = crop_eye(gray, eye_points=shapes[42:48])

                eye_img_l = cv2.resize(eye_img_l, dsize=IMG_SIZE)
                eye_img_r = cv2.resize(eye_img_r, dsize=IMG_SIZE)
                eye_img_r = cv2.flip(eye_img_r, flipCode=1)

                cv2.imshow('l', eye_img_l)
                cv2.imshow('r', eye_img_r)

                eye_input_l = eye_img_l.copy().reshape((1, IMG_SIZE[1], IMG_SIZE[0], 1)).astype(np.float32) / 255.
                eye_input_r = eye_img_r.copy().reshape((1, IMG_SIZE[1], IMG_SIZE[0], 1)).astype(np.float32) / 255.

                pred_l = model.predict(eye_input_l)
                pred_r = model.predict(eye_input_r)

                global state_l

                # visualize
                state_l = 'O %.1f' if pred_l > 0.1 else '- %.1f'
                state_r = 'O %.1f' if pred_r > 0.1 else '- %.1f'

                state_l = state_l % pred_l
                state_r = state_r % pred_r

                cv2.rectangle(img, pt1=tuple(eye_rect_l[0:2]), pt2=tuple(eye_rect_l[2:4]), color=(255, 255, 255), thickness=2)
                cv2.rectangle(img, pt1=tuple(eye_rect_r[0:2]), pt2=tuple(eye_rect_r[2:4]), color=(255, 255, 255), thickness=2)

                cv2.putText(img, state_l, tuple(eye_rect_l[0:2]), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)
                cv2.putText(img, state_r, tuple(eye_rect_r[0:2]), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)

                cv2.imshow('result', img)

                try:
                    if state_l <= ('0.3'): #눈이 0.3이하로 감기면
                        rtn = True
                        set_realtime('y',gname) #리얼타임 db에 업로드
                        door_open()
                        print("문열림")
                        sendMessage("[문열림]", "문이 열렸습니다.")
                        break
                except Exception as e:
                    print("정상 작동되지 않습니다. 관리자에게 문의해주세요.")
                    print(e)

                if cv2. waitKey(1) == ord('q'):
                    break

            if rtn:
                break

    # video_capture, cv2 종료
    video_capture.release()
    cv2.destroyAllWindows()