FROM python:3.10-slim

# Instalar SWI-Prolog a nivel de sistema operativo
RUN apt-get update && apt-get install -y swi-prolog && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . /app

RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 5000
CMD ["python", "app.py"]