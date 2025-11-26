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

# Logging configuration

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)

# Environment variables

KAFKA_BROKER = os.getenv("KAFKA_BROKER")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC")
KAFKA_USERNAME = os.getenv("KAFKA_USERNAME")
KAFKA_PASSWORD = os.getenv("KAFKA_PASSWORD")
KAFKA_SSL_TRUSTSTORE_PEM = "certs/client.truststore.pem"
ZMQ_BIND_ADDRESS = "tcp://*:5561"

# Kafka consumer setup with SASL_SSL like the producer

try:
    consumer = KafkaConsumer(
        KAFKA_TOPIC,
        bootstrap_servers=KAFKA_BROKER,
        auto_offset_reset="earliest",
        enable_auto_commit=True,
        value_deserializer=lambda m: m.decode("utf-8"),
        security_protocol="SASL_SSL",
        sasl_mechanism="PLAIN",
        sasl_plain_username=KAFKA_USERNAME,
        sasl_plain_password=KAFKA_PASSWORD,
        ssl_cafile=KAFKA_SSL_TRUSTSTORE_PEM,
        ssl_check_hostname=False
    )
except Exception as e:
    logging.error(f"Error connecting to Kafka: {e}")
    sys.exit(1)

# ZeroMQ publisher
context = zmq.Context()
socket = context.socket(zmq.PUB)
socket.bind(ZMQ_BIND_ADDRESS)

# Flag for clean shutdown
running = True

def handle_signal(sig, frame):
    """Handles clean shutdown of the program on SIGINT or SIGTERM."""
    global running
    logging.info("Interruption received, shutting down...")
    running = False
    consumer.close()
    socket.close()
    context.term()
    sys.exit(0)

signal.signal(signal.SIGINT, handle_signal)
signal.signal(signal.SIGTERM, handle_signal)

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
            time.sleep(0.01)  # Avoid flooding ZeroMQ
    except Exception as e:
        logging.error(f"Error during message processing: {e}")
        time.sleep(1)