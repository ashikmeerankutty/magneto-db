package magneto.db.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author Ashik Meerankutty
 *
 */

public class MagnetoClientShell {

    public static final String CONN_EXIT = "exit";
    public static final String PROMPT = "> ";
    protected final PrintStream commandOutput;
    protected final PrintStream errorStream;
    protected SocketChannel magnetoClient = null;

    public MagnetoClientShell(String host, Integer port, PrintStream commandOutput, PrintStream errorStream) {
        this.commandOutput = commandOutput;
        this.errorStream = errorStream;

        try {
            InetSocketAddress magnetoAddress = new InetSocketAddress(host, port);
            this.magnetoClient = SocketChannel.open(magnetoAddress);
            commandOutput.println("Established connection to test via " + host + ":" + port);
            commandOutput.print(PROMPT);
        } catch (Exception e) {
            commandOutput.print("Could not connect to server: " + e);
        }
    }

    public static void showPrompt() {
        System.out.print(PROMPT);
        System.out.flush();
    }

    public static void main(String args[]) {
        String host = args[0];
        Integer port = Integer.parseInt(args[1]);
        MagnetoClientShell shell = new MagnetoClientShell(host, port, System.out, System.err);
        try {
            shell.process();
        }
        catch( Exception e) {
            System.out.println("Could not connect to server: " + e.getMessage());
        }
    }

    protected void process() throws IOException  {
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(System.in));

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            if (line.toLowerCase().startsWith("put")) {
                byte[] message = new String(line).getBytes();
                ByteBuffer buffer = ByteBuffer.wrap(message);
                this.magnetoClient.write(buffer);
                ByteBuffer putReadBuffer = ByteBuffer.allocate(256);
                this.magnetoClient.read(putReadBuffer);
                System.out.println(new String(putReadBuffer.array()).trim());
            } else if (line.toLowerCase().startsWith("get")) {
                byte[] message = new String(line).getBytes();
                ByteBuffer writeBuffer = ByteBuffer.wrap(message);
                this.magnetoClient.write(writeBuffer);
                ByteBuffer readBuffer = ByteBuffer.allocate(256);
                this.magnetoClient.read(readBuffer);
                // Remove -1 appended to end
                String result = new String(readBuffer.array()).trim().replaceAll("-1", "");
                System.out.println(result);
            } else if (line.toLowerCase().startsWith("delete")) {
                //TODO: Implement delete operation
                System.out.println("delete(key)");
            } else if (line.toLowerCase().startsWith("locate")) {
                //TODO: Implement locate operation
                System.out.println("locate(key)");
            } else if (line.toLowerCase().startsWith("help")) {
                System.out.println();
                System.out.println("Commands : ");
                System.out.println("put key value -- Associate the given value with the key.");
                System.out.println("get key -- Retrieve the value associated with the key.");
                System.out.println("delete key -- Remove all the values associated with the key.");
                System.out.println("locate key -- Determine which servers host the given key.");
                System.out.println("help -- Print this message.");
                System.out.println("exit -- Exit from this shell.");
                System.out.println();
            } else if (line.startsWith("quit") || line.startsWith("exit")) {
                byte[] message = new String(CONN_EXIT).getBytes();
                ByteBuffer buffer = ByteBuffer.wrap(message);
                this.magnetoClient.write(buffer);
                System.out.println("ok bye");
                System.exit(0);
            } else {
                System.out.println("Invalid command");
            }
            showPrompt();
        }

    }
}