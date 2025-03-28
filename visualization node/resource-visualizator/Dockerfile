FROM python:3.11-slim
WORKDIR /app
RUN pip install flask python-dotenv requests gunicorn
COPY . .
EXPOSE 5000
CMD ["gunicorn", "--bind", "0.0.0.0:5000", "resource-visualizator:app"]