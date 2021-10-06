import RPi.GPIO as GPIO
import time

PIN_IN	= 17
PIN_OUT	= 4
GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)
# GPIO.setup(PIN_IN, GPIO.IN, pull_up_down=GPIO.PUD_UP)
# GPIO.setup(PIN_IN, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)
GPIO.setup(PIN_IN, GPIO.IN)
GPIO.setup(PIN_OUT, GPIO.OUT, initial=GPIO.LOW)

notification 	= False
garage_open 	= False
command 		= ''


def garage_event(pin):
	global garage_open
	global notification

	notification = True
	v = GPIO.input(pin)
	print("pin %s's value is %s" % (pin, v ))
	garage_open = bool( v )


def garage_status():
	global garage_open

	v = GPIO.input(PIN_IN)
	garage_open = bool( v )		#	Pull up resistor → not
	return garage_open


def garage():
	print('Toggle Garage')
	GPIO.output(PIN_OUT, GPIO.HIGH)
	time.sleep(1)
	GPIO.output(PIN_OUT, GPIO.LOW)


def exit():
	GPIO.cleanup()


# GPIO.add_event_detect(PIN_IN, GPIO.BOTH, callback=garage_event, bouncetime=300 )			#	80% success
GPIO.add_event_detect(PIN_IN, GPIO.BOTH, callback=garage_event )							#	100% success but needs initial correct status
#GPIO.add_event_detect(PIN_IN, GPIO.BOTH, callback=garage_event, bouncetime=1000 )

garage_status()
