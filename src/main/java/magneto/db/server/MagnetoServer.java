package magneto.db.server;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Ashik Meerankutty
 *
 */

public class MagnetoServer {

    private static final Logger logger = LogManager.getLogger();

    public static void main(String args[]) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel magnetoSocket = ServerSocketChannel.open();
        InetSocketAddress magnetoAddress = new InetSocketAddress("localhost", 5678);

        magnetoSocket.bind(magnetoAddress);

        magnetoSocket.configureBlocking(false);

        int ops = magnetoSocket.validOps();
        SelectionKey selectKey = magnetoSocket.register(selector, ops, null);

        System.out.println("Server started waiting for client connection");

        MagnetoStore magnetoStore = new MagnetoStore();

        // Keeps server running
        while (true) {
            // Selects a set of keys whose corresponding channels are ready for I/O
            // operations
            selector.select();

            Set<SelectionKey> magnetoKeys = selector.selectedKeys();
            Iterator<SelectionKey> magnetoIterator = magnetoKeys.iterator();

            while (magnetoIterator.hasNext()) {
                SelectionKey selectedKey = magnetoIterator.next();

                // Tests whether this key's channel is ready to accept a new socket connection
                if (selectedKey.isAcceptable()) {
                    SocketChannel magnetoClient = magnetoSocket.accept();
                    magnetoClient.configureBlocking(false);
                    magnetoClient.register(selector, SelectionKey.OP_READ);
                    System.out.println("Connection accepted: " + magnetoClient.getLocalAddress());
                } else if (selectedKey.isReadable()) {
                    SocketChannel magnetoClient = (SocketChannel) selectedKey.channel();
                    ByteBuffer magnetoBuffer = ByteBuffer.allocate(256);
                    magnetoClient.read(magnetoBuffer);
                    String data = new String(magnetoBuffer.array()).trim();
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

}