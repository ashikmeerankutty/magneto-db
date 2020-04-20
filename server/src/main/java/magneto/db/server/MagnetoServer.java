package magneto.db.server;

import magneto.db.routing.MagnetoClientRouter;
import magneto.db.store.*;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.xml.crypto.Data;

/**
 * @author Ashik Meerankutty
 *
 */

public class MagnetoServer {

    public Selector selector;
    public ServerSocketChannel magnetoSocket;

    MagnetoServer(int port) throws IOException {
        this.selector = Selector.open();
        this.magnetoSocket = ServerSocketChannel.open();
        InetSocketAddress magnetoAddress = new InetSocketAddress("localhost", port);
        this.magnetoSocket.bind(magnetoAddress);
        this.magnetoSocket.configureBlocking(false);
        int ops = magnetoSocket.validOps();
        magnetoSocket.register(selector, ops, null);
        System.out.println("Server started waiting for client connection at "+"localhost"+":"+port);
    }

    public static void main(String args[]) throws IOException {
        String nodeType = args[0];
        int port = Integer.parseInt(args[1]);

        MagnetoServer magnetoServer = new MagnetoServer(port);
        if(nodeType.equals("node"))
        {
            magnetoServer.runNode();
        }
        if(nodeType.equals("master")) {
            magnetoServer.runMaster();
        }
    }

    public void runNode() throws IOException {
        MagnetoStore magnetoStore = new MagnetoStore();

        // Keeps server running
        while (true) {
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
                    System.out.println("Connection accepted: " + magnetoClient.getLocalAddress());
                } else if (selectedKey.isReadable()) {
                    SocketChannel magnetoClient = (SocketChannel) selectedKey.channel();
                    ByteBuffer magnetoBuffer = ByteBuffer.allocate(256);
                    magnetoClient.read(magnetoBuffer);
                    String data = new String(magnetoBuffer.array()).trim();
                    if(data.equals("-1")) {
                        magnetoClient.close();
                    } 
                    String[] words = new String[3];
                    if (data.length() > 0) {
                        words = data.split("\\s+");
                        if (words[0].equals("put"))
                            magnetoStore.addToStore(words[1], words[2]);
                        if (words[0].equals("get")) {
                            String response = magnetoStore.getFromStore(words[1]);
                            byte[] message = new String(response).getBytes();
                            ByteBuffer responseBuffer = ByteBuffer.wrap(message);
                            magnetoClient.write(responseBuffer);
                        }
                    }

                    System.out.println("Message received: " + data);
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
                    System.out.println("Connection accepted: " + magnetoClient.getLocalAddress());
                } else if (selectedKey.isReadable()) {
                    SocketChannel magnetoClient = (SocketChannel) selectedKey.channel();
                    ByteBuffer magnetoBuffer = ByteBuffer.allocate(256);
                    magnetoClient.read(magnetoBuffer);
                    String data = new String(magnetoBuffer.array()).trim();
                    if(data.equals("-1")) {
                        magnetoClient.close();
                    } 
                    String[] words = new String[3];
                    words = data.split("\\s+");
                    String key = words[1];
                    magnetoClientRouter.getNode(key);
                    System.out.println("Message received: " + data);
                }
            }
            magnetoIterator.remove();
        }
    }
}