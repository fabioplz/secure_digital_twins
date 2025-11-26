import logging
import os
import signal
import sys
from kafka import KafkaConsumer
import zmq
import time
from dotenv import load_dotenv
import json

# Load environment variables
load_dotenv()

# Console logging configuration
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)

# Get environment variables from .env
KAFKA_BROKER = os.getenv('KAFKA_BROKER')
KAFKA_TOPIC = os.getenv('KAFKA_TOPIC')
ZMQ_BIND_ADDRESS = "tcp://*:5561"

# Kafka consumer configuration
try:
    consumer = KafkaConsumer(
        KAFKA_TOPIC,
        bootstrap_servers=KAFKA_BROKER,
        auto_offset_reset='earliest',
        enable_auto_commit=True,
        value_deserializer=lambda m: m.decode('utf-8'),
        security_protocol='PLAINTEXT'
    )
except Exception as e:
    logging.error(f"Error connecting to Kafka: {e}")
    sys.exit(1)

# ZeroMQ publisher configuration
context = zmq.Context()
socket = context.socket(zmq.PUB)
socket.bind(ZMQ_BIND_ADDRESS)  # Bind PUB socket

# Flag for safe termination
running = True

def handle_signal(sig, frame):
    """Handles clean shutdown of the program on SIGINT (Ctrl+C)."""
    global running
    logging.info("Interruption received, shutting down the program...")
    running = False
    consumer.close()
    socket.close()
    context.term()
    sys.exit(0)

# Intercept SIGINT (Ctrl+C)
signal.signal(signal.SIGINT, handle_signal)

logging.info("Kafka -> ZeroMQ bridge started...")

while running:
    try:
        for message in consumer:
            payload = message.value
            socket.send_string(payload)
            try:
                parsed = json.loads(payload)
                logging.info(json.dumps(parsed, indent=2))
            except json.JSONDecodeError:
                logging.info(f"Raw message: {payload}")
            time.sleep(0.01)  # Breve pausa per evitare flooding su ZMQ
    except Exception as e:
        logging.error(f"Error during message processing: {e}")
        time.sleep(1)