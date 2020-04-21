package magneto.db.requests;

import magneto.db.store.MagnetoStore;

/**
 * @author Ashik Meerankutty
 * Represent a node which should be mapped to a hash ring
 */
public interface Requests {

  /**
   * Handle get operation
   * @return the value of the key
   */
  public String getData(String key, MagnetoStore magnetoStore);

  /**
   * Handle put operation
   * @return success or failure
   */
  public void putData(String key, String value, MagnetoStore magnetoStore);

  /**
   * Handle locate operation
   * @return node with data
   */
  public String locateData(String key);
}