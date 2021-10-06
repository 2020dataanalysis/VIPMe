#   Sentinel Server VIP tracking application
#   PC Server to connect with Android app.
#   GPS-server.py aka i.py
#   https://github.com/2020dataanalysis/Sentinel_GPS_Server.git
#   09/17/2020
#   Sam Portillo

#   Since many people use iphones this app will not be used.
#   No looping.
#   Takes 1 command then exits
#   This can be used as part of VIPMe to verify clients.
#   so unknown hackers can not open your garage.
#   Keep track of other android users.

#   https://techwithtim.net/tutorials/socket-programming/
#   09/15/2020  →   Since the client is not halted by not receiving a response
#                   a response is not needed. → Enter d.py
#   Works with leave activity, come back, & continues to work with activity.

import socket 
import threading
import time

HEADER = 64
PORT = 3000
# SERVER = "10.0.0.34"    #socket.gethostbyname(socket.gethostname())
SERVER = socket.gethostbyname(socket.gethostname())
ADDR = (SERVER, PORT)
FORMAT = "utf-8"            # 'utf-8'

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(ADDR)

#connected = False
notification = False
notification_message = ''
#VIP_present  = False
print( SERVER )
VIP_dictionary = {'ff7acb10efd49e0a': 'Sammy'}
VIP_IN_list = []

def handle_client(conn, addr):
    global notification
    global notification_message
    connected = True

    # print('Open conn = {}'.format( conn ))
    print(f"[NEW CONNECTION] {addr} connected.")
    # sss = "Socket Connection to Raspberry Pi"
    # print( "Sending ", sss )
    # send(conn, sss)

    while connected:
        connected = False
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
            connected = False
            time.sleep(10)
            send(conn, 'This app will self destruct in 3 ...')
            time.sleep(3)
            continue

        if command == 'get_VIP':
            print('get_VIP')
            send(conn, "VIP=" + name)

        if command == 'Garage':
            print('Garage')

        if command == "VIP On Site":
            print("Welcome to BEAR !")
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

        print('.')


    conn.close()
    print('Socket Closed.')


def send( conn, m ):
    notification_message = m + "\r\n"
    conn.send(notification_message.encode(FORMAT))


def start():
    server.listen()
    print(f"Server is listening on {SERVER}")
    while True:
        conn, addr = server.accept()
        # print('type conn = {}'.format( type(conn)))
        thread = threading.Thread(target=handle_client, args=(conn, addr))
        thread.start()
        print(f"[ACTIVE CONNECTIONS] {threading.activeCount() - 1}")


start()