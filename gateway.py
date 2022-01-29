from time import sleep, strftime
from typing import Tuple
import board
from digitalio import DigitalInOut
from adafruit_character_lcd.character_lcd import Character_LCD_Mono
from datetime import datetime
import socket
import RPi.GPIO as GPIO  #Importamos el paquete RPi.GPIO y en el código nos refiriremos a el como GPIO
import threading
import bluetooth
import random
from paho.mqtt import client as mqtt_client
import adafruit_dht

global t_stop_buzzer

global tree_down
global tree_number
global first_line
global second_line
global temperature
global humidity
global lcd_command
global lcd_message_1
global lcd_message_2
global lcd_buzzer
global lcd_done
global lcd_number
global pump_command
global pump_number
global pump_command_stop
global topic_command_response
global topic_command
global topic_sensors
global soil_percentage
global b_moisture



b_moisture = "0.0"
soil_percentage = 50
gateway = "gateway1"
topic_command_response = "applefarm/"+gateway+"/command/response"
topic_command = "applefarm/"+gateway+"/command"
topic_sensors = "applefarm/"+gateway
pump_command = False
pump_number = 0
pump_command_stop = False
lcd_number = 0
lcd_buzzer = False
lcd_message_1 = ""
lcd_message_2 = ""
lcd_command = False
temperature = 0
humidity = 0
tree_number = 0
tree_down = False
pin_buzz = 18  #Variable que contiene el pin(GPIO.BCM) al cual conectamos la señal del LED
pin_led = 23

GPIO.setmode(GPIO.BCM)   #Establecemos el modo según el cual nos referiremos a los GPIO de nuestra RPi            
GPIO.setup(pin_buzz, GPIO.OUT) #Configuramos el GPIO18 como salida
GPIO.setup(pin_led, GPIO.OUT)
GPIO.output(pin_buzz, GPIO.LOW)
GPIO.output(pin_led, GPIO.LOW)

broker = "broker.hivemq.com" 
#broker = 'localhost'
port = 1883
client_id = f'python-mqtt-{random.randint(0, 1000)}'
client_id_gateway = f'python-mqtt-{random.randint(0, 1000)}'
sclient_id = f'python-mqtt-{random.randint(0, 1000)}'
username = 'architecture'
password = 'architecture123'


def connect_mqtt():
    def on_connect(client_mqtt, userdata, flags, rc):
        if rc == 0:
            print("INFO: Connected publisher to MQTT Broker!")
        else:
            print("INFO: Failed publisher to connect, return code %d\n", rc)

    client_mqtt = mqtt_client.Client(client_id)
    client_mqtt.username_pw_set(username, password)
    client_mqtt.on_connect = on_connect
    client_mqtt.connect(broker, port)
    return client_mqtt

def sconnect_mqtt() -> mqtt_client:
    def on_connect(sclient, userdata, flags, rc):
        if rc == 0:
            print("INFO: Connected subscriber to MQTT Broker!")
        else:
            print("INFO: Failed subscriber to connect, return code %d\n", rc)

    sclient = mqtt_client.Client(sclient_id)
    sclient.username_pw_set(username, password)
    sclient.on_connect = on_connect
    sclient.connect(broker, port)
    return sclient

def subscribe(sclient: mqtt_client):
   
    
    def on_message(sclient, userdata, msg):
        global lcd_message_1
        global lcd_message_2
        global lcd_command
        global tree_number
        global tree_down
        global lcd_number
        global pump_command
        global pump_command_stop
        global pump_number
        global soil_percentage
        stext = msg.payload.decode()
        
        result_splited = stext.split("-")

        size = len(result_splited)
        
        
        if result_splited[0] == "Buzzer":
            if size > 1 and size < 4:
                if result_splited[1] == "On":
                    if size > 2:
                        if buzzer_control(True, int(result_splited[2]),True) == True:
                            msg_broker = "Buzzer for tree: "+ result_splited[2] + " is On"
                            client_mqtt.publish(topic_command_response, msg_broker, 1)
                        else:
                            msg_broker = "Buzzer for tree: " + str(tree_number) +  "is alredy ON"
                            client_mqtt.publish(topic_command_response, msg_broker, 1)
                        
                    else:
                        msg_broker = "You must write which tree is down"
                        client_mqtt.publish(topic_command_response, msg_broker, 1)

                elif result_splited[1] == "Off":
                    if size > 2:
                        if buzzer_control(False, int(result_splited[2]),True) == True:             
                            msg_broker = "Buzzer for tree: "+ result_splited[2] + " is Off"
                            client_mqtt.publish(topic_command_response, msg_broker, 1)
                        else:
                            msg_broker = "The Buzzer is not for this tree is for tree: " + str(tree_number)
                            client_mqtt.publish(topic_command_response, msg_broker, 1)
                    

                else:
                    msg_broker = "Not a buzzer command"
                    client_mqtt.publish(topic_command_response, msg_broker, 1)
            else:
                msg_broker = "Not a correct number of commands for buzzer"
                client_mqtt.publish(topic_command_response, msg_broker, 1)

        elif result_splited[0] == "LCD":
            if size > 1 and size < 5:
                if result_splited[1] == "On":
                    if size > 2:
                        if tree_down == False:
                            if lcd_number == 0 or lcd_number == int(result_splited[3]):
                                lcd_number = int(result_splited[3])
                                lcd_message_1 = result_splited[2]
                                lcd_message_2 = result_splited[3]
                                lcd_command = True   
                                msg_broker = "LCD showing the message"
                                client_mqtt.publish(topic_command_response, msg_broker, 1)
                            else:
                                msg_broker = "There is another message from other tree showing" 
                                client_mqtt.publish(topic_command_response, msg_broker, 1)
                        else:
                            msg_broker = "A tree is down the message is not tree showing"
                            client_mqtt.publish(topic_command_response, msg_broker, 1)
                        
                    else:
                        msg_broker = "You must write a message" 
                        client_mqtt.publish(topic_command_response, msg_broker, 1) 

                elif result_splited[1] == "Off":
                    if lcd_number == int(result_splited[2]):
                        lcd_number = 0
                        lcd_command = False
                        msg_broker = "LCD stopped showing the message" 
                        client_mqtt.publish(topic_command_response, msg_broker, 1) 
                    else:                      
                        msg_broker = "The message is not from this tree is from: " + str(lcd_number)
                        client_mqtt.publish(topic_command_response, msg_broker, 1)

                else:
                    msg_broker = "Not a lcd command"
                    client_mqtt.publish(topic_command_response, msg_broker, 1)
            else:
                msg_broker = "Not a correct number of commands for LCD"
                client_mqtt.publish(topic_command_response, msg_broker, 1)

        elif result_splited[0] == "Moisture":
            soil_percentage = float(result_splited[1])
            msg_broker = "Changed minimum soil moisture for water pump"
            client_mqtt.publish(topic_command_response, msg_broker, 1)

        elif result_splited[0] == "Pump":
            if size > 1 and size < 5:
                if result_splited[1] == "On":
                    GPIO.output( pin_led , GPIO.HIGH )
                    pump_command = True
                    pump_number = result_splited[2]
                    msg_broker = "Pump working"
                    client_mqtt.publish(topic_command_response, msg_broker, 1)
                if result_splited[1] == "Off":
                    GPIO.output( pin_led , GPIO.LOW )
                    pump_command = False
                    pump_number = 0
                    msg_broker = "Pump stopped"
                    if result_splited[3] == "Yes":
                        pump_command_stop = True
                    if result_splited[3] == "No":
                        pump_command_stop = False
                    client_mqtt.publish(topic_command_response, msg_broker, 1)
            else:
                msg_broker = "Not a correct number of commands for pump"
                client_mqtt.publish(topic_command_response, msg_broker, 1)

        else:
            msg_broker = "Not a command"
            client_mqtt.publish(topic_command_response, msg_broker, 1)



    sclient.subscribe(topic_command, 1)
    sclient.on_message = on_message





def local_sensors():
    global temperature
    global humidity
    dhtDevice = adafruit_dht.DHT22(board.D4)
    while 1:
        try:
            temperature = dhtDevice.temperature
            #print("Temperature: " + str(temperature))
            humidity = dhtDevice.humidity
            #print("Humidity: " + str(humidity))
            client_mqtt.publish(topic_sensors, '{"moisture":' + b_moisture + ', "temperature":'+ str(temperature) + ', "humidity":' + str(humidity) + '}', 0)
            #client_mqtt.publish(topic_sensors, '{"humidity":' + str(humidity) +'}', 0)
            sleep(5)
        except RuntimeError as error:
        # Errors happen fairly often, DHT's are hard to read, just keep going
            sleep(5)
            continue
        except Exception as error:
            dhtDevice.exit()
            raise error

def get_ip_address():
    return [
             (s.connect(('8.8.8.8', 53)),
              s.getsockname()[0],
              s.close()) for s in
                  [socket.socket(socket.AF_INET, socket.SOCK_DGRAM)]
           ][0][1]
'''
def get_cpu_temp():
    tempFile = open("/sys/class/thermal/thermal_zone0/temp")
    cpu_temp = tempFile.read()
    tempFile.close()
    return float(cpu_temp)/1000

def get_cpu_speed():
    tempFile = open("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
    cpu_speed = tempFile.read()
    tempFile.close()
    return float(cpu_speed)/1000
'''

def lcd():
    # Modify this if you have a different sized character LCD
    lcd_columns = 16
    lcd_rows = 2

    lcd_rs = DigitalInOut(board.D26)
    lcd_en = DigitalInOut(board.D19)
    lcd_d4 = DigitalInOut(board.D13)
    lcd_d5 = DigitalInOut(board.D6)
    lcd_d6 = DigitalInOut(board.D5)
    lcd_d7 = DigitalInOut(board.D11)

    # Initialise the LCD class
    lcd = Character_LCD_Mono(
        lcd_rs, lcd_en, lcd_d4, lcd_d5, lcd_d6, lcd_d7, lcd_columns, lcd_rows
    )

    
    global lcd_done
    global lcd_command
    while 1:
        if tree_down == False and lcd_command == False:
            lcd_done = False
            for i in range(6):
                if tree_down == True or lcd_command == True:
                    break   
                lcd.clear()
                ip = get_ip_address()
                lcd.message = datetime.now().strftime('%b %d  %H:%M:%S\n')
                lcd.message = '\nIP {}'.format(ip)
                sleep(1)

            for i in range(6):
                if tree_down == True or lcd_command == True:
                    break   
                lcd.clear()
                lcd.message = "Temper.: " + str(round(temperature, 2)) + "C"
                lcd.message = "\nHumidity: " + str(round(humidity, 2)) + "%"
                sleep(1)
                
            '''
            for i in range(6):
                if tree_down == True or lcd_command == True:
                    break   
                lcd.clear()
                cpu_temp = get_ip_address()
                lcd.message = "CPU Temp: " + str(round(get_cpu_temp(), 2))
                lcd.message = "\nCPU Speed: " + str(int(get_cpu_speed()))
                sleep(2)
            '''    
        elif tree_down == True:
            if lcd_done == False or lcd_command == True:
                lcd.clear()
                lcd.message = "A tree is down!"
                lcd.message = "\nGo to tree: " + str(tree_number)
                lcd_done = True
                lcd_command = False

        elif lcd_command == True:
            if lcd_done == False:
                lcd.clear()
                lcd.message = lcd_message_1
                lcd.message = "\nFrom tree: " + lcd_message_2
                lcd_done = True

            

def buzzer():
    while 1:
        GPIO.output(pin_buzz, GPIO.HIGH)
        sleep(1)
        GPIO.output(pin_buzz, GPIO.LOW)
        sleep(1)
        if t_stop_buzzer:
            break

def stop_buzzer():
    GPIO.output(pin_buzz, GPIO.LOW)

def bluetooth_sensors():
    global pump_number
    global b_moisture

    host = ""
    port_sensor = 2
    server_sensor = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    print("INFO: Creating Bluetooth Socket for sensors")
    try:
        #server.bind((host, bluetooth.PORT_ANY))
        server_sensor.bind((host, port_sensor))
        print("INFO: Binding sensors complete")
    except:
        print("INFO: Binding sensors incomplete")

    server_sensor.listen(1)

    client_sensor, address_sensor = server_sensor.accept()
    print("INFO: Server sensor connected to: ", address_sensor)
    print("INFO: Client ", client_sensor)

    while 1:
        data = client_sensor.recv(1024)
        result = str(data).split("'")[1::2]

        result_splited = result[0].split()

        b_moisture = result_splited[0]
        b_x = result_splited[1]
        b_y = result_splited[2]
        b_z = result_splited[3]
        b_ph = result_splited[4]
        b_tree = result_splited[5]
        
        
          
        client_mqtt.publish(topic_sensors + "/tree" + b_tree, '{"x":' + b_x + ', "y":' + b_y + ', "z":' + b_z + ', ph:' +  b_ph + '}', 0)
        

        if float(b_moisture) < soil_percentage and pump_command_stop == False:
            GPIO.output( pin_led , GPIO.HIGH )
            pump_number = result_splited[2]
        elif float(b_moisture) >= soil_percentage and pump_command == False:
            GPIO.output( pin_led , GPIO.LOW )
            pump_number = 0

        if float(b_x) < -0.9 and tree_number == 0:
            buzzer_control(True, int(b_tree),False)

        elif float(b_x) >= -0.9 and tree_number == int(b_tree):
            buzzer_control(False, int(b_tree),False)

        
        
            

            
        
def buzzer_control(mode,number,lcd):
    global tree_number
    global tree_down
    global t_stop_buzzer
    global d
    global lcd_buzzer
    global lcd_done
    global lcd_number

    if mode == True:
        if tree_number == 0:
            t_stop_buzzer = False
            d = threading.Thread(target = buzzer)
            d.start()
            tree_down = True
            tree_number = number
            lcd_number = 0
            if lcd == True:
                lcd_buzzer = True
            return True
        elif lcd == True:
            if tree_number == number:
                lcd_buzzer = True
                return False

            lcd_done = False
            tree_number = number
            lcd_number = 0
            return True
        else: 
            return False
    
    else:
        if lcd_buzzer == False and number == tree_number:
            t_stop_buzzer = True
            d.join()
            stop_buzzer()
            tree_number = 0
            tree_down = False
            return True

        elif lcd_buzzer == True and number == tree_number:
            if lcd == True:
                t_stop_buzzer = True
                d.join()
                stop_buzzer()
                tree_number = 0
                tree_down = False
                lcd_buzzer = False
                return True
            
            else:
                return False
        else:
            return False


client_mqtt = connect_mqtt()
client_mqtt.loop_start()


sclient = sconnect_mqtt()
subscribe(sclient)
sclient.loop_start()

t = threading.Thread(target = lcd)
t.start()

r = threading.Thread(target = local_sensors)
r.start()

s = threading.Thread(target = bluetooth_sensors)
s.start()

host = ""
port_actuator = 1
server_actuator = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
print("INFO: Creating Bluetooth Socket for actuators")
try:
    #server.bind((host, bluetooth.PORT_ANY))
    server_actuator.bind((host, port_actuator))
    print("INFO: Binding actuators complete")
except:
    print("ERROR: Binding actuators incomplete")

server_actuator.listen(1)

client_actuator, address_actuator = server_actuator.accept()
print("INFO: Server actuator connected to: ", address_actuator)
print("INFO: Client ", client_actuator)


while 1:
    #b_input = int(input())
    data = client_actuator.recv(1024)
    result = str(data).split("'")[1::2]
    print("Data recieved for actuators: ", str(result[0]))
    
    result_splited = result[0].split("-")

    size = len(result_splited)
    
    
    if result_splited[0] == "Buzzer":
        if size > 1 and size < 4:
            if result_splited[1] == "On":
                if size > 2:
                    if buzzer_control(True, int(result_splited[2]),True) == True:
                        client_actuator.send("Buzzer for tree: "+ result_splited[2] + " is On")
                    else:
                        client_actuator.send("Buzzer for tree: " + str(tree_number) +  "is alredy ON")
                    
                else:
                    client_actuator.send("You must write which tree is down")

            elif result_splited[1] == "Off":
                if size > 2:
                    if buzzer_control(False, int(result_splited[2]),True) == True:             
                        client_actuator.send("Buzzer for tree: "+ result_splited[2] + " is Off")
                    else:
                        client_actuator.send("The Buzzer is not for this tree is for tree: " + str(tree_number))
                

            else:
                client_actuator.send("Not a buzzer command")
        else:
            client_actuator.send("Not a correct number of commands for buzzer")

    elif result_splited[0] == "LCD":
        if size > 1 and size < 5:
            if result_splited[1] == "On":
                if size > 2:
                    if tree_down == False:
                        if lcd_number == 0 or lcd_number == int(result_splited[3]):
                            lcd_number = int(result_splited[3])
                            lcd_message_1 = result_splited[2]
                            lcd_message_2 = result_splited[3]
                            lcd_command = True   
                            client_actuator.send("LCD showing the message")
                        else:
                            client_actuator.send("There is another message from other tree showing") 
                    else:
                        client_actuator.send("A tree is down the message is not tree showing")
                    
                else:
                    client_actuator.send("You must write a message")   

            elif result_splited[1] == "Off":
                if lcd_number == int(result_splited[2]):
                    lcd_number = 0
                    lcd_command = False
                    client_actuator.send("LCD stopped showing the message")   
                else:
                    client_actuator.send("The message is not from this tree is from: " + str(lcd_number)) 

            else:
                client_actuator.send("Not a lcd command")
        else:
            client_actuator.send("Not a correct number of commands for LCD")

    elif result_splited[0] == "Moisture":
        soil_percentage = float(result_splited[1])
        client_actuator.send("Changed minimum soil moisture for water pump")

    elif result_splited[0] == "Pump":
        if size > 1 and size < 5:
            if result_splited[1] == "On":
                GPIO.output( pin_led , GPIO.HIGH )
                pump_command = True
                pump_number = result_splited[2]
                client_actuator.send("Pump working")
            if result_splited[1] == "Off":
                GPIO.output( pin_led , GPIO.LOW )
                pump_command = False
                pump_number = 0
                if result_splited[3] == "Yes":
                    pump_command_stop = True
                if result_splited[3] == "No":
                    pump_command_stop = False
                client_actuator.send("Pump stopped")
        else:
            client_actuator.send("Not a correct number of commands for pump")

    else:
        client_actuator.send("Not a command")



    
            


client_actuator.close()
server_actuator.close()
