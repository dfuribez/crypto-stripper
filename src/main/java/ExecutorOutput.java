import java.util.HashMap;
import java.util.List;

public class ExecutorOutput {
  private String body = "";
  private List<String> headers;
  private List<HashMap<String, String>> urlParameters;
  private String error = "";
  private Boolean replaceResponse = false;
  private String stdErr = "";
  private short statusCode = 0;
  private String reasonPhrase = "";

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

  public Boolean getReplaceResponse() {
    return replaceResponse;
  }

  public void setReplaceResponse(Boolean replaceResponse) {
    this.replaceResponse = replaceResponse;
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
