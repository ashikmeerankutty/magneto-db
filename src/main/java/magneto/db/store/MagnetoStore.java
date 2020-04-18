
package magneto.db.store;

import java.util.HashMap;

/**
 * @author Ashik Meerankutty
 *
 */

public class MagnetoStore {
  HashMap<String, String> magnetoStore;

  public MagnetoStore() {
    this.magnetoStore = new HashMap<String, String>();
  }

  public void addToStore(String key, String value) {
    System.out.println(key);
    System.out.println(value);
    magnetoStore.put(key, value);
  } 

  public boolean isExists(String key) {
    if(magnetoStore.containsKey(key)) return true;
    return false;
  }

  public String getFromStore(String key) {
    return magnetoStore.get(key);
  }
}