import bluetooth
import threading
import random
from time import sleep
import sys

global bt_addr
sock_actuator = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
bt_addr = 'e4:5f:01:3c:92:eb'
port = 1
connected_act = False


def bluetooth_sensors():
    sock_sensor = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    port = 2
    connected_sensor = False
    print("Trying to connect to {} on PSM 0x{}...".format(bt_addr, port))    
    try:
        sock_sensor.connect((bt_addr, port))
        print("Connected to server sensors.")
        connected_sensor = True
    except:
        print("Not connected to server sensors.")
    
    if connected_sensor == True:
        while True:
            moisture = random.randint(0,99)
            x = random.uniform(-1, 1)
            y = random.uniform(-1, 1)
            z = random.uniform(-1, 1)
            sock_sensor.send("Moisture " + str(moisture) + " 15")
            #print("Moisture: " + str(moisture))
            sock_sensor.send("Accelerometer " + str(x) + " " + str(y) + " " + str(z) + " 15")
            #print("Accelerometer: "+ str(x) + " " + str(y) + " " + str(z))
            sleep(5)
        

    sock_actuator.close()
    sys.exit("Try again later")


s = threading.Thread(target = bluetooth_sensors)
s.start()


print("Trying to connect to {} on PSM 0x{}...".format(bt_addr, port))
try:
    sock_actuator.connect((bt_addr, port))

    print("Connected to server actuators. Type something...")
    connected_act = True

except:
    print("Not connected to server actuators.")

if connected_act == True:
    while True:
        
        data = input()
        if not data:
            data = "Nothing"
            #break
        #data = data + "-15"
        sock_actuator.send(data)
        data = sock_actuator.recv(1024)
        result = str(data).split("'")[1::2]
        print("Data recieved: ", str(result[0]))
    

sock_actuator.close()
sys.exit("Try again later")