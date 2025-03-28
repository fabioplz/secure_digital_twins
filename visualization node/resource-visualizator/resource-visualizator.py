import os
import json
import requests
from flask import Flask, render_template, request, jsonify
from dotenv import load_dotenv
import urllib3

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

def get_access_token():
    """Gets the access token from Keycloak."""
    url = f"{KEYCLOAK_BASE_URL}/realms/{REALM}/protocol/openid-connect/token"
    payload = {
        "grant_type": "client_credentials",
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_SECRET
    }
    response = requests.post(url, data=payload, cert=(CERT_PATH, KEY_PATH), verify=False, timeout=REQUEST_TIMEOUT)
    response.raise_for_status()
    return response.json().get("access_token")

def fetch_fhir_resource(resource_type):
    """Makes a request to the FHIR server with the access token."""
    try:
        token = get_access_token()
        url = f"{API_URL}/{resource_type}"
        headers = {"Authorization": f"Bearer {token}", "Cache-Control": "no-cache"}
        response = requests.get(url, headers=headers, cert=(CERT_PATH, KEY_PATH), verify=False, timeout=REQUEST_TIMEOUT)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        return {"error": str(e)}

@app.route('/')
def index():
    """Displays the main page."""
    return render_template('index.html')

@app.route('/fetch_resource', methods=['POST'])
def fetch_resource():
    """Endpoint to request FHIR resources."""
    resource_type = request.form.get("resource_type", "").strip()
    if not resource_type:
        return jsonify({"error": "The query field is empty."}), 400

    result = fetch_fhir_resource(resource_type)
    return jsonify(result)

if __name__ == '__main__':
    app.run(debug=True)