import socket
import xml.etree.ElementTree as ET
import xmltodict, json
import sys

HOST = '127.0.0.1'  # Standard loopback interface address (localhost)
PORT = 12345        # Port to listen on (non-privileged ports are > 1023)
BUFFER_SIZE= 10000


datastore_collector = {}

def convert_to_json(data):
	str_data=data.decode("utf-8")
	#print("raw data: "+str_data);
	try:
		json_object = xmltodict.parse(str_data)
		#print(json.dumps(json_object, indent=4, sort_keys=True))
		conf_name=list(json_object.keys())[0]
		datastore_collector[conf_name]=json_object[conf_name]		
		print(json.dumps(datastore_collector, indent=4, sort_keys=True))
	except:
		print("Unable to convert data into JSON");
	

def start_socket_server():
	request_counter = 0
	# Create a TCP/IP socket
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

	# Bind the socket to the address given on the command line
	server_name = HOST
	server_address = (server_name, PORT)
	print("Starting socket server at port "+str(PORT))
	sock.bind(server_address)
	sock.listen(1)

	while True:
		try:
			print ("Waiting for a connection")
			connection, client_address = sock.accept()
			try:
				request_counter+=1
				print("client connected "+str(client_address))
				while True:
					data = connection.recv(BUFFER_SIZE)
					print(str(sys.getsizeof(data))+" bytes received.");
					if data:
						print("Request #"+str(request_counter))
						convert_to_json(data)
						break;
			finally:
				connection.close()

		except KeyboardInterrupt:
			print("Keyboard interrupt. Closing socket.")
			sock.close()
			break;


start_socket_server()
