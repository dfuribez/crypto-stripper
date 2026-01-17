import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.persistence.PersistedList;
import burp.api.montoya.persistence.PersistedObject;
import burp.api.montoya.persistence.Persistence;
import burp.api.montoya.scanner.audit.issues.AuditIssue;
import burp.api.montoya.scanner.audit.issues.AuditIssueConfidence;
import burp.api.montoya.scanner.audit.issues.AuditIssueSeverity;
import com.google.gson.Gson;
import models.ExecutorOutput;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;

import static burp.api.montoya.http.HttpService.httpService;

public class Utils {

  public static ArrayList<String> headersToArray(List<HttpHeader> headersList) {
    ArrayList<String> headers = new ArrayList<>();
    for (HttpHeader header : headersList) {
      if (!Arrays.asList(Constants.dangerousPseudoHeaders).contains(header.name())) {
        headers.add(header.toString());
      }
    }

    return headers;
  }

  public static List<HashMap<String, String>> parametersToArray(List<ParsedHttpParameter> parameters) {
    List<HashMap<String, String>> array = new ArrayList<>();

    for (ParsedHttpParameter param : parameters) {
      HashMap<String, String> p = new HashMap<>();
      p.put("name", param.name());
      p.put("value", param.value());

      array.add(p);
    }

    return array;
  }

  public static HashMap<String, String> prepareRequestForExecutor(
      HttpRequest request, int messageId, String source) {
    HashMap<String, String> result = new HashMap<>();

    String headers = new Gson().toJson(Utils.headersToArray(request.headers()));

    String urlParameters = new Gson().toJson(
        Utils.parametersToArray(request.parameters(HttpParameterType.URL)));

    result.put("body", new String(request.body().getBytes(), StandardCharsets.UTF_8));
    result.put("headers", headers);
    result.put("urlParameters", urlParameters);
    result.put("url", Utils.removeQueryFromUrl(request.url()));
    result.put("messageId", String.valueOf(messageId));
    result.put("httpMethod", request.method());
    result.put("path", request.path());
    result.put("toolSource", source);
    result.put("host", request.httpService().host());
    result.put("port", String.valueOf(request.httpService().port()));
    result.put("secure", String.valueOf(request.httpService().secure()));

    return result;
  }

  public static HashMap<String, String> prepareResponseForExecutor(
      HttpResponse response, String url, int messageId, String source) {
    HashMap<String, String> result = new HashMap<>();

    String headers = new Gson().toJson(Utils.headersToArray(response.headers()));

    String urlParameters = new Gson().toJson(null);

    result.put("body", new String(response.body().getBytes(), StandardCharsets.UTF_8));
    result.put("headers", headers);
    result.put("urlParameters", urlParameters);
    result.put("url", url);
    result.put("messageId", String.valueOf(messageId));
    result.put("statusCode", String.valueOf(response.statusCode()));
    result.put("reasonPhrase", response.reasonPhrase());
    result.put("toolSource", source);

    return result;
  }

  public static boolean checkFileExists(String path) {
    if (path == null || path.isEmpty()) {
      return false;
    }

    File file = new File(path);

    return file.isFile();
  }

  public static List<HttpHeader> listToHttpHeaders(List<String> headersList) {
    List<HttpHeader> headers = new ArrayList<>();

    if (headersList == null || headersList.isEmpty()) {
      return  headers;
    }

    headersList.forEach(h -> headers.add(HttpHeader.httpHeader(h)));
    return headers;
  }

  public static List<HttpParameter> listToUrlParams(
      List<HashMap<String, String>> urlParametersList) {
    List<HttpParameter> urlParameters = new ArrayList<>();

    if (urlParametersList == null || urlParametersList.isEmpty()) {
      return  urlParameters;
    }

    urlParametersList.forEach(p -> urlParameters.add(
        HttpParameter.urlParameter(p.get("name"), p.get("value")))
    );

    return urlParameters;
  }

  public static HttpRequest executorToHttpRequest(HttpRequest request, ExecutorOutput output) {
    if (output.error != null && !output.error.isEmpty()) {
      return request
          .withHeader(Constants.STRIPPER_HEADER, Constants.X_STRIPPER_ERROR);
    }

    HttpRequest modified = request
        .withService(httpService(output.host, output.port, output.secure))
        .withPath(output.path)
        .withRemovedParameters(request.parameters(HttpParameterType.URL))
        .withMethod(output.httpMethod);

    // avoids kettling
    for (HttpHeader header : request.headers()) {
      if (!Arrays.asList(Constants.dangerousPseudoHeaders).contains(header.name())) {
        modified = modified.withRemovedHeader(header.name());
      }
    }

    modified = modified.withAddedHeaders(Utils.listToHttpHeaders(output.headers));

    return modified
        .withBody(output.body)
        .withAddedParameters(Utils.listToUrlParams(output.urlParameters))
        .withHeader(Constants.STRIPPER_HEADER, "true");
  }

  public static HttpResponse executorToHttpResponse(HttpResponse response, ExecutorOutput output) {
    if (output.error != null && !output.error.isEmpty()) {
      return response
          .withAddedHeader(Constants.STRIPPER_HEADER, Constants.X_STRIPPER_ERROR);
    }

    HttpResponse modified = response
        .withRemovedHeaders(response.headers())
        .withAddedHeaders(Utils.listToHttpHeaders(output.headers))
        .withStatusCode(output.statusCode)
        .withReasonPhrase(output.reasonPhrase);

    return modified
        .withBody(output.body)
        .withAddedHeader(Constants.STRIPPER_HEADER, "true");
  }

  public static String removeQueryFromUrl(String url) {
    return url.split("\\?")[0];
  }

  public static String getCommandFromPath(Persistence persistence, String path) {
    if (path == null) {
      return null;
    }

    String globalPython = persistence.preferences().getString(Constants.GLOBAL_PYTHON_PATH_KEY);
    String globalNode = persistence.preferences().getString(Constants.GLOBAL_NODE_PATH_KEY);

    String nodePath = persistence.extensionData().getString(Constants.PROJECT_NODE_PATH_KEY);
    String pythonPath = persistence.extensionData().getString(Constants.PROJECT_PYTHON_PATH_KEY);

    if (!Utils.checkFileExists(pythonPath)) {
      pythonPath = globalPython;
    }

    if (!Utils.checkFileExists(nodePath)) {
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

    PersistedList<String> stripperScope = extensionData.getStringList(Constants.STRIPPER_SCOPE_LIST_KEY);

    PersistedList<String> stripperBlackList = extensionData.getStringList(Constants.STRIPPER_BLACK_LIST_KEY);

    PersistedList<String> stripperForceIntercept =
        extensionData.getStringList(Constants.STRIPPER_FORCE_INTERCEPT_LIST_KEY);

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

  public static boolean isUrlInScope(String url, PersistedList<String> scope) {
    for (String regex : scope) {
      try {
        if (url.matches(regex)) {
          return true;
        }
      } catch (Exception e) {
        return false;
      }
    }
    return false;
  }

  public static boolean isValidRegex(String regex) {
    try {
      "".matches(regex);
    } catch (Exception error) {
      return false;
    }
      return true;
  }

  public static boolean resourceToFile(MontoyaApi api, String resource, String path) {
    try (InputStream in = Utils.class.getClassLoader().getResourceAsStream(resource)) {
      if (in == null) {
        api.logging().logToError("Error reading template.js");
        return false;
      }

      Files.copy(in, Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (IOException e) {
      api.logging().logToError(e.toString());
      return false;
    }
  }

  public static boolean checkScriptVersion(short version) {
    return version >= 2;
  }

  public static void openFolder(String path) {
    if (Desktop.isDesktopSupported()) {
      File file = new File(path);
      Desktop desktop = Desktop.getDesktop();
      try {
        desktop.open(new File(file.getParent()));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static String removePathFromURL(String url) {
    try {
      URI uri = new URI(url);
      return new URI(
          uri.getScheme(),
          uri.getUserInfo(),
          uri.getHost(),
          uri.getPort(),
          null,
          null,
          null
      ).toString();
    } catch (URISyntaxException e) {
      return null;
    }
  }

  public static void setIssue(MontoyaApi montoyaApi, Map<String, String> issue, String url, HttpRequest request, HttpResponse response) {
    try {
      montoyaApi.logging().logToOutput(issue.get("name"));
      montoyaApi.siteMap().add(AuditIssue.auditIssue(
          issue.get("name"),
          issue.get("detail"),
          issue.get("remediation"),
          url,
          AuditIssueSeverity.INFORMATION,
          AuditIssueConfidence.CERTAIN,
          issue.get("background"),
          issue.get("remediationBackground"),
          AuditIssueSeverity.INFORMATION,
          HttpRequestResponse.httpRequestResponse(request, response)
      ));
    } catch (Exception e) {
      montoyaApi.logging().logToError("Error setting issue", e);
    }

  }
}
