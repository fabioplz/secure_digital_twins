FROM python:3.13-slim
RUN pip install streamlit python-dotenv matplotlib kafka-python
COPY fhir-analyzer-client.py /app/fhir-analyzer-client.py
RUN mkdir -p /etc/certs
COPY certs/ /app/certs
WORKDIR /app
CMD ["streamlit", "run", "fhir-analyzer-client.py"]