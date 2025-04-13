import java.util.ArrayList;

public class JsonSettings {
  private boolean enableRequest;
  private boolean enableResponse;
  private boolean enableForceIntercept;

  private String[] scope;
  private String[] blackList;
  private String[] forceIntercept;

  public boolean isEnableRequest() {
    return enableRequest;
  }

  public void setEnableRequest(boolean enableRequest) {
    this.enableRequest = enableRequest;
  }

  public boolean isEnableResponse() {
    return enableResponse;
  }

  public void setEnableResponse(boolean enableResponse) {
    this.enableResponse = enableResponse;
  }

  public boolean isEnableForceIntercept() {
    return enableForceIntercept;
  }

  public void setEnableForceIntercept(boolean enableForceIntercept) {
    this.enableForceIntercept = enableForceIntercept;
  }

  public String[] getScope() {
    return scope;
  }

  public void setScope(String[] scope) {
    this.scope = scope;
  }

  public String[] getBlackList() {
    return blackList;
  }

  public void setBlackList(String[] blackList) {
    this.blackList = blackList;
  }

  public String[] getForceIntercept() {
    return forceIntercept;
  }

  public void setForceIntercept(String[] forceIntercept) {
    this.forceIntercept = forceIntercept;
  }

}
