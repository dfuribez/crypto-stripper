import java.util.HashMap;
import java.util.List;

public class ExecutorOutput {
  public String body = "";
  public List<String> headers;
  public List<HashMap<String, String>> urlParameters;

  public String host;
  public int port;
  public boolean secure;

  public String error = "";
  public String stdErr = "";

  public short statusCode = 0;
  public String reasonPhrase = "";
  public String path = "";
  public String httpMethod = "";

  public short version;

  public String eventLog;
}
