FROM python:3.13-slim
RUN pip install kafka-python requests python-dotenv
COPY resp-cap-assess.py /app/resp-cap-assess.py
RUN mkdir -p /etc/certs
COPY certs/ /app/certs
WORKDIR /app
CMD ["python", "-u", "resp-cap-assess.py"]