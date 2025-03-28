import logging
import os
import signal
import sys
import json
import time
import math
import requests
from kafka import KafkaProducer
from dotenv import load_dotenv
import urllib3

# Disable SSL certificate warnings
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# Logging configuration
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

# Load environment variables from the .env file
load_dotenv()
KEYCLOAK_BASE_URL = os.getenv("KEYCLOAK_BASE_URL")
REALM = os.getenv("REALM")
CLIENT_ID = os.getenv("ANALYTICS_CLIENT_ID")
CLIENT_SECRET = os.getenv("ANALYTICS_CLIENT_SECRET")
API_URL = os.getenv("FHIR_SERVER")
KAFKA_BROKER = os.getenv("KAFKA_BROKER")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC")
KAFKA_USERNAME = os.getenv("KAFKA_USERNAME")
KAFKA_PASSWORD = os.getenv("KAFKA_PASSWORD")

# Certificate file paths
KAFKA_SSL_TRUSTSTORE_PEM = "certs/client.truststore.pem"
CERT_PATH = os.getenv("CERT_PATH", "certs/cert.pem")
KEY_PATH = os.getenv("KEY_PATH", "certs/key.pem")

REQUEST_TIMEOUT = 10
EVALUATION_TIME = int(os.getenv("EVALUATION_TIME", 1))  # in seconds
DEFAULT_PATIENT_ID = 1

running = True  # Global flag to control the main loop


class TokenManager:
    """
    Singleton class for managing the authentication token.
    If the token is expired, a new one is requested.
    """
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(TokenManager, cls).__new__(cls)
            cls._instance.access_token = None
            cls._instance.expires_at = 0
        return cls._instance

    def get_access_token(self):
        """
        Returns the current access token, or fetches a new one if it has expired.
        """
        if self.access_token and time.time() < self.expires_at - 10:
            return self.access_token

        token_url = f"{KEYCLOAK_BASE_URL}/realms/{REALM}/protocol/openid-connect/token"
        payload = {
            "grant_type": "client_credentials",
            "client_id": CLIENT_ID,
            "client_secret": CLIENT_SECRET
        }
        try:
            response = requests.post(token_url, data=payload, cert=(CERT_PATH, KEY_PATH),
                                     verify=False, timeout=REQUEST_TIMEOUT)
            response.raise_for_status()
            token_data = response.json()
            self.access_token = token_data["access_token"]
            self.expires_at = time.time() + int(token_data.get("expires_in", 60))
            return self.access_token
        except requests.RequestException as e:
            logging.error(f"Error fetching token: {e}")
            return None


def call_api_with_token(token, query):
    """
    Makes a call to the FHIR server using the provided authentication token.
    """
    url = f"{API_URL}/{query}"
    headers = {
        "Authorization": f"Bearer {token}",
        "Cache-Control": "no-cache"
    }
    try:
        response = requests.get(url, headers=headers, cert=(CERT_PATH, KEY_PATH),
                                verify=False, timeout=REQUEST_TIMEOUT)
        response.raise_for_status()
        return response.json()
    except requests.RequestException as e:
        logging.error(f"API call error ({query}): {e}")
        return {}


def compute_otis_values(f_prev, weight, volMinPerc, rcexp_in):
    """
    Calculates the optimal respiratory frequency (f_opt) and lung volume (v_opt)
    based on the Otis model.
    """
    try:
        a_coeff = (2 * math.pi ** 2) / 60
        vd = 2.2 * weight / 1000
        volume_min = weight * 0.1 * (volMinPerc / 100)
        if vd == 0 or a_coeff * rcexp_in == 0:
            return f_prev, volume_min
        numerator = math.sqrt(1.0 + 2.0 * a_coeff * rcexp_in * (volume_min - f_prev * vd) / vd) - 1.0
        f_opt = numerator / (a_coeff * rcexp_in)
        if f_opt <= 0:
            f_opt = f_prev
        v_opt = volume_min / f_opt
        return f_opt, v_opt
    except Exception as e:
        logging.error(f"Error in Otis calculation: {e}")
        return f_prev, weight * 0.1


def extract_average_value(response, default_value, unit_conversion=False):
    """
    Extracts the average value from the observations retrieved from the FHIR server.
    If unit_conversion is True, converts the value (e.g., from mL to liters).
    """
    entries = response.get("entry", [])
    values = [
        entry.get("resource", {}).get("valueQuantity", {}).get("value")
        for entry in entries
        if entry.get("resource", {}).get("valueQuantity", {}).get("value") is not None
    ]
    if values:
        avg_value = sum(values) / len(values)
    else:
        avg_value = default_value
    if unit_conversion and avg_value != default_value:
        avg_value = avg_value / 1000  # Convert from mL to liters
    return avg_value


