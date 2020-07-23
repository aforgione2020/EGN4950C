from flask import Flask
from flask import jsonify
from picamera import PiCamera
from time import sleep
import random
import string

app = Flask(__name__)
camera = PiCamera()

@app.route('/')
def index():
    imgName = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(8))
    camera.start_preview()
    sleep(5)
    camera.capture('static/' + imgName + '.jpg')
    camera.stop_preview()
    return jsonify(imageName= imgName + '.jpg')

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
