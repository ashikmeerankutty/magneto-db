package magneto.db.server;

import magneto.db.requests.MagnetoRequests;
import magneto.db.routing.MagnetoClientRouter;
import magneto.db.store.MagnetoStore;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Ashik Meerankutty
 *
 */

public class MagnetoServer {

    public static final String CONN_EXIT = "exit";
    public Selector selector;
    public ServerSocketChannel magnetoSocket;
    public MagnetoStore magnetoStore;
    public MagnetoRequests requests;

    MagnetoServer(int port) throws IOException {
        this.magnetoStore = new MagnetoStore();
        this.requests = new MagnetoRequests();
        this.selector = Selector.open();
        this.magnetoSocket = ServerSocketChannel.open();
        InetSocketAddress magnetoAddress = new InetSocketAddress("localhost", port);
        this.magnetoSocket.bind(magnetoAddress).configureBlocking(false);
        int ops = magnetoSocket.validOps();
        magnetoSocket.register(selector, ops, null);
        System.out.println("Server started waiting for client connection at " + "localhost" + ":" + port);
    }

    public static void main(String args[]) throws IOException {
        String nodeType = args[0];
        int port = Integer.parseInt(args[1]);

        MagnetoServer magnetoServer = new MagnetoServer(port);
        if (nodeType.equals("node")) {
            magnetoServer.runNode();
        }
        if (nodeType.equals("master")) {
            magnetoServer.runMaster();
        }
    }

    public void runNode() throws IOException {
        // Keeps server running
        while (true) {
            // Selects a set of keys whose corresponding channels are ready for I/O
            // operations
            this.selector.select();

            Set<SelectionKey> magnetoKeys = selector.selectedKeys();
            Iterator<SelectionKey> magnetoIterator = magnetoKeys.iterator();

            while (magnetoIterator.hasNext()) {
                SelectionKey selectedKey = magnetoIterator.next();

                // Tests whether this key's channel is ready to accept a new socket connection
                if (selectedKey.isAcceptable()) {
                    SocketChannel magnetoClient = this.magnetoSocket.accept();
                    magnetoClient.configureBlocking(false).register(selector, SelectionKey.OP_READ);
                    System.out.println("Connection accepted: " + magnetoClient.getLocalAddress());
                } else if (selectedKey.isReadable()) {
                    SocketChannel magnetoClient = (SocketChannel) selectedKey.channel();
                    ByteBuffer magnetoBuffer = ByteBuffer.allocate(256);
                    magnetoClient.read(magnetoBuffer);
                    String data = new String(magnetoBuffer.array()).trim();
                    if (data.equals(CONN_EXIT)) {
                        System.out.println("Connection disconnected: " + magnetoClient.getLocalAddress() );
                        magnetoClient.close();
                    }
                    requests.handleRequest(data, magnetoClient, magnetoStore);
                }
            }
            magnetoIterator.remove();
        }
    }

    public void runMaster() throws IOException {

        // Keeps server running
        while (true) {
            MagnetoClientRouter magnetoClientRouter = new MagnetoClientRouter();
            // Selects a set of keys whose corresponding channels are ready for I/O
            // operations
            try {
                this.selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Set<SelectionKey> magnetoKeys = selector.selectedKeys();
            Iterator<SelectionKey> magnetoIterator = magnetoKeys.iterator();

            while (magnetoIterator.hasNext()) {
                SelectionKey selectedKey = magnetoIterator.next();

                // Tests whether this key's channel is ready to accept a new socket connection
                if (selectedKey.isAcceptable()) {
                    SocketChannel magnetoClient = this.magnetoSocket.accept();
                    magnetoClient.configureBlocking(false);
                    magnetoClient.register(selector, SelectionKey.OP_READ);
                    System.out.println("Message Routed to : " + magnetoClient.getLocalAddress());
                } else if (selectedKey.isReadable()) {
                    SocketChannel magnetoClient = (SocketChannel) selectedKey.channel();
                    ByteBuffer magnetoBuffer = ByteBuffer.allocate(256);
                    magnetoClient.read(magnetoBuffer);
                    String data = new String(magnetoBuffer.array()).trim();
                    if (data.equals("-1")) {
                        magnetoClient.close();
                    }
                    String[] words = new String[3];
                    words = data.split("\\s+");
                    String key = words[1];
                    String response;
                    if (words[0].equals("put")) {
                        response = magnetoClientRouter.getNode(key, data);
                    }
                    if (words[0].equals("get")) {
                        response = magnetoClientRouter.getNode(key, data);
                        byte[] message = new String(response).getBytes();
                        ByteBuffer responseBuffer = ByteBuffer.wrap(message);
                        magnetoClient.write(responseBuffer);
                    }
                    System.out.println("Message received: " + data);
                }
            }
            magnetoIterator.remove();
        }
    }
}