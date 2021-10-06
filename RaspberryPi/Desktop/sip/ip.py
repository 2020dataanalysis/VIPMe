#	WiFi Presence Detection
#	pd.py
#	Logs to file any changes of WiFi presence.
#	Sam Portillo
#	09/05/2020

#	pipenv install
#	pipenv shell
#
#	Raspberry Pi 3:
#		https://installvirtual.com/how-to-install-python-3-8-on-raspberry-pi-raspbian/
#		sudo pip3 install getmac
#		from datetime import datetime
#		sudo pip3 install scapy
#		sudo python3 pd.py

#	Raspberry Pi 4:
#		No sudo or pip3 is needed.
#		pip intall getmac
#		pip install scapy
#		python ip.py

import os, time, sys, socket
import getmac
from scapy.all import *
livehosts=[]
networkaddr = ''
import datetime


def mytimeStamp():
	h = datetime.datetime.now().hour
	m = datetime.datetime.now().minute
	s = datetime.datetime.now().second
	hms = [h, m, s]
	return hms


def getmaclist():
	for x in range(1, 255):
		mac = getmac.get_mac_address( ip='10.0.0.' + str(x) )
		if mac != '00:00:00:00:00:00':
			print( '{}  {}'.format( x, mac ) )


def printArp( ip_start, ip_end ):
	global arpTable
	for i in range( ip_start, ip_end ):
		if arpTable[i][1]:
			print( '{} {}'.format( str(i).rjust(3, ' '), arpTable[i] ) )


def init_table( ip_start, ip_end ):
	global arpTable
	arpTable = {}

	for i in range( ip_start, ip_end ):
		arpTable[i] = ['', '', False]


def logPresenceDetection(x, message):
	#home = 'C:/Z/locate/ip'
	home = '/home/pi/Desktop'
	if not os.path.exists(home):
		os.makedirs(home)
	os.chdir(home)

	filename = time.strftime("%Y%m%d-%H") + '.txt'
	file = open(filename, 'a')
	timestamp = time.strftime("%m%d-%H%M%S")
	file.write('{} {} {}\n'.format( timestamp, x, message ))
	file.close()


#def pingIp(start_ip_last_octet, end_ip_last_octet):
def pingIp( networkaddr, ip_start, ip_end ):
	global arpTable

	for x in range( ip_start, ip_end ):
		packet = IP(dst=networkaddr+str(x))/ICMP()
		response = sr1(packet, timeout=1, verbose=0)

		hms = mytimeStamp()
		if not (response is None):
			if response[ICMP].type==0:
				print( networkaddr + str(x))
				mac = getmac.get_mac_address( ip=networkaddr + str(x) )
				entry = arpTable[x]
				if entry[1] and mac != entry[1]:
					logPresenceDetection( x, 'Pigeon Hole: New MAC address overwrite.')
				entry = [hms, mac, True ]
				arpTable[x] = entry
				logPresenceDetection( x, str( entry ) )

		else:
			#print( x )
			entry = arpTable[x]
			if entry[2]:
				entry[0] = hms
				entry[2] = False
				arpTable[x] = entry
				logPresenceDetection( x, str( entry ) )


if __name__ == '__main__':
	ip_prefix = '10.0.0.'
	ip_start = 1
	ip_end = 254
	init_table( ip_start, ip_end )

	while True:
		printArp(ip_start, ip_end)
		start_time = time.time()
		pingIp( ip_prefix, ip_start, ip_end )
		end_time = time.time() - start_time
		print('Processing {} numbers took {} seconds.'.format( ip_end - ip_start + 1, int(end_time) ))
