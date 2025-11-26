package client;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import org.zeromq.ZContext;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ZeroMiddleware {
    private JFrame frame;
    private JButton connectButton;
    private JTextArea outputAreaServer, outputAreaClient;

    private ZMQ.Socket socketSubServer, socketSubClient;
    private ZContext context;
    private Thread communicationThreadServer, communicationThreadClient;

    public ZeroMiddleware() {
        frame = new JFrame("ZeroMQ Middleware");
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
                System.exit(0);
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1)); 
        frame.add(panel);
        placeComponents(panel);

        frame.setVisible(true);
    }

    private void placeComponents(JPanel panel) {
        JPanel buttonPanel = new JPanel();
        connectButton = new JButton("Connect");
        connectButton.setBackground(new Color(0, 122, 255));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);
        buttonPanel.add(connectButton);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        
        panel.add(buttonPanel);  

        JPanel outputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.add(outputPanel);

        outputAreaServer = new JTextArea();
        outputAreaServer.setLineWrap(true);
        outputAreaServer.setWrapStyleWord(true);
        outputAreaServer.setEditable(false);
        JScrollPane scrollPaneServer = new JScrollPane(outputAreaServer);
        outputPanel.add(scrollPaneServer);

        outputAreaClient = new JTextArea();
        outputAreaClient.setLineWrap(true);
        outputAreaClient.setWrapStyleWord(true);
        outputAreaClient.setEditable(false);
        JScrollPane scrollPaneClient = new JScrollPane(outputAreaClient);
        outputPanel.add(scrollPaneClient);

        panel.add(Box.createVerticalStrut(10)); 


        connectButton.addActionListener(e -> {
            connect();
            connectButton.setEnabled(false);
        });

        outputAreaServer.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                outputAreaServer.setCaretPosition(outputAreaServer.getDocument().getLength());
            }

            public void removeUpdate(DocumentEvent e) {
                outputAreaServer.setCaretPosition(outputAreaServer.getDocument().getLength());
            }

            public void changedUpdate(DocumentEvent e) {
                outputAreaServer.setCaretPosition(outputAreaServer.getDocument().getLength());
            }
        });
        
        outputAreaClient.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
            	outputAreaClient.setCaretPosition(outputAreaClient.getDocument().getLength());
            }

            public void removeUpdate(DocumentEvent e) {
            	outputAreaClient.setCaretPosition(outputAreaClient.getDocument().getLength());
            }

            public void changedUpdate(DocumentEvent e) {
            	outputAreaClient.setCaretPosition(outputAreaClient.getDocument().getLength());
            }
        });
    }

    private void connect() {
        synchronized (this) {
            context = new ZContext();
            socketSubServer = context.createSocket(SocketType.SUB);
            socketSubClient = context.createSocket(SocketType.SUB);
        }

        outputAreaServer.append("Connecting to server...\n");

        try {
            socketSubServer.connect("tcp://*:5555");
            socketSubClient.connect("tcp://*:5556");
        } catch (ZMQException ex) {
            outputAreaServer.append("Failed to connect to server: " + ex.getMessage() + "\n");
            return;
        }

        socketSubServer.subscribe("Server output".getBytes(ZMQ.CHARSET));
        socketSubServer.subscribe("Server input".getBytes(ZMQ.CHARSET));
        socketSubClient.subscribe("Client output".getBytes(ZMQ.CHARSET));
        socketSubClient.subscribe("Client input".getBytes(ZMQ.CHARSET));


        communicationThreadServer = new Thread(() -> {
            outputAreaServer.append("Server Thread started. Entering while loop...\n");

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String receivedDataServer = socketSubServer.recvStr();
                    receivedDataServer = receivedDataServer.trim().replace("Server input", "").trim();
                    receivedDataServer = receivedDataServer.trim().replace("Server output", "").trim();
                    outputAreaServer.append(receivedDataServer + "\n");
                }
            } catch (ZMQException ex) {
                if (ex.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                    outputAreaServer.append("ZMQ context terminated.\n");
                } else if (ex.getErrorCode() == ZMQ.Error.EINTR.getCode()) {
                    outputAreaServer.append("Socket interrupted during recv.\n");
                } else {
                    throw ex;
                }
            } finally {
                cleanup(socketSubServer);
                outputAreaServer.append("Resources cleaned up after interruption.\n");
            }
        });

        communicationThreadClient = new Thread(() -> {
            outputAreaClient.append("Client Thread started. Entering while loop...\n");

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String receivedDataClient = socketSubClient.recvStr();
                    receivedDataClient = receivedDataClient.trim().replace("Client output", "").trim();
                    outputAreaClient.append(receivedDataClient + "\n");
                }
            } catch (ZMQException ex) {
                if (ex.getErrorCode() == ZMQ.Error.ETERM.getCode()) {
                    outputAreaClient.append("ZMQ context terminated.\n");
                } else if (ex.getErrorCode() == ZMQ.Error.EINTR.getCode()) {
                    outputAreaClient.append("Socket interrupted during recv.\n");
                } else {
                    throw ex;
                }
            } finally {
                cleanup(socketSubClient);
                outputAreaClient.append("Resources cleaned up after interruption.\n");
            }
        });

        communicationThreadServer.start();
        communicationThreadClient.start();
    }

    private void cleanup(ZMQ.Socket socket) {
        synchronized (this) {
            if (socket != null) {
                socket.close();
            }
            if (context != null) {
                context.close();
                context = null;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ZeroMiddleware::new);
    }
}
