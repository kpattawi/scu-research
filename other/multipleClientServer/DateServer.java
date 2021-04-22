import java.net.ServerSocket;
import java.net.*;
import java.io.*;
import java.io.IOException;
import java.util.Date;

public class DateServer {
    private static final int PORT = 9090;

    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(PORT);

        System.out.println("[Server] Waiting for client connection...");
        Socket client = listener.accept();
        System.out.println("[Server] Connected to client!");
        
        PrintWriter out = new PrintWriter(client.getOutputStream(), autoFlush: true);
        out.println( (new Date()).toString());
        System.out.println("[Server] Sent date to client.");

        client.close();
        listener.close();
    }
}