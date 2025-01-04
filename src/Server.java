import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {



    private  int valuesSum = 0;

    private  int periodValuesSum = 0;

    private  int allConnectedClients = 0;
    private  int newConnectedClients = 0;

    private  int computedRequests = 0;

    private  int periodComputedRequests = 0;

    private Map<String, Integer> globalOperationCount = new ConcurrentHashMap<>();

    private Map<String, Integer> periodOperationCount = new ConcurrentHashMap<>();


    public int incorrectOperations = 0;

    public int periodIncorrectOperations = 0;

    public Server(int port) {
        this.globalOperationCount.put("ADD" , 0);
        this.globalOperationCount.put("SUB" , 0);
        this.globalOperationCount.put("MUL" , 0);
        this.globalOperationCount.put("DIV" , 0);

        this.periodOperationCount.put("ADD" , 0);
        this.periodOperationCount.put("SUB" , 0);
        this.periodOperationCount.put("MUL" , 0);
        this.periodOperationCount.put("DIV" , 0);
        int total = 0;
        System.out.println("Start statistics");
        System.out.println("Newly connected clients :" + this.allConnectedClients);
        System.out.println("Number of computed requests :" + this.computedRequests);
        System.out.println("Number of incorrect operations :" + this.incorrectOperations);
        System.out.println("Sum of computed values :" + this.valuesSum);
        System.out.println("Numbers of particular requested operations:");
        for (Map.Entry<String, Integer> entry : globalOperationCount.entrySet()){
            System.out.println(entry.getKey() + ":" + entry.getValue());
            total += entry.getValue();
        }
        System.out.println("Total requested operations:" + total);


        new Thread(() -> establishUDP(port));
        new Thread(() -> establishTCP(port));
        new Thread(() -> writeStatistics());
    }

    public synchronized void establishUDP(int port) {
        try {
            DatagramSocket UDP_socket = new DatagramSocket(port);

            byte[] UDP_buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(UDP_buffer, UDP_buffer.length);
            while (true) {

                UDP_socket.receive(packet);

                byte[] packetData = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, packetData, 0, packet.getLength());
                DatagramPacket safePacket = new DatagramPacket(
                        packetData, packetData.length, packet.getAddress(), packet.getPort()
                );

                new Thread(() -> handleMessage(UDP_socket, safePacket)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized static void handleMessage(DatagramSocket socket, DatagramPacket packet) {
        try {

            String message = new String(packet.getData(), 0, packet.getLength());
            if (message.startsWith("CCS DISCOVER")) {
                String response = "CCS FOUND";
                byte[] responseData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(
                        responseData, responseData.length, packet.getAddress(), packet.getPort()
                );
                socket.send(responsePacket);

            } else {
                System.out.println("Unrecognized message");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void establishTCP(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = socket.accept();
                new Thread(new ClientHandler(clientSocket, this)).start();
                updateNewConnectedClients();
                updateAllConnectedClients();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeStatistics() {
        while (true){
            System.out.println("Start statistics");
            System.out.println("Newly connected clients :" + this.allConnectedClients);
            System.out.println("Number of computed requests :" + this.computedRequests);
            System.out.println("Number of incorrect operations :" + this.incorrectOperations);
            System.out.println("Sum of computed values :" + this.valuesSum);
            System.out.println("Numbers of particular requested operations:");
            for (Map.Entry<String, Integer> entry : globalOperationCount.entrySet()){
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }

            System.out.println();
            System.out.println();
            System.out.println();

            System.out.println("Last 10 sec statistics");
            System.out.println("Newly connected clients :" + this.newConnectedClients);
            System.out.println("Number of computed requests :" + this.periodComputedRequests);
            System.out.println("Number of incorrect operations :" + this.periodIncorrectOperations);
            System.out.println("Sum of computed values :" + this.periodValuesSum);
            System.out.println("Numbers of particular requested operations:");
            for (Map.Entry<String, Integer> entry : globalOperationCount.entrySet()){
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
            resetPeriodStats();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void updateValuesSum(int value) {
        this.valuesSum+= value;
    }

    public synchronized void updatePeriodValuesSum(int value) {
        this.periodValuesSum+= value;
    }

    public void updateAllConnectedClients() {
        this.allConnectedClients++;
    }

    public void updateNewConnectedClients() {
        this.newConnectedClients++;
    }

    public synchronized void updateComputedRequests() {
        this.computedRequests++;
    }

    public synchronized void updatePeriodComputedRequests() {
        this.periodComputedRequests++;
    }

    public synchronized void updateIncorrectOperations() {
        this.incorrectOperations++;
    }

    public synchronized void updatePeriodIncorrectOperations() {
        this.periodIncorrectOperations++;
    }

    public synchronized void updateGlobalOperationCount(String operation){
        int val = this.globalOperationCount.get(operation);
        this.globalOperationCount.replace(operation, val, val+1);
    }

    public synchronized void updatePeriodOperationCount(String operation){
        int val = this.periodOperationCount.get(operation);
        this.periodOperationCount.replace(operation, val, val+1);
    }

    public void resetPeriodStats(){
        this.periodValuesSum = 0;
        this.periodIncorrectOperations = 0;
        this.periodComputedRequests = 0;
        this.newConnectedClients = 0;
        this.periodOperationCount.forEach((key, value) -> {this.periodOperationCount.put(key, 0);});
    }

}
