import RPi.GPIO as GPIO
import time

def button_handler(pin):
	print("pin %s's value is %s" % (pin, GPIO.input(pin)))

if __name__ == '__main__':
	button_pin = 17

	GPIO.setmode(GPIO.BCM)

	#	No resistors:
	# GPIO.setup(button_pin, GPIO.IN, pull_up_down = GPIO.PUD_DOWN)		#	This does not detect anything.
	GPIO.setup(button_pin, GPIO.IN, pull_up_down = GPIO.PUD_UP)

    # events can be GPIO.RISING, GPIO.FALLING, or GPIO.BOTH
	GPIO.add_event_detect(button_pin, GPIO.BOTH,
                          callback=button_handler,
                          bouncetime=300)

	while True:
		if GPIO.input(button_pin):     # if port 25 == 1
			print ("High")
		else:                  # if port 25 != 1
			print ( "Low" )

		time.sleep(1)


	GPIO.cleanup()
	print('done.')
