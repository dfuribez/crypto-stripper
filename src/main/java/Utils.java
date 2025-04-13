import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.persistence.PersistedObject;
import burp.api.montoya.persistence.Persistence;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Utils {

  public static ArrayList<String> burpListToArray(
      List object) {

    ArrayList<String> o = new ArrayList<String>();
    object.forEach((item) -> o.add(item.toString()));

    return o;
  }

  public static List<HashMap<String, String>> parametersToArray(
      List<ParsedHttpParameter> parameters
  ) {
    List<HashMap<String, String>> array = new ArrayList<>();

    for (ParsedHttpParameter param : parameters) {
      HashMap<String, String> p = new HashMap<String, String>();
      p.put("name", param.name());
      p.put("value", param.value());

      array.add(p);
    }

    return array;
  }

  public static HashMap<String, String> prepareRequestForExecutor(
      HttpRequest request,
      int messageId
  ) {
    HashMap<String, String> result = new HashMap<String, String>();

    String headers = new Gson().toJson(
        burpListToArray(request.headers()));

    String urlParameters = new Gson().toJson(
        parametersToArray(request.parameters(HttpParameterType.URL)));

    result.put("body", request.bodyToString());
    result.put("headers", headers);
    result.put("urlParameters", urlParameters);
    result.put("url", request.url());
    result.put("messageId", String.valueOf(messageId));

    return result;
  }

  public static HashMap<String, String> prepareResponseForExecutor(
      HttpResponse response,
      String url,
      int messageId
  ) {
    HashMap<String, String> result = new HashMap<String, String>();


    String headers = new Gson().toJson(
        burpListToArray(response.headers()));

    String urlParameters = new Gson().toJson(null);

    result.put("body", response.bodyToString());
    result.put("headers", headers);
    result.put("urlParameters", urlParameters);
    result.put("url", url);
    result.put("messageId", String.valueOf(messageId));

    return result;
  }

  public static boolean checkFileExists(String path) {

    if (path == null || path.isEmpty()) {
      return false;
    }

    File file = new File(path);

    return file.isFile();
  }

  public static List<HttpHeader> listToHttpHeaders(
      List<String> headersList
  ) {
    List<HttpHeader> headers = new ArrayList<HttpHeader>();

    if (headersList == null || headersList.isEmpty()) {
      return  headers;
    }

    for (String h : headersList) {
      headers.add(HttpHeader.httpHeader(h));
    }

    return headers;
  }

  public static List<HttpParameter> listToUrlParams(
      List<HashMap<String, String>> urlParametersList
  ) {
    List<HttpParameter> urlParameters = new ArrayList<HttpParameter>();

    if (urlParametersList == null || urlParametersList.isEmpty()) {
      return  urlParameters;
    }

    for (HashMap<String, String> param : urlParametersList) {
      urlParameters.add(
          HttpParameter.urlParameter(param.get("name"), param.get("value"))
      );
    }

    return urlParameters;
  }

  public static HttpRequest executorToHttp(
      HttpRequest request,
      ExecutorResponse output
  ) {

    HttpRequest modified = request
        .withRemovedHeaders(request.headers())
        .withRemovedParameters(request.parameters(HttpParameterType.URL))
        .withHeader(Constants.STRIPPER_HEADER, "true");


    for (HttpHeader h : listToHttpHeaders(output.getHeaders())) {
      modified = modified.withHeader(h);
    }

    if (output.getError() != null && !output.getError().isEmpty()) {
      modified = modified
          .withHeader(Constants.STRIPPER_HEADER, "error")
          .withBody(output.getError());
      return modified;
    }

    return modified
        .withBody(output.getBody())
        .withAddedParameters(listToUrlParams(output.getUrlParameters()));
  }

  public static HttpResponse executorToHttpResponse(
      HttpResponse response,
      ExecutorResponse output
  ) {

    HttpResponse modified = response
        .withRemovedHeaders(response.headers())
        .withAddedHeaders(listToHttpHeaders(output.getHeaders()));

    if (output.getError() != null && !output.getError().isEmpty()) {
      return modified
          .withAddedHeader(Constants.STRIPPER_HEADER, "error")
          .withBody(output.getError());
    }

    return modified
        .withBody(output.getBody())
        .withAddedHeader(Constants.STRIPPER_HEADER, "true");

  }

  public static String removeQueryFromUrl(String url) {
    return url.split("\\?")[0];
  }

  public static String getCommandFromPath(
      Persistence persistence,
      String path
  ) {

    if (path == null) {
      return null;
    }

    String globalPython = persistence.preferences().getString(
        Constants.GLOBAL_PYTHON_PATH_KEY);
    String globalNode = persistence.preferences().getString(
        Constants.GLOBAL_NODE_PATH_KEY);

    String nodePath = persistence.extensionData().getString(
        Constants.PROJECT_NODE_PATH_KEY);
    String pythonPath = persistence.extensionData().getString(
        Constants.PROJECT_PYTHON_PATH_KEY);

    if (!checkFileExists(pythonPath)) {
      pythonPath = globalPython;
    }

    if (!checkFileExists(nodePath)) {
      nodePath = globalNode;
    }

    int dotIndex = path.lastIndexOf(".");

    if (dotIndex > 0) {
      String extension = path.substring(dotIndex + 1);
      return switch (extension) {
        case "py" -> pythonPath;
        case "js" -> nodePath;
        default -> null;
      };
    }

    return null;
  }

  public static HashMap<String, PersistedList<String>> loadScope(PersistedObject extensionData) {

    HashMap<String, PersistedList<String>> output = new HashMap<>();

    PersistedList<String> stripperScope =
        extensionData.getStringList(
            Constants.STRIPPER_SCOPE_LIST_KEY);

    PersistedList<String> stripperBlackList =
        extensionData.getStringList(
            Constants.STRIPPER_BLACK_LIST_KEY);

    PersistedList<String> stripperForceIntercept =
        extensionData.getStringList(
            Constants.STRIPPER_FORCE_INTERCEPT_LIST_KEY);

    if (stripperScope == null) {
      stripperScope = PersistedList.persistedStringList();
    }

    if (stripperBlackList == null) {
      stripperBlackList = PersistedList.persistedStringList();
    }

    if (stripperForceIntercept == null) {
      stripperForceIntercept = PersistedList.persistedStringList();
    }

    output.put("scope", stripperScope);
    output.put("blacklist", stripperBlackList);
    output.put("force", stripperForceIntercept);

    return output;
  }

  public static PersistedList<String> arrayToPersisted(String[] list){
    PersistedList<String> out = PersistedList.persistedStringList();

    Collections.addAll(out, list);

    return out;
  }

}
