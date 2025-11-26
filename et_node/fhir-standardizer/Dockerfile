FROM python:3.13-slim
RUN pip install kafka-python fhir.resources python-dotenv requests faker
COPY fhir-standardizer.py /app/fhir-standardizer.py
RUN mkdir -p /etc/certs
COPY certs/ /app/certs
WORKDIR /app
CMD ["python", "-u", "fhir-standardizer.py"]
