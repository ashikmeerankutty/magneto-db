
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
    magnetoStore.put(key, value);
  } 

  public boolean isExists(String key) {
    if(magnetoStore.containsKey(key)) return true;
    return false;
  }

  public String getFromStore(String key) {
    if(isExists(key)) return magnetoStore.get(key);
    return "null";
  }
}