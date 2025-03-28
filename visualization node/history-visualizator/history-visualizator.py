import os
import io
import base64
import requests
import matplotlib.pyplot as plt
from flask import Flask, render_template, request
from dotenv import load_dotenv
from datetime import datetime
import urllib3

# Disable SSL warnings
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# Load environment variables
load_dotenv()

KEYCLOAK_BASE_URL = os.getenv("KEYCLOAK_BASE_URL")
REALM = os.getenv("REALM")
CLIENT_ID = os.getenv("VISUALIZATION_CLIENT_ID")
CLIENT_SECRET = os.getenv("VISUALIZATION_CLIENT_SECRET")
API_URL = os.getenv("FHIR_SERVER")
CERT_PATH = "certs/cert.pem"
KEY_PATH = "certs/key.pem"
REQUEST_TIMEOUT = 10

app = Flask(__name__)

# Observation codes (LOINC codes)
observation_codes = {
    "Heart Rate": "8867-4",
    "Total Lung Volume": "50115-7",
    "Respiration Rate": "9279-1",
    "Lead 3 Electric Potential": "19994-3",
    "Carbon Dioxide": "2002-4",
    "Arterial Pressure": "8480-6",
    "Airway Pressure": "2000-8",
    "Oxygen Saturation": "2708-6"
}

def get_access_token():
    """Get access token from Keycloak."""
    try:
        url = f"{KEYCLOAK_BASE_URL}/realms/{REALM}/protocol/openid-connect/token"
        payload = {
            "grant_type": "client_credentials",
            "client_id": CLIENT_ID,
            "client_secret": CLIENT_SECRET
        }
        response = requests.post(url, data=payload, cert=(CERT_PATH, KEY_PATH), verify=False, timeout=REQUEST_TIMEOUT)
        response.raise_for_status()
        return response.json().get("access_token")
    except requests.exceptions.RequestException as e:
        print(f"Authentication error: {e}")
        return None

def fetch_fhir_resource(resource_type, params=None):
    """Fetch FHIR resource using the access token."""
    token = get_access_token()
    if not token:
        return {"error": "Unable to obtain access token"}
    try:
        url = f"{API_URL}/{resource_type}"
        headers = {"Authorization": f"Bearer {token}", "Cache-Control": "no-cache"}
        response = requests.get(url, headers=headers, params=params, cert=(CERT_PATH, KEY_PATH), verify=False, timeout=REQUEST_TIMEOUT)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        return {"error": str(e)}

@app.route('/', methods=['GET'])
def index():
    """Render the search form and display the graph if parameters are provided."""
    image_data = None
    error = None
    patient_id = request.args.get("patient_id", "").strip()
    observation_code = request.args.get("observation_code", "").strip()
    
    if patient_id and observation_code:
        # Verify if the patient exists
        patient_response = fetch_fhir_resource(f"Patient/{patient_id}")
        if "error" in patient_response:
            error = f"Patient not found: {patient_response['error']}"
        else:
            # Fetch patient observations
            params = {
                "patient": patient_id,
                "code": observation_code,
                "_sort": "date"  # Sort by date
            }
            data = fetch_fhir_resource("Observation", params=params)

            if "error" in data:
                error = f"Fetch error: {data['error']}"
            else:
                dates = []
                values = []
                unit = None

                for entry in data.get("entry", []):
                    resource = entry.get("resource", {})
                    date_str = resource.get("effectiveDateTime") or resource.get("issued")
                    if date_str and "valueQuantity" in resource:
                        try:
                            dt = datetime.fromisoformat(date_str.replace("Z", "+00:00"))
                            dates.append(dt)
                            value = resource["valueQuantity"].get("value")
                            unit = resource["valueQuantity"].get("unit", "")
                            if value is not None:
                                values.append(float(value))
                        except Exception as e:
                            print(f"Date conversion error: {e}")

                if not dates or not values:
                    error = "No data found for this patient and observation type."
                else:
                    combined = sorted(zip(dates, values), key=lambda x: x[0])
                    sorted_dates, sorted_values = zip(*combined)

                    observation_label = next((key for key, value in observation_codes.items() if value == observation_code), observation_code)

                    plt.figure(figsize=(10, 5))
                    plt.plot(sorted_dates, sorted_values, marker='o', linestyle='-')
                    plt.xlabel("Date and Time")
                    plt.xticks(rotation=45)
                    plt.ylabel(f"{observation_label} ({unit})" if unit else observation_label)
                    plt.title(f"{observation_label} Trend for Patient {patient_id}")
                    plt.grid(True, linestyle="--", alpha=0.6)
                    plt.tight_layout()

                    buf = io.BytesIO()
                    plt.savefig(buf, format="png")
                    plt.close()
                    buf.seek(0)
                    image_data = base64.b64encode(buf.getvalue()).decode("utf-8")

    return render_template("index.html", observation_codes=observation_codes, image_data=image_data, error=error, patient_id=patient_id, observation_code=observation_code)

if __name__ == '__main__':
    app.run(debug=True)