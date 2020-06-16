package magneto.db.routing;

import magneto.db.node.Node;

public class MagnetoRouter implements Node{
  private final String idc;
  private final String ip;
  private final int port;

  public MagnetoRouter(String idc,String ip, int port) {
      this.idc = idc;
      this.ip = ip;
      this.port = port;
  }

  @Override
  public String getKey() {
      return idc + "-"+ip+":"+port;
  }

  @Override
  public String toString(){
      return getKey();
  }

  public String getIp() {
      return ip;
  }

  public int getPort() {
    return port;
}

  public static void main(String[] args) {
      //initialize 4 service node
      MagnetoRouter node1 = new MagnetoRouter("IDC1","localhost",5671);
      MagnetoRouter node2 = new MagnetoRouter("IDC1","localhost",5672);
      MagnetoRouter node3 = new MagnetoRouter("IDC1","localhost",5673);
      MagnetoRouter node4 = new MagnetoRouter("IDC1","localhost",5674);

      //hash them to hash ring
    //   ConsistentHashing<MagnetoRouter> consistentHashing = new ConsistentHashing<>(Arrays.asList(node1,node2,node3,node4),10);


      //we have 5 requester ip, we are trying them to route to one service node
    //   String requestIP1 = "hello";
    //   String requestIP2 = "world";
    //   String requestIP3 = "test";
    //   String requestIP4 = "heyhey";
    //   String requestIP5 = "lol";

    //   goRoute(consistentHashing,requestIP1,requestIP2,requestIP3,requestIP4,requestIP5);

    //   MagnetoRouter node5 = new MagnetoRouter("IDC2","127.0.0.1",8080);//put new service online
    //   System.out.println("-------------putting new node online " +node5.getKey()+"------------");
    //   consistentHashing.addNode(node5,10);

    //   goRoute(consistentHashing,requestIP1,requestIP2,requestIP3,requestIP4,requestIP5);

    //   consistentHashing.removeNode(node3);
    //   System.out.println("-------------remove node online " + node3.getKey() + "------------");
    //   goRoute(consistentHashing,requestIP1,requestIP2,requestIP3,requestIP4,requestIP5);


  }

//   private static void goRoute(ConsistentHashing<MagnetoRouter> consistentHashing ,String ... requestIps){
//       for (String requestIp: requestIps) {
//           System.out.println(requestIp + " is route to " + consistentHashing.routeNode(requestIp));
//       }
//   }
}