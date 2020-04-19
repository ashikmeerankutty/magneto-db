package magneto.db.routing;

/**
 * @author Ashik Meerankutty
 *
 * Hash String to long value
 */
public interface HashFunction {
    long hash(String key);
}