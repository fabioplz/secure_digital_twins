# Usa un'immagine base con Python
FROM python:3.9-slim

# Imposta la directory di lavoro
WORKDIR /app

# Copia i file necessari
COPY pulse-to-kafka.py .

# Installa le dipendenze
RUN pip install pyzmq kafka-python python-dotenv

# Esegui lo script
CMD ["python", "pulse-to-kafka.py"]
