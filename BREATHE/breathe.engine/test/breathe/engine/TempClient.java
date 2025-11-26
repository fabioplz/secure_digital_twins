package breathe.engine;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class TempClient {
    private ZMQ.Socket socketPub, socketSub;
    private ZContext context;
    private boolean isConnected = false;
    private Thread communicationThread;

    private Map<String, Double> receivedDataMap;
    private String simTime;
    private String unit;

    private final int respiratoryRate = 12; // Respiratory Rate (RR)
    private final double ieRatio = 0.67;    // I:E Ratio
    private final double pinsp = 20.0;      // Inspiratory Pressure
    private final double vt = 500.0;        // Tidal Volume
    private double peep = 5.0;        // PEEP
    private String ventilatorType = "Pressure"; // Fixed ventilator type

    public TempClient() {
        receivedDataMap = new HashMap<>();
        connectToServer();
    }

    private void connectToServer() {
        context = new ZContext();

        socketPub = context.createSocket(SocketType.PUB);
        socketSub = context.createSocket(SocketType.SUB);

        System.out.println("Connecting to server...");

        try {
            socketPub.bind("tcp://*:5556");
            socketSub.connect("tcp://*:5555");
        } catch (Exception ex) {
            System.out.println("Failed to connect to server: " + ex.getMessage());
            return;
        }

        socketSub.subscribe("Server".getBytes(ZMQ.CHARSET));
        isConnected = true;

        communicationThread = new Thread(() -> {
            try {
                int firstConnection = 0;
                while (isConnected && !Thread.currentThread().isInterrupted()) {
                    if (firstConnection < 3) {
                        socketPub.send("Client {\"message\":\"requestData\"}".getBytes(ZMQ.CHARSET), 0);
                        firstConnection++;
                    }
                    String receivedData = socketSub.recvStr();
                    receivedData = receivedData.trim().replace("Server", "").trim();
                    
                    storeData(receivedData);

                    double value = ventilatorType.equals("Volume") ? processVolume() : processPressure();

                    String request = "Client {\"message\":\"input\", \"ventilatorType\":\"" + ventilatorType
                            + "\", \"value\":\"" + value + "\", \"unit\":\"" + unit + "\" }";
                    socketPub.send(request.getBytes(ZMQ.CHARSET), 0);
                }
            } catch (Exception ex) {
                System.out.println("Error during communication: " + ex.getMessage());
            } finally {
                cleanup();
            }
        });

        communicationThread.start();
    }

    private void storeData(String data) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(data);

            JsonNode patientDataNode = rootNode.path("Patient Data");
            if (patientDataNode.isObject()) {
                if (patientDataNode.has("SimTime")) {
                    JsonNode simTimeNode = patientDataNode.get("SimTime");
                    simTime = simTimeNode.get("value").asText();
                }

                patientDataNode.fieldNames().forEachRemaining(key -> {
                    if (!key.equals("SimTime")) {
                        try {
                            JsonNode valueNode = patientDataNode.get(key);
                            double value = valueNode.get("value").asDouble();
                            receivedDataMap.put(key, value);
                        } catch (Exception e) {
                            System.out.println("Error parsing value for " + key);
                        }
                    }
                });
            }

        } catch (Exception e) {
            System.out.println("Error parsing received data: " + e.getMessage());
        }
    }

    private double processVolume() {
        unit = "mL";
        return vt; // Return the fixed tidal volume
    }

    private double processPressure() {
        unit = "mmHg";

        if (simTime == null) {
            return peep;
        }

        double currentSimTime = Double.parseDouble(simTime);

        double totalCycleDuration = 60.0 / respiratoryRate;
        double inspiratoryTime = totalCycleDuration * (ieRatio / (1 + ieRatio));

        double cycleTime = currentSimTime % totalCycleDuration;

        return (cycleTime < inspiratoryTime) ? pinsp : peep;
    }

    private void cleanup() {
        if (socketPub != null) {
            socketPub.close();
            socketPub = null;
        }
        if (socketSub != null) {
            socketSub.close();
            socketSub = null;
        }
        if (context != null) {
            context.close();
            context = null;
        }
        isConnected = false;
    }

    public void disconnectFromServer() {
        isConnected = false;

        try {
            if (socketPub != null) {
                socketPub.send("Client {\"message\":\"disconnect\"}".getBytes(ZMQ.CHARSET), 0);
            }

            if (communicationThread != null) {
                communicationThread.join();
            }

            cleanup();
        } catch (Exception ex) {
            System.out.println("Error during disconnection: " + ex.getMessage());
        }
    }
    
    public void changetoVolume() {
    	ventilatorType = "Volume";
    }
    
    public void setInvalidPeep() {
    	peep = 50000000;
    }
}
