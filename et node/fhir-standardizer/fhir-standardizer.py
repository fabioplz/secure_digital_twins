from kafka import KafkaConsumer
import sys
import json
import re
import os
import logging
import random
from datetime import timezone, datetime
import requests
from fhir.resources.patient import Patient
from fhir.resources.humanname import HumanName
from fhir.resources.observation import Observation
from fhir.resources.quantity import Quantity
from fhir.resources.codeableconcept import CodeableConcept
from fhir.resources.coding import Coding
from fhir.resources.reference import Reference
from fhir.resources.bundle import Bundle, BundleEntry
from faker import Faker
from dotenv import load_dotenv
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# Load configuration from .env
load_dotenv()

KEYCLOAK_BASE_URL = os.getenv("KEYCLOAK_BASE_URL")
REALM = os.getenv("REALM")
CLIENT_ID = os.getenv("UPLOAD_CLIENT_ID")
CLIENT_SECRET = os.getenv("UPLOAD_CLIENT_SECRET")
FHIR_SERVER = os.getenv("FHIR_SERVER")
CERT_PATH = "certs/cert.pem"
KEY_PATH = "certs/key.pem"
REQUEST_TIMEOUT = 10

KAFKA_BROKER = os.getenv('KAFKA_BROKER')
KAFKA_TOPIC = os.getenv('KAFKA_TOPIC')

# LOINC codes mapping for observations
OBSERVATION_CODES = {
    # MISSING VALUE "Weight": "29463-7"
    "Heart Rate": "8867-4",
    "Total Lung Volume": "50115-7",
    "Respiration Rate": "9279-1",
    "Lead 3 Electric Potential": "19994-3",
    "Carbon Dioxide": "2002-4",
    "Arterial Pressure": "8480-6",
    "Airway Pressure": "2000-8",
    "Oxygen Saturation": "2708-6"
}

# Logger configuration
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

# Create session with SSL certificates
session = requests.Session()
session.cert = (CERT_PATH, KEY_PATH)
session.verify = False

# Faker setup for synthetic data
fake = Faker("it_IT")

# Global variable for the token
token_cache = {
    "access_token": None,
    "expires_at": 0
}

def get_access_token():
    """Request an OAuth2 access token from Keycloak and return the token and its lifespan in seconds."""
    url = f"{KEYCLOAK_BASE_URL}/realms/{REALM}/protocol/openid-connect/token"
    headers = {"Content-Type": "application/x-www-form-urlencoded"}
    payload = {
        "grant_type": "client_credentials",
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_SECRET
    }
    try:
        response = session.post(url, data=payload, headers=headers, timeout=REQUEST_TIMEOUT)
        response.raise_for_status()
        token_json = response.json()
        token = token_json.get("access_token")
        expires_in = token_json.get("expires_in", 3600)  # default: 1 hour
        logging.info("Access token obtained successfully.")
        return token, expires_in
    except requests.exceptions.RequestException as e:
        logging.error(f"Error while retrieving the token: {e}")
        return None, 0

def get_valid_token():
    """Return the stored token if valid, otherwise request a new token."""
    global token_cache
    now = datetime.now().timestamp()
    if token_cache.get("access_token") and now < token_cache.get("expires_at", 0):
        return token_cache["access_token"]
    else:
        token, expires_in = get_access_token()
        if token:
            # Set expiration with a 30-second tolerance
            token_cache["access_token"] = token
            token_cache["expires_at"] = now + expires_in - 30
            return token
        else:
            return None

def upload_fhir_resource(resource, resource_type, server_url, token):
    """Uploads a FHIR resource separately and returns its ID."""
    url = f"{server_url}/{resource_type}"
    headers = {"Content-Type": "application/json", "Authorization": f"Bearer {token}"}
    data = json.loads(resource.json(indent=4))
    try:
        response = session.post(url, headers=headers, json=data, timeout=REQUEST_TIMEOUT)
        response.raise_for_status()
        resource_id = response.json().get("id", None)
        logging.info(f"Resource {resource_type}/{resource_id} uploaded successfully.")
        return resource_id
    except requests.exceptions.RequestException as e:
        logging.error(f"Error while uploading {resource_type}: {e}")
        return None

def upload_fhir_bundle(bundle, server_url, token):
    """Sends a FHIR Bundle to the server and logs an identifier based on the timestamp."""
    bundle_id = datetime.now().strftime("%Y%m%d%H%M%S%f")  # Unique timestamp
    url = f"{server_url}"
    headers = {"Content-Type": "application/json", "Authorization": f"Bearer {token}"}
    data = json.loads(bundle.json(indent=4))
    try:
        response = session.post(url, headers=headers, json=data, timeout=REQUEST_TIMEOUT)
        response.raise_for_status()
        logging.info(f"Bundle {bundle_id} uploaded successfully.")
        return response.json()
    except requests.exceptions.RequestException as e:
        logging.error(f"Error while uploading bundle {bundle_id}: {e}")
        return None

