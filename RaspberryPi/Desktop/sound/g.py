import time
from subprocess import call
# call(["aplay", "/home/pi/Desktop/sound/graham.wav"])
time.sleep(10)
call(["aplay", "ebcrosby.wav"])
