package magneto.db.routing;

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

  private static void goRoute(ConsistentHashing<MagnetoRouter> consistentHashing, String requestKey) {
      System.out.println(requestKey + " is route to " + consistentHashing.routeNode(requestKey));
  }

  public void getNode(String key) {
    System.out.println(key);
    goRoute(this.consistentHashing, key);
  }
}