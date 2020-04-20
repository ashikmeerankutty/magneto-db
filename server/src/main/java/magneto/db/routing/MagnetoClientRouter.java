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
    String data = routeData(host, port, requestData);
    return data;
  }

  public String routeData(String host, int port, String data) throws IOException {
    InetSocketAddress magnetoAddress = new InetSocketAddress(host, port);
    SocketChannel magnetoClient = SocketChannel.open(magnetoAddress);
    System.out.println("Established connection to test via " + host + ":" + port);
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
}