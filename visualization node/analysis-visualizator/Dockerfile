FROM python:3.13-slim
RUN pip install streamlit python-dotenv matplotlib kafka-python
COPY analysis-visualizator.py /app/analysis-visualizator.py
RUN mkdir -p /etc/certs
COPY certs/ /app/certs
WORKDIR /app
CMD ["streamlit", "run", "analysis-visualizator.py"]