package magneto.db.routing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashSet;

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

  public String getNodesFromMaster(String key, String requestData) throws IOException {
    MagnetoRouter requestHost = this.consistentHashing.routeNode(key);
    int port = requestHost.getPort();
    String host = requestHost.getIp();
    SocketChannel magnetoClient = getClientSocket(host, port, requestHost, key);
    String address = requestHost.getPort() + requestHost.getIp();
    if (magnetoClient != null){
      String data = routeData(magnetoClient, requestData);
      System.out.println("Client request routed to : "+host+":"+port);
      String dataFromReplicas = replicateData(key, requestData, address);
      if(!data.equals("null")) {
        return data;
      }
      return dataFromReplicas;
    }
    return replicateData(key, requestData, address);
  }

  public String replicateData(String key, String requestData, String initialAddress) throws IOException {
    int i = 1;
    int replicas = 2;// Number of replicas
    HashSet<String> addresses = new HashSet<String>(); 
    SocketChannel magnetoClient = null;
    String data = "";
    addresses.add(initialAddress);
    while(i <= replicas) {
      MagnetoRouter requestHost = this.consistentHashing.routeNextNode(key, i, addresses);
      int port = requestHost.getPort();
      String host = requestHost.getIp();
      String address = requestHost.getPort() + requestHost.getIp();
      addresses.add(address);
      System.out.println("Request replicated at : "+host+":"+port);
      magnetoClient = getClientSocket(host, port, requestHost, key);
      if(magnetoClient == null) {
        continue;
      }
      data = routeData(magnetoClient, requestData);
      i++;
    }
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
    InetSocketAddress magnetoAddress = getSocketAddress(host, port);
    try {
      magnetoClient = SocketChannel.open(magnetoAddress);
    } 
    catch(IOException e) {
      System.out.println("Server can't be connected using replicas instead");
      return null;
    }
    return magnetoClient;  
  }
}