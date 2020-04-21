package magneto.db.requests;

import magneto.db.routing.MagnetoClientRouter;
import magneto.db.store.MagnetoStore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


public class MagnetoRequests implements Requests {

  String SUCCESS_WRITE = "success";

  MagnetoClientRouter magnetoClientRouter;

  public void Requests() {
    this.magnetoClientRouter = new MagnetoClientRouter();
  }

  @Override
  public String getData(String key, MagnetoStore magnetoStore) {
    return magnetoStore.getFromStore(key);
  }

  @Override
  public void putData(String key, String value, MagnetoStore magnetoStore) {
    magnetoStore.addToStore(key, value);
  }

  @Override
  public String locateData(String key) {
    return null;
  }

  public void handleRequest(String data, SocketChannel magnetoClient, MagnetoStore magnetoStore) throws IOException {
    String[] words = new String[3];
    words = data.split("\\s+");
    String response;
    if(words[0].equals("put")) {
      putData(words[1], words[2], magnetoStore);
    }
    if(words[0].equals("get")) {
      response = getData(words[1], magnetoStore);
      byte[] message = new String(response).getBytes();
      ByteBuffer responseBuffer = ByteBuffer.wrap(message);
      magnetoClient.write(responseBuffer);
    }
  }

}