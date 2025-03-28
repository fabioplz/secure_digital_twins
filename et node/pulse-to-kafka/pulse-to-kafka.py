import logging
import os
import signal
import sys
from kafka import KafkaProducer
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
ZMQ_SERVER = os.getenv('ZMQ_SERVER')

# Kafka configuration
try:
    producer = KafkaProducer(bootstrap_servers=KAFKA_BROKER, security_protocol='PLAINTEXT')
except Exception as e:
    logging.error(f"Error connecting to Kafka: {e}")
    sys.exit(1)

# ZeroMQ configuration
context = zmq.Context()
socket = context.socket(zmq.SUB)
socket.connect(ZMQ_SERVER)  # Connecting to ZeroMQ server
socket.setsockopt_string(zmq.SUBSCRIBE, "")  # Subscribe to all messages

# Flag for safe termination
running = True

def handle_signal(sig, frame):
    """Handles clean shutdown of the program on SIGINT (Ctrl+C)."""
    global running
    logging.info("Interruption received, shutting down the program...")
    running = False
    socket.close()
    context.term()
    producer.flush()
    producer.close()
    sys.exit(0)

# Intercept SIGINT (Ctrl+C)
signal.signal(signal.SIGINT, handle_signal)

logging.info("ZeroMQ -> Kafka bridge started...")

while running:
    try:
        message = socket.recv_string(flags=zmq.NOBLOCK)  # Avoid infinite blocking
        if message.startswith('Server output'):
            message = message.replace('Server output ', '', 1)
        producer.send(KAFKA_TOPIC, message.encode("utf-8"))
        producer.flush()  # Ensure the message is sent
        message = json.loads(message)
        logging.info(json.dumps(message, indent=2))
    except zmq.Again:
        time.sleep(0.1)  # Wait before retrying if there are no messages
    except Exception as e:
        logging.error(f"Error during reception or sending: {e}")