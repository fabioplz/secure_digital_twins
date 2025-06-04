import logging
import os
import signal
import sys
import json
import time
import math
from kafka import KafkaProducer
from dotenv import load_dotenv

# Logging configuration
logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s"
)

# Load environment variables from the .env file
load_dotenv()
KAFKA_BROKER = os.getenv("KAFKA_BROKER")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC")
KAFKA_USERNAME = os.getenv("KAFKA_USERNAME")
KAFKA_PASSWORD = os.getenv("KAFKA_PASSWORD")
KAFKA_SSL_TRUSTSTORE_PEM = "certs/client.truststore.pem"

print(f"KBroker {KAFKA_BROKER}")

# Static input configuration
EVALUATION_TIME = int(os.getenv("EVALUATION_TIME", 1))  # in seconds
DEFAULT_PATIENT_ID = 1
STATIC_FREQUENCY = 14.0  # respiri/minuto
STATIC_VOLUME = 0.6  # litri
STATIC_WEIGHT = 70  # kg

running = True  # Global flag to control the main loop


def compute_otis_values(f_prev, weight, volMinPerc, rcexp_in):
    """
    Calcola la frequenza respiratoria e il volume ottimali secondo il modello di Otis.
    """
    try:
        a_coeff = (2 * math.pi**2) / 60
        vd = 2.2 * weight / 1000
        volume_min = weight * 0.1 * (volMinPerc / 100)
        if vd == 0 or a_coeff * rcexp_in == 0:
            return f_prev, volume_min
        numerator = (
            math.sqrt(1.0 + 2.0 * a_coeff * rcexp_in * (volume_min - f_prev * vd) / vd)
            - 1.0
        )
        f_opt = numerator / (a_coeff * rcexp_in)
        if f_opt <= 0:
            f_opt = f_prev
        v_opt = volume_min / f_opt
        return f_opt, v_opt
    except Exception as e:
        logging.error(f"Errore nel calcolo Otis: {e}")
        return f_prev, weight * 0.1


def check_safety(
    volume, frequency, vol_min=400, vol_max=1500, freq_min=5, freq_max=55, tol=1
):
    """
    Classifica la condizione respiratoria in base ai limiti di sicurezza.
    """
    if (
        (volume * 1000 < vol_min - tol * vol_min / 100)
        or (volume * 1000 > vol_max + tol * vol_max / 100)
        or (frequency < freq_min - tol * freq_min / 100)
        or (frequency > freq_max + tol * freq_max / 100)
    ):
        return "critical"

    if (
        math.isclose(volume * 1000, vol_min, abs_tol=tol * vol_min / 100)
        or math.isclose(volume * 1000, vol_max, abs_tol=tol * vol_max / 100)
        or math.isclose(frequency, freq_min, abs_tol=tol * freq_min / 100)
        or math.isclose(frequency, freq_max, abs_tol=tol * freq_max / 100)
    ):
        return "borderline"

    return "normal"


def monitor_static_patient(
    weight=STATIC_WEIGHT, vol_min_perc=30, rcexp_in=0.5, f_opt_past=12
):
    """
    Usa valori statici per frequenza e volume per simulare un monitoraggio.
    """
    f_patient_avg = STATIC_FREQUENCY
    v_patient_avg = STATIC_VOLUME

    f_opt, v_opt = compute_otis_values(f_opt_past, weight, vol_min_perc, rcexp_in)
    message = check_safety(v_patient_avg, f_patient_avg)

    result = {
        "static_frequency": round(f_patient_avg, 3),
        "static_volume": round(v_patient_avg, 3),
        "optimal_respiratory_frequency": round(f_opt, 3),
        "optimal_lung_volume": round(v_opt, 3),
        "safety_status": message,
    }

    logging.info(json.dumps(result, indent=2))
    return json.dumps(result), f_opt


def handle_exit(sig, frame):
    global running
    logging.info("Terminazione in corso...")
    running = False


def main():
    signal.signal(signal.SIGINT, handle_exit)
    signal.signal(signal.SIGTERM, handle_exit)

    try:
        producer = KafkaProducer(
            bootstrap_servers=KAFKA_BROKER,
            security_protocol="SASL_SSL",
            sasl_mechanism="PLAIN",
            sasl_plain_username=KAFKA_USERNAME,
            sasl_plain_password=KAFKA_PASSWORD,
            ssl_cafile=KAFKA_SSL_TRUSTSTORE_PEM,
            ssl_check_hostname=False,
            value_serializer=lambda v: v.encode("utf-8"),
        )
    except Exception as e:
        logging.error(f"Errore nella connessione a Kafka: {e}")
        sys.exit(1)

    f_opt_past = 12

    while running:
        try:
            message, f_opt_past = monitor_static_patient(
                weight=STATIC_WEIGHT,
                vol_min_perc=30,
                rcexp_in=0.5,
                f_opt_past=f_opt_past,
            )
            producer.send(KAFKA_TOPIC, message)
            producer.flush()
            time.sleep(EVALUATION_TIME)
        except Exception as e:
            logging.error(f"Errore nel loop di monitoraggio: {e}")

    producer.close()
    logging.info("Kafka producer chiuso.")


if __name__ == "__main__":
    main()
