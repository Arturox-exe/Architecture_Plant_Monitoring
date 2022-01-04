import bluetooth
import threading
import random
from time import sleep
import sys
global tree
global bt_addr
import smbus			

tree = "15"

sock_actuator = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
bt_addr = 'e4:5f:01:3c:92:eb'
port = 1
connected_act = False


PWR_MGMT_1   = 0x6B
SMPLRT_DIV   = 0x19
CONFIG       = 0x1A
GYRO_CONFIG  = 0x1B
INT_ENABLE   = 0x38
ACCEL_XOUT_H = 0x3B
ACCEL_YOUT_H = 0x3D
ACCEL_ZOUT_H = 0x3F
GYRO_XOUT_H  = 0x43
GYRO_YOUT_H  = 0x45
GYRO_ZOUT_H  = 0x47

def MPU_Init():
	#write to sample rate register
	bus.write_byte_data(Device_Address, SMPLRT_DIV, 7)
	
	#Write to power management register
	bus.write_byte_data(Device_Address, PWR_MGMT_1, 1)
	
	#Write to Configuration register
	bus.write_byte_data(Device_Address, CONFIG, 0)
	
	#Write to Gyro configuration register
	bus.write_byte_data(Device_Address, GYRO_CONFIG, 24)
	
	#Write to interrupt enable register
	bus.write_byte_data(Device_Address, INT_ENABLE, 1)

def read_raw_data(addr):
	#Accelero and Gyro value are 16-bit
        high = bus.read_byte_data(Device_Address, addr)
        low = bus.read_byte_data(Device_Address, addr+1)
    
        #concatenate higher and lower value
        value = ((high << 8) | low)
        
        #to get signed value from mpu6050
        if(value > 32768):
                value = value - 65536
        return value

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

            acc_x = read_raw_data(ACCEL_XOUT_H)
            acc_y = read_raw_data(ACCEL_YOUT_H)
            acc_z = read_raw_data(ACCEL_ZOUT_H)

            x = acc_x/16384.0
            y = acc_y/16384.0
            z = acc_z/16384.0

            sock_sensor.send("Moisture " + str(moisture) + " " + tree)
            #print("Moisture: " + str(moisture))
            sock_sensor.send("Accelerometer " + str(x) + " " + str(y) + " " + str(z) + " " + tree)
            #print("Accelerometer: "+ str(x) + " " + str(y) + " " + str(z))
            sleep(5)
        

    sock_actuator.close()
    sys.exit("Try again later")

bus = smbus.SMBus(1) 	# or bus = smbus.SMBus(0) for older version boards
Device_Address = 0x68   # MPU6050 device address

MPU_Init()

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
        #data = data + "-" + tree
        sock_actuator.send(data)
        data = sock_actuator.recv(1024)
        result = str(data).split("'")[1::2]
        print("Data recieved: ", str(result[0]))
    

sock_actuator.close()
sys.exit("Try again later")