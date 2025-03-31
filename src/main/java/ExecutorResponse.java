import java.util.List;

public class ExecutorResponse {
  private String body;
  private List<String> headers;
  private List<String> urlParameters;
  private String error;

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

  public List<String> getUrlParameters() {
    return urlParameters;
  }

  public void setUrlParameters(List<String> urlParameters) {
    this.urlParameters = urlParameters;
  }
}
