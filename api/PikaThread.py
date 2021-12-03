import pika
import threading
import json

class PikaThread():
    result = ""
    received = False
    def callback(self, ch, method, properties, body):
        self.result = json.loads(body.decode("utf-8"))
        self.received = True

    def receive_command(self):
        connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
        channel = connection.channel()
 
        channel.basic_consume(
                    queue="resWiki", 
                    on_message_callback=self.callback, 
                    auto_ack=True)
        channel.start_consuming()
        
    

    


