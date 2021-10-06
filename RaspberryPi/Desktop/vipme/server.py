import socket
import threading
import time
#import keyboard
# from pynput.keyboard import Key, Listener

import gpio_garage

HEADER = 64
# socket.gethostbyname(socket.gethostname())
# SERVER = "10.0.0.34"    	# Ultra
# PORT = 3000				# Ultra

# SERVER = "10.0.0.12"		#	PC Room
# PORT = 5000				#	PC Room

SERVER	= "10.0.0.124"			# VIPMe - Garage
PORT 	= 8070					# VIPMe - Garage

ADDR = (SERVER, PORT)
FORMAT = "utf-8"            # 'utf-8'

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.settimeout( 1 )
server.bind(ADDR)

print( SERVER )
VIP_dictionary = {'ff7acb10efd49e0a': 'Sammy',
'35f911411f4dc20a': 'Samsung',
'138c90e24e3fd29b': 'Mom'}


VIP_IN_list = []

last_garage_notification_status = False


def handle_client(conn, addr):
	global last_garage_notification_status

	# print('Open conn = {}'.format( conn ))
	print(f"[NEW CONNECTION] {addr} connected.")
	msg = conn.recv(1024).decode(FORMAT)
	msg = msg.strip()
	print( 'msg = [{}]'.format( msg ) )
	message     = msg.split("=")
	android_id  = message[0]
	command     = message[1]
	print(f"[{addr}] [{android_id}] [{command}]")
	send(conn, "server: " + command)

	if android_id in VIP_dictionary.keys():
		name = VIP_dictionary[android_id]

	else:
		#   Save android_id to a file.
		send(conn, 'You are not a registerd VIP.')
		send(conn, 'Call 925.705.0592')
		time.sleep(10)
		send(conn, 'This app will self destruct in 3 ...')
		time.sleep(3)

	if command == 'get_VIP':
		send(conn, "VIP=" + name)

	if command == "VIP On Site":
		VIP_IN_list.append( name )
		send(conn, 'Hi {}!  Welcome to BEAR !'.format(name))
		send(conn, 'VIP On Site')
		send(conn, 'Yeah ' + name + ' is here !')

	if command == "VIP Off Site":
		m = 'VIP has left the building.'
		print( m )
		VIP_IN_list.remove( name )
		send(conn, 'VIP Off Site')
		send(conn, f'{name} has left the building.')


	if command == 'Status_Event':
		m = f'Garage event Status = { gpio_garage.garage_open }'
		print( m )
		send(conn, m )


	if command == 'Status':
		b = gpio_garage.garage_status()
		send(conn, f'Garage Status = { b }')

	if command == 'Garage':
		gpio_garage.garage()
		send(conn, 'Toggle Garage')


	if command == 'Garage_Open':
		if gpio_garage.garage_open:
			send(conn, f'Garage is already open.')
		else:
			gpio_garage.garage()
			send(conn, f'Opening...')


	if command == 'Garage_Close':
		if not gpio_garage.garage_open:
			send(conn, f'Garage is already closed.')
		else:
			gpio_garage.garage()
			send(conn, f'Closing...')


	if gpio_garage.notification:
		gpio_garage.notification = False
		b = gpio_garage.garage_open
		if last_garage_notification_status != b:
			last_garage_notification_status = b
			send(conn, f'notification_status={ b }')

	print('.')



	time.sleep(1)
	conn.close()
	print('Socket Closed.')


def send( conn, m ):
	message = m + "\r\n"
	conn.send(message.encode(FORMAT))


def start():
	server.listen()
	# server.setblocking( False )
	# server.settimeout( 1 )
	print('Ctrl - c to exit')
	print(f"Server is listening on {SERVER}")
	while True:
		try:
			conn, addr = server.accept()
			thread = threading.Thread(target=handle_client, args=(conn, addr))
			thread.start()
			print(f"[ACTIVE CONNECTIONS] {threading.activeCount() - 1}")

		except socket.timeout as e:
			# print( e )
			pass
			# print('.', end='')

		# print('type conn = {}'.format( type(conn)))
		# print('.')


	server.shutdown(socket.SHUT_RDWR)
	server.close()
	print('Server down.')
#
# def on_press(key):
#     print('{0} pressed'.format(
#         key))
#
# def on_release(key):
#     print('{0} release'.format(
#         key))
#     if key == Key.esc:
#         # Stop listener
#         return False
#
#
# with Listener(
#         on_press=on_press,
#         on_release=on_release) as listener:
#     listener.join()
start()
