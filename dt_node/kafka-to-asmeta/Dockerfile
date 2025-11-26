FROM python:3.13-slim
RUN pip install kafka-python requests python-dotenv zmq
COPY kafka-to-asmeta.py /app/kafka-to-asmeta.py
RUN mkdir -p /etc/certs
COPY certs/ /app/certs
WORKDIR /app
CMD ["python", "-u", "kafka-to-asmeta.py"]