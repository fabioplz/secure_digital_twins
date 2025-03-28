import streamlit as st
import matplotlib.pyplot as plt
import time
import json
import logging
from kafka import KafkaConsumer
import os
from dotenv import load_dotenv

# Logging configuration
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)
load_dotenv()

# Kafka Configuration
KAFKA_BROKER = os.getenv("KAFKA_BROKER")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC")
KAFKA_USERNAME = os.getenv("KAFKA_USERNAME")
KAFKA_PASSWORD = os.getenv("KAFKA_PASSWORD")

# Certificate file paths
KAFKA_SSL_TRUSTSTORE_PEM = "certs/client.truststore.pem"

# Create Kafka consumer
consumer = KafkaConsumer(
    KAFKA_TOPIC,
    bootstrap_servers=KAFKA_BROKER,
    security_protocol="SASL_SSL",
    sasl_mechanism="PLAIN",
    sasl_plain_username=KAFKA_USERNAME,
    sasl_plain_password=KAFKA_PASSWORD,
    ssl_cafile=KAFKA_SSL_TRUSTSTORE_PEM,
    ssl_check_hostname=False,
    value_deserializer=lambda x: x.decode('utf-8')
)

# ------------------ Global Variables ------------------ #
history_f = []
history_v = []
history_f_opt = []
history_v_opt = []

# ------------------ Streamlit Interface ------------------ #
st.title("OTIS Monitoring with Kafka")

# Placeholder for the plot and alarm message
plot_placeholder = st.empty()
alert_placeholder = st.empty()

# Loop to receive data from Kafka and update the plot
for message in consumer:    
    data = json.loads(json.loads(message.value))
    logging.info(f"Message received: {data}")

    patient_id = data.get("patient_id", 1)
    weight_global = data.get("patient_weight", 70)
    f_patient = data.get("actual_respiratorion_frequency", 12)
    v_patient = data.get("actual_lung_volume", 0.5)
    f_opt_global = data.get("optimal_respiratorion_frequency", 12)
    v_opt_global = data.get("optimal_lung_volume", 0.5)
    alert_message = data.get("message", "normal")

    # Update data history
    history_f.append(f_patient)
    history_v.append(v_patient)
    history_f_opt.append(f_opt_global)
    history_v_opt.append(v_opt_global)
    
    # Keep only the last 50 data points to avoid overload
    if len(history_f) > 50:
        history_f.pop(0)
        history_v.pop(0)
        history_f_opt.pop(0)
        history_v_opt.pop(0)
    
    # Set alert message color based on severity
    if alert_message == "critical":
        alert_color = "red"
    elif alert_message == "warning":
        alert_color = "yellow"
    else:
        alert_color = "green"

    # Generate the alarm message
    alert_placeholder.markdown(f"<h2 style='text-align: center; color: {alert_color};'>{alert_message.upper()}</h2>", unsafe_allow_html=True)
    
    # Create the plot with two subplots (larger size)
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(16, 8))  # Increase figure size
    ax1.plot(history_f, label="Real Frequency", marker='o', linestyle='--', color='#E74C3C')
    ax1.plot(history_f_opt, label="Optimal Frequency", marker='s', linestyle='-', color='#3498DB')
    ax1.set_title("Respiratory Frequency")
    ax1.set_xlabel("Cycle")
    ax1.set_ylabel("Brady/min")
    ax1.legend(loc="upper right")
    ax1.grid(True)

    ax2.plot(history_v, label="Real Volume", marker='o', linestyle='--', color='#27AE60')
    ax2.plot(history_v_opt, label="Optimal Volume", marker='s', linestyle='-', color='#9B59B6')
    ax2.set_title("Lung Volume")
    ax2.set_xlabel("Cycle")
    ax2.set_ylabel("Liters")
    ax2.legend(loc="upper right")
    ax2.grid(True)

    # Update the plot in the Streamlit placeholder
    plot_placeholder.pyplot(fig) 
    
    time.sleep(0.5)
