package magneto.db.routing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class MagnetoClientRouter {
  ConsistentHashing<MagnetoRouter> consistentHashing;

  public MagnetoClientRouter() {
    MagnetoRouter node1 = new MagnetoRouter("IDC1", "localhost", 5671);
    MagnetoRouter node2 = new MagnetoRouter("IDC1", "localhost", 5672);
    MagnetoRouter node3 = new MagnetoRouter("IDC1", "localhost", 5673);
    MagnetoRouter node4 = new MagnetoRouter("IDC1", "localhost", 5674);

    // Uses 10 virtual nodes
    this.consistentHashing = new ConsistentHashing<>(Arrays.asList(node1, node2, node3, node4), 10);
  }

  // private static String goRoute(ConsistentHashing<MagnetoRouter>
  // consistentHashing, String requestKey) {
  // return requestKey + " is route to " +
  // consistentHashing.routeNode(requestKey);
  // }

  public String getNode(String key, String requestData) throws IOException {
    MagnetoRouter requestHost = this.consistentHashing.routeNode(key);
    int port = requestHost.getPort();
    String host = requestHost.getIp();
    SocketChannel magnetoClient = getClientSocket(host, port, requestHost, key);
    String data = routeData(magnetoClient, requestData);
    return data;
  }

  public String routeData(SocketChannel magnetoClient, String data) throws IOException {
    // get data from available node
    
    // SocketChannel magnetoClient = SocketChannel.open(magnetoAddress);
    // SocketChannel magnetoClient = routeData(host, port, requestHost);

    byte[] message = new String(data).getBytes();
    ByteBuffer buffer = ByteBuffer.wrap(message);
    String[] words = new String[3];
    words = data.split("\\s+");
    magnetoClient.write(buffer);
    if (words[0].equals("get")) {
      ByteBuffer readBuffer = ByteBuffer.allocate(256);
      magnetoClient.read(readBuffer);
      String response = new String(readBuffer.array()).trim();
      byte[] finalMessage = new String("-1").getBytes();
      ByteBuffer finalBuffer = ByteBuffer.wrap(finalMessage);
      magnetoClient.write(finalBuffer);
      return response;
    }
    byte[] finalMessage = new String("-1").getBytes();
    ByteBuffer finalBuffer = ByteBuffer.wrap(finalMessage);
    magnetoClient.write(finalBuffer);
    return "success";
  }

  public InetSocketAddress getSocketAddress(String host, int port) {
    return new InetSocketAddress(host, port);
  }

  public SocketChannel getClientSocket(String host, int port, MagnetoRouter requestHost, String key) {
    SocketChannel magnetoClient;
    while(true) {
      InetSocketAddress magnetoAddress = getSocketAddress(host, port);
      try {
        magnetoClient = SocketChannel.open(magnetoAddress);
      } 
      catch(IOException e) {
        System.out.println("Server can't be connected routing to next server");
        this.consistentHashing.removeNode(requestHost);
        requestHost = this.consistentHashing.routeNode(key);
        port = requestHost.getPort();
        host = requestHost.getIp();
        continue;
      }
      System.out.println("Server can't be connected routing to next server");
      break;
    }
    return magnetoClient;  
  }
}