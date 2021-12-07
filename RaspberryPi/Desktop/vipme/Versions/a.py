#	Basic usage of the Raspberry Pi GPIO pins
#		https://www.youtube.com/watch?v=U6N5pRDOrg4
import RPi.GPIO as GPIO
import time

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)

GPIO.setup(17, GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(18, GPIO.OUT, initial=GPIO.LOW)


def doit(ev=None):
	GPIO.output(18, GPIO.HIGH)
	time.sleep(1)
	GPIO.output(18, GPIO.LOW)


GPIO.add_event_detect(17, GPIO.FALLING, callback=doit, bouncetime=300 )

while True:
	time.sleep(1)
