import java.util.HashMap;
import java.util.List;

public class ExecutorOutput {
  private String body = "";
  private List<String> headers;
  private List<HashMap<String, String>> urlParameters;
  private String error = "";
  private String stdErr = "";
  private short statusCode = 0;
  private String reasonPhrase = "";
  private String path = "";
  private String httpMethod = "";
  private short version;
  private String host;
  private int port;
  private boolean secure;


  public boolean isSecure() {
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }


  public short getVersion() {
    return version;
  }

  public void setVersion(short version) {
    this.version = version;
  }

  public String getHttpMethod() {
    return httpMethod.toUpperCase();
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }

  public void setReasonPhrase(String reasonPhrase) {
    this.reasonPhrase = reasonPhrase;
  }

  public short getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(short statusCode) {
    this.statusCode = statusCode;
  }
  public String getStdErr() {
    return stdErr;
  }

  public void setStdErr(String stdErr) {
    this.stdErr = stdErr;
  }

  public List<HashMap<String, String>> getUrlParameters() {
    return urlParameters;
  }

  public void setUrlParameters(List<HashMap<String, String>> urlParameters) {
    this.urlParameters = urlParameters;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public List<String> getHeaders() {
    return headers;
  }

  public void setHeaders(List<String> headers) {
    this.headers = headers;
  }

}
