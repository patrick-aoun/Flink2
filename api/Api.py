from fastapi import FastAPI
from fastapi.openapi.utils import get_openapi
import threading
import pika
import time
import PikaThread
import uvicorn
import sys

app = FastAPI()
thread = PikaThread.PikaThread()

t1 = threading.Thread(target=thread.receive_command)
t1.start()


@app.post("/search")
def search(keyword: str):
    if(thread.received):
        thread.received = False
        thread.result = ""
    with pika.BlockingConnection() as conn:
        channel = conn.channel()
        channel.basic_publish(
            exchange='',
            routing_key="searchWiki",
            body=keyword,
            properties=pika.BasicProperties(reply_to='searchWiki'))
    rcvd = thread.received
    while not rcvd:
        rcvd = thread.received
    return thread.result

def custom_openapi():
    if app.openapi_schema:
        return app.openapi_schema
    openapi_schema = get_openapi(
        title="Custom title",
        version="2.5.0",
        description="This is a very custom OpenAPI schema",
        routes=app.routes,
    )
    openapi_schema["info"]["x-logo"] = {
        "url": "https://fastapi.tiangolo.com/img/logo-margin/logo-teal.png"
    }
    app.openapi_schema = openapi_schema
    return app.openapi_schema


app.openapi = custom_openapi

@app.on_event("shutdown")
def shutdown_event():
    t1.join(0)



if __name__ == "__main__":
    
    uvicorn.run(app, port=8000, host="127.0.0.1") 
    