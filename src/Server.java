import java.io.IOException;
import java.net.*;

public class Server {

    public Server(int port){

    }

    public synchronized void establishUDP(int port){
        try {
            DatagramSocket UDP_socket = new DatagramSocket(port);

            byte[] UDP_buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(UDP_buffer, UDP_buffer.length);
            while (true){

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
            if (message.startsWith("CCS DISCOVER")){
                String response = "CCS FOUND";
                byte[] responseData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(
                        responseData, responseData.length, packet.getAddress(), packet.getPort()
                );
                socket.send(responsePacket);

            }else{
                System.out.println("Unrecognized message");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void establishTCP(int port){
        try(ServerSocket socket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = socket.accept();
                new Thread(new ClientHandler(clientSocket, this)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
