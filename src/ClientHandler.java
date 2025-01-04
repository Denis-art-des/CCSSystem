import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler implements Runnable {

    Socket socket;

    Server server;

    String[] operationList;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.operationList = new String[]{"ADD", "SUB", "MUL", "DIV"};
    }

    @Override
    public void run() {
        try (BufferedReader clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            OutputStream clientStream = socket.getOutputStream();
            PrintWriter recipientOut = new PrintWriter(clientStream, true);
            String message;
            while ((message = clientIn.readLine()) != null) {
                String[] messageParts = message.trim().split(" ");

                if (messageParts.length != 3) {
                    server.updatePeriodIncorrectOperations();
                    server.updateIncorrectOperations();
                    recipientOut.println("Error");
                    continue;
                }

                int result;
                try {
                    int arg1 = Integer.parseInt(messageParts[1]);
                    int arg2 = Integer.parseInt(messageParts[2]);
                    switch (messageParts[0]) {
                        case "ADD":
                            result = arg1 + arg2;
                            server.updateGlobalOperationCount("ADD");
                            server.updatePeriodOperationCount("ADD");
                            break;
                        case "SUB":
                            result = arg1 - arg2;
                            server.updateGlobalOperationCount("SUB");
                            server.updatePeriodOperationCount("SUB");
                            break;
                        case "MUL":
                            result = arg1 * arg2;
                            server.updateGlobalOperationCount("MUL");
                            server.updatePeriodOperationCount("MUL");
                            break;
                        case "DIV":
                            result = arg1 / arg2;
                            server.updateGlobalOperationCount("DIV");
                            server.updatePeriodOperationCount("DIV");
                            break;
                        default:
                            server.updatePeriodIncorrectOperations();
                            server.updateIncorrectOperations();
                            recipientOut.println("Error");
                            continue;
                    }
                    recipientOut.println(result);
                    server.updateValuesSum(result);
                    server.updatePeriodValuesSum(result);
                } catch (NumberFormatException | ArithmeticException e) {
                    server.updatePeriodIncorrectOperations();
                    server.updateIncorrectOperations();
                    recipientOut.println("Error");
                }

                server.updateComputedRequests();
                server.updatePeriodComputedRequests();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
