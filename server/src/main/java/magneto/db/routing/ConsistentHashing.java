package magneto.db.routing;

import magneto.db.node.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Ashik Meerankutty
 *
 * To hash Node objects to a hash ring with a certain amount of virtual node.
 * Method routeNode will return a Node instance which the object key should be allocated to according to consistent hash algorithm
 *
 * @param <T>
 */

public class ConsistentHashing<T extends Node> {
  private final SortedMap<Long, VirtualNode<T>> ring = new TreeMap<>();
  private final HashFunction hashFunction;

  public ConsistentHashing(Collection<T> pNodes, int vNodeCount) {
      this(pNodes,vNodeCount, new MD5Hash());
  }

  /**
   *
   * @param pNodes collections of physical nodes
   * @param vNodeCount amounts of virtual nodes
   * @param hashFunction hash Function to hash Node instances
   */
  public ConsistentHashing(Collection<T> pNodes, int vNodeCount, HashFunction hashFunction) {
      if (hashFunction == null) {
          throw new NullPointerException("Hash Function is null");
      }
      this.hashFunction = hashFunction;
      if (pNodes != null) {
          for (T pNode : pNodes) {
              addNode(pNode, vNodeCount);
          }
      }
  }

  /**
   * add physical node to the hash ring with some virtual nodes
   * @param pNode physical node needs added to hash ring
   * @param vNodeCount the number of virtual node of the physical node. Value should be greater than or equals to 0
   */
  public void addNode(T pNode, int vNodeCount) {
      if (vNodeCount < 0) throw new IllegalArgumentException("illegal virtual node counts :" + vNodeCount);
      int existingReplicas = getExistingReplicas(pNode);
      for (int i = 0; i < vNodeCount; i++) {
          VirtualNode<T> vNode = new VirtualNode<>(pNode, i + existingReplicas);
          ring.put(hashFunction.hash(vNode.getKey()), vNode);
      }
  }

  /**
   * remove the physical node from the hash ring
   * @param pNode
   */
  public void removeNode(T pNode) {
      Iterator<Long> it = ring.keySet().iterator();
      while (it.hasNext()) {
          Long key = it.next();
          VirtualNode<T> virtualNode = ring.get(key);
          if (virtualNode.isVirtualNodeOf(pNode)) {
              it.remove();
          }
      }
  }

  /**
   * with a specified key, route the nearest Node instance in the current hash ring
   * @param objectKey the object key to find a nearest Node
   * @return
   */
  public T routeNode(String objectKey) {
      if (ring.isEmpty()) {
          return null;
      }
      Long hashVal = hashFunction.hash(objectKey);
      SortedMap<Long,VirtualNode<T>> tailMap = ring.tailMap(hashVal);
      Long nodeHashVal = !tailMap.isEmpty() ? tailMap.firstKey() : ring.firstKey();
      return ring.get(nodeHashVal).getPhysicalNode();
  }

  public T routeNextNode(String objectKey, int replicaIndex, HashSet<String> addresses) {
    if (ring.isEmpty()) {
        return null;
    }
    Long hashVal = hashFunction.hash(objectKey);
    SortedMap<Long,VirtualNode<T>> subRingMap = ring;
    SortedMap<Long,VirtualNode<T>> tailMap = ring.tailMap(hashVal);
    Long removeNodeHashVal = !tailMap.isEmpty() ? tailMap.firstKey() : subRingMap.firstKey();
    int i = 0;
    while(i <= replicaIndex-1){ 
        tailMap.remove(removeNodeHashVal);
        subRingMap.remove(removeNodeHashVal);
        removeNodeHashVal = !tailMap.isEmpty() ? tailMap.firstKey() : subRingMap.firstKey();
        MagnetoRouter physicalNode = (MagnetoRouter) ring.get(removeNodeHashVal).getPhysicalNode();
        String address = physicalNode.getPort() + physicalNode.getIp();
        if(!addresses.contains(address)){
            i++;
        }
    }
    return ring.get(removeNodeHashVal).getPhysicalNode();
}


  public int getExistingReplicas(T pNode) {
      int replicas = 0;
      for (VirtualNode<T> vNode : ring.values()) {
          if (vNode.isVirtualNodeOf(pNode)) {
              replicas++;
          }
      }
      return replicas;
  }

  
  //default hash function
  private static class MD5Hash implements HashFunction {
      MessageDigest instance;

      public MD5Hash() {
          try {
              instance = MessageDigest.getInstance("MD5");
          } catch (NoSuchAlgorithmException e) {
          }
      }

      @Override
      public long hash(String key) {
          instance.reset();
          instance.update(key.getBytes());
          byte[] digest = instance.digest();

          long h = 0;
          for (int i = 0; i < 4; i++) {
              h <<= 8;
              h |= ((int) digest[i]) & 0xFF;
          }
          return h;
      }
  }

}