def check_safety(volume, frequency, vol_min=400, vol_max=1500, freq_min=5, freq_max=55, tol=1):
    """
    Controlla se il volume e la frequenza rientrano nel riquadro di sicurezza.
    
    Parametri:
      volume     - il volume corrente (ad es. in mL)
      frequency  - la frequenza respiratoria (ad es. in b/min)
      vol_min    - limite inferiore del volume sicuro
      vol_max    - limite superiore del volume sicuro
      freq_min   - limite inferiore della frequenza sicura
      freq_max   - limite superiore della frequenza sicura
      tol        - tolleranza in percentuale per determinare se un valore è sul bordo
      
    Ritorna:
      "normal"     se volume e frequenza sono strettamente all'interno dei limiti,
      "borderline" se uno dei valori è (quasi) uguale ad uno dei limiti,
      "critical"   se uno dei valori risulta fuori dai limiti di sicurezza.
    """
    # Controllo se volume o frequenza sono fuori dai limiti (con tolleranza)
    if (volume < vol_min - tol*vol_min/100) or (volume > vol_max + tol*vol_max/100) or (frequency < freq_min - tol*freq_min/100) or (frequency > freq_max + tol*freq_max/100):
        return "critical"
    
    # Controllo se volume o frequenza sono sul bordo (considerando la tolleranza)
    if (math.isclose(volume, vol_min, abs_tol=tol*vol_min/100) or math.isclose(volume, vol_max, abs_tol=tol*vol_max/100) or 
        math.isclose(frequency, freq_min, abs_tol=tol*freq_min/100) or math.isclose(frequency, freq_max, abs_tol=tol*freq_max/100)):
        return "borderline"
    
    # Se non è né critical né borderline, allora è normal
    return "normal"

def monitor_patient(patient_id, weight=70, vol_min_perc=30, rcexp_in=0.5, f_opt_past=12):
    """
    Retrieves the latest 8 observations for respiratory frequency and lung volume,
    calculates the average values, uses these averages to compute the optimal Otis curve,
    logs the information, and returns the result.
    """
    token = TokenManager().get_access_token()
    if not token:
        logging.error("Error: No token available.")
        return {}, None

    # Retrieve the last 8 respiratory frequency observations (code 9279-1)
    freq_query = f"/Observation?patient={patient_id}&code=9279-1&_sort=-date&_count=8"
    freq_response = call_api_with_token(token, freq_query)
    f_patient_avg = extract_average_value(freq_response, default_value=12)

    # Retrieve the last 8 lung volume observations (code 50115-7)
    vol_query = f"/Observation?patient={patient_id}&code=50115-7&_sort=-date&_count=8"
    vol_response = call_api_with_token(token, vol_query)
    v_patient_avg = extract_average_value(vol_response, default_value=0.5, unit_conversion=True)

    # Compute optimal values using the average respiratory frequency
    f_opt, v_opt = compute_otis_values(f_opt_past, weight, vol_min_perc, rcexp_in)

    # Evaluate actual values with optimal ones
    message = check_safety(v_patient_avg, f_patient_avg) 
    
    result = {
        "patient_id": patient_id,
        "patient_weight": weight,
        "actual_respiratorion_frequency": round(f_patient_avg, 3),
        "actual_lung_volume": round(v_patient_avg, 3),
        "optimal_respiratorion_frequency": round(f_opt, 3),
        "optimal_lung_volume": round(v_opt, 3),
        "message": message,
    }

    logging.info(json.dumps(result, indent=2))
    return json.dumps(result), f_opt


def handle_exit(sig, frame):
    """
    Signal handler for graceful termination. Sets the running flag to False.
    """
    global running
    logging.info("Termination in progress...")
    running = False


def main():
    """
    Main function: initializes the Kafka producer, retrieves the patient's weight,
    runs the monitoring loop, and sends messages to Kafka.
    """
    # Register signal handlers for termination
    signal.signal(signal.SIGINT, handle_exit)
    signal.signal(signal.SIGTERM, handle_exit)

    # Initialize the Kafka producer
    try:
        producer = KafkaProducer(
            bootstrap_servers=KAFKA_BROKER,
            security_protocol="SASL_SSL",
            sasl_mechanism="PLAIN",
            sasl_plain_username=KAFKA_USERNAME,
            sasl_plain_password=KAFKA_PASSWORD,
            ssl_cafile=KAFKA_SSL_TRUSTSTORE_PEM,
            ssl_check_hostname=False,
            value_serializer=lambda v: json.dumps(v).encode("utf-8")
        )
    except Exception as e:
        logging.error(f"Error connecting to Kafka: {e}")
        sys.exit(1)

    # Retrieve the patient's weight observation (code 29463-7)
    token = TokenManager().get_access_token()
    weight_query = f"/Observation?patient={DEFAULT_PATIENT_ID}&code=29463-7&_sort=-date&_count=1"
    weight_response = call_api_with_token(token, weight_query)
    weight = extract_average_value(weight_response, default_value=70)
    f_opt_past = 12
    # Main monitoring loop
    while running:
        try:
            message, f_opt_past = monitor_patient(
                patient_id=DEFAULT_PATIENT_ID,
                weight=weight,
                vol_min_perc=30,
                rcexp_in=0.5,
                f_opt_past=f_opt_past
            )
            producer.send(KAFKA_TOPIC, message)
            producer.flush()
            time.sleep(EVALUATION_TIME)
        except Exception as e:
            logging.error(f"Error during monitoring loop: {e}")

    # Close the Kafka producer upon termination
    producer.close()
    logging.info("Kafka producer closed.")


if __name__ == "__main__":
    main()