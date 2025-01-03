public class Main {

    public static void main(String[] args) {
        if (args.length == 1){
            try {
                int port = Integer.parseInt(args[0]);
                new Server(port);
            }catch (ClassCastException e){
                System.out.println("invalid argument type.");
            }
        }else {
            System.out.println("invalid arguments.");
            System.out.println("Correct call: CCS.jar <port>");
        }
    }
}