def create_observation_resource(input_data, observation_name, patient_id):
    """Creates a FHIR Observation resource."""
    value = input_data["value"]
    unit = "%" if observation_name == "Oxygen Saturation" else input_data["unit"]
    code = OBSERVATION_CODES.get(observation_name, "unknown")
    display = re.sub(r'([a-z])([A-Z])', r'\1 \2', observation_name).capitalize()
    return Observation(
        status="final",
        code=CodeableConcept(
            coding=[Coding(system="http://loinc.org", code=code, display=display)]
        ),
        valueQuantity=Quantity(value=value, unit=unit),
        subject=Reference(reference=f"Patient/{patient_id}"),
        effectiveDateTime=datetime.now(timezone.utc).isoformat()
    )

def check_patient_exists(server_url, token):
    """Checks if the patient with fixed ID '1' exists in the FHIR server."""
    url = f"{server_url}/Patient/1"
    headers = {"Authorization": f"Bearer {token}"}

    try:
        response = session.get(url, headers=headers, timeout=REQUEST_TIMEOUT)
        if response.status_code == 200:
            logging.info("Patient 'Patient/1' found in the FHIR server.")
            return True
        elif response.status_code == 404:
            logging.info("Patient 'Patient/1' not found. It will be created.")
            return False
        else:
            logging.warning(f"Unexpected error while checking the patient: {response.status_code}")
            return False
    except requests.exceptions.RequestException as e:
        logging.error(f"Error while checking the patient: {e}")
        return False

def create_synthetic_patient():
    """Generates a synthetic patient with realistic data."""
    gender = random.choice(["male", "female"])
    first_name = fake.first_name_female() if gender == "female" else fake.first_name_male()
    last_name = fake.last_name()
    return Patient(
        active=True,
        name=[HumanName(use="official", family=last_name, given=[first_name])],
        gender=gender,
        birthDate=fake.date_of_birth(minimum_age=20, maximum_age=80)
    )

def process_pulse_observation_optimized(pulse_data, fhir_server_url, token, patient_id):
    """Processes measurement data and sends it in a bundle, associating the observations with the existing patient_id."""
    observations = {
        "Heart Rate": pulse_data["Patient Data"]["HeartRate"],
        "Total Lung Volume": pulse_data["Patient Data"]["TotalLungVolume"],
        "Respiration Rate": pulse_data["Patient Data"]["RespirationRate"],
        "Lead 3 Electric Potential": pulse_data["Patient Data"]["Lead3ElectricPotential"],
        "Carbon Dioxide": pulse_data["Patient Data"]["CarbonDioxide"],
        "Arterial Pressure": pulse_data["Patient Data"]["ArterialPressure"],
        "Airway Pressure": pulse_data["Patient Data"]["AirwayPressure"],
        "Oxygen Saturation": pulse_data["Patient Data"]["OxygenSaturation"]
    }
    # Create the bundle containing the observations
    bundle = Bundle(type="transaction")
    bundle.entry = [
        BundleEntry(
            fullUrl=f"urn:uuid:{random.randint(1000, 9999)}",
            resource=create_observation_resource(data, name, patient_id),
            request={"method": "POST", "url": "Observation"}
        ) for name, data in observations.items()
    ]
    upload_fhir_bundle(bundle, fhir_server_url, token)

if __name__ == "__main__":
    # Get a valid token (it will be automatically renewed when it expires)
    token = get_valid_token()
    if not token:
        logging.error("Unable to obtain the initial access token.")
        sys.exit(1)
    
    # Load the Patient resource only once outside the loop
    if check_patient_exists(FHIR_SERVER, token):
        patient_id = "1" 
    else:
        patient = create_synthetic_patient()
        patient_id = upload_fhir_resource(patient, "Patient", FHIR_SERVER, token)
        if not patient_id:
            logging.error("Error uploading the patient. Stopping the process.")
            sys.exit(1)
    
    # Start the Kafka consumer
    consumer = KafkaConsumer(KAFKA_TOPIC, bootstrap_servers=KAFKA_BROKER, security_protocol='PLAINTEXT')
    for message in consumer:
        raw_data = message.value.decode('utf-8')
        try:
            data = json.loads(raw_data)
        except json.JSONDecodeError:
            logging.warning(f"Invalid JSON message: {raw_data}")
            continue

        # Get a valid token for the current request (it will be renewed if necessary)
        token = get_valid_token()
        if not token:
            logging.error("Unable to obtain a valid access token for the current request.")
            continue

        process_pulse_observation_optimized(data, FHIR_SERVER, token, patient_id)