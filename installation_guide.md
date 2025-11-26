# Digital Twins Healthcare Pipeline – Installation & Execution Guide

This document provides a complete setup and execution guide for all components of the **Digital Twins Healthcare Pipeline**, including:

* **BREATHE** (physiological simulator)
* **Storage Node** (Keycloak, FHIR server, object storage)
* **ET Node** (data ingestion pipeline)
* **DT Node** (digital twin analytics)
* **Visualization Node** (dashboards and visual tools)

---

## 1. Pre-requisites

Before installing and running the system, ensure the following software is installed:

1. **Docker Desktop**
   Verify installation:

   ```bash
   ```

docker run hello-world

````

2. **Python & pip**

3. **Java 23**

4. **Maven**  
   Verify installation:  
   ```bash
mvn --help
````

> **Note:** Docker Desktop **must be running** whenever you use `docker` or `docker-compose`.

---

## 2. BREATHE – Installation & Startup

### 2.1 Install Pulse dependency

Navigate to:

```
et node/BREATHE/breathe.engine
```

Install the Pulse library:

```bash
mvn install:install-file \
  -Dfile=jar/Pulse.jar \
  -DgroupId=breathe \
  -DartifactId=pulse \
  -Dversion=1.0 \
  -Dpackaging=jar
```

### 2.2 Build BREATHE engine

```bash
mvn clean install
```

### 2.3 Build all BREATHE submodules

Run in each module directory (`breathe.engine`, `breathe.web`, `breathe.swingGUI`, `breathe.extVentilator`):

```bash
mvn clean install
```

### 2.4 Run the BREATHE Web Interface

Inside:

```
et node/BREATHE/breathe.web
```

Run:

```bash
./mvnw
```

Then open your browser at:

```
http://localhost:8080
```

Click:

* **Play**
* **Start Simulation**

You should see real-time graph lines.

> If graphs appear, the BREATHE simulator is running correctly.
> Keep this terminal window open whenever you run BREATHE.

### 2.5 License Note

The web interface uses a **trial license**.
Renew the license, please visit the following link:
https://lightningchart.com/
In the top-right corner, click Download and select the Trial version.
You will be asked to provide some information. Once submitted, you will receive a .txt file by email.
This file should be placed in the frontend directory of the project (i.e., the web folder) and used to replace the corresponding lines in the relevant file.

---

## 3. Storage Node

### 3.1 Start Docker Services

From the storage node directory:

```bash
docker-compose up -d
```

### 3.2 Configure Keycloak Clients

Open:

```
http://localhost:8080
```

Login:

* **Username:** admin
* **Password:** admin

Create the following clients with settings below:
* `upload-client`
* `analytics-client`
* `visualization-client`
![Client Settings](images/client_setting.png)

Copy the **client secrets** into `docker-compose.yml` (lines 7–9).

### 3.3 Restart Containers

```bash
docker-compose down
docker-compose up -d
```

### 3.4 Configure Realm Frontend URLs

Navigate to:
**Configure → Realm Settings → General**

Set frontend configuration as shown:

![Realm Settings](images/realm_settings.png)

### 3.5 Update `.env`

Insert the generated Keycloak credentials into the `.env` file.

### 3.6 Verify FHIR Node

Ensure the **FHIR container** receives data and responds to queries.
BREATHE and ET Node must be active.

---

## 4. ET Node

### 4.1 Add Keycloak Credentials

Insert **UPLOAD CLIENT** credentials from Storage Node into the ET Node `.env`.

### 4.2 Start ET Node Containers

```bash
docker-compose up -d
```

Active containers should include:

* Apache Kafka (broker + controller)
* `breathe-to-kafka` wrapper
* `fhir-standardizer`

### 4.3 Verify Data Transmission

Ensure:

* BREATHE is running
* Simulation is started
* Kafka logs show data being published
* Standardizer logs show FHIR uploads

---

## 5. DT Node

### 5.1 Add Keycloak Credentials

Add **ANALYTICAL CLIENT** credentials to the DT Node `.env`.

### 5.2 Start Dependencies

Start the **Storage Node** and **ET Node**, then wait until they are ready.

### 5.3 Start DT Node

```bash
docker-compose up -d
```

Expected behavior:

* `resp-cap-assessment` outputs analytics in logs
* Kafka → ZeroMQ translator runs (optional)

---

## 6. Visualization Node

### 6.1 Add Keycloak Credentials

Add **VISUALIZATION CLIENT** credentials to the `.env`.

### 6.2 Start Dependencies

Start **Storage Node** and **ET Node** and wait until they are initialized.

### 6.3 Start Visualization Services

```bash
docker-compose up -d
```

### 6.4 Open Dashboards

* [http://localhost:8083](http://localhost:8083)
* [http://localhost:8082](http://localhost:8082)

All components must be running:

* BREATHE
* ET Node
* Storage Node
* DT Node

---

## Notes on TLS

TLS certificates were previously generated to secure communication among components.
If communication issues appear, verify certificate validity and regenerate if needed.