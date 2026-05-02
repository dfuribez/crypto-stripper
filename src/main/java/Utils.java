import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Annotations;
import burp.api.montoya.core.ByteArray;
import burp.api.montoya.core.HighlightColor;
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
import net.miginfocom.swing.MigLayout;

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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import static burp.api.montoya.http.HttpService.httpService;

public class Utils {

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

    String headers = new Gson().toJson(KUtils.headersToArray(request.headers()));

    String urlParameters = new Gson().toJson(
        Utils.parametersToArray(request.parameters(HttpParameterType.URL)));

    result.put("body", new String(request.body().getBytes(), StandardCharsets.UTF_8));
    result.put("headers", headers);
    result.put("urlParameters", urlParameters);
    result.put("url", KUtils.Url.clean(request.url()));
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

    String headers = new Gson().toJson(KUtils.headersToArray(response.headers()));

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
          .withHeader(K.HEADER.STRIPPER, K.Error.ERROR);
    }

    HttpRequest modified = request
        .withService(httpService(output.host, output.port, output.secure))
        .withPath(output.path)
        .withRemovedParameters(request.parameters(HttpParameterType.URL))
        .withMethod(output.httpMethod);

    // avoids kettling
    for (HttpHeader header : request.headers()) {
      if (!Arrays.asList(K.Gen.dangerousPseudoHeaders).contains(header.name())) {
        modified = modified.withRemovedHeader(header.name());
      }
    }

    modified = modified.withAddedHeaders(Utils.listToHttpHeaders(output.headers));

    return modified
        .withBody(ByteArray.byteArray(output.body.getBytes(StandardCharsets.UTF_8)))
        .withAddedParameters(Utils.listToUrlParams(output.urlParameters))
        .withHeader(K.HEADER.STRIPPER, "true")
        .withUpdatedHeader("Host", output.host);
  }

  public static HttpResponse executorToHttpResponse(HttpResponse response, ExecutorOutput output) {
    if (output.error != null && !output.error.isEmpty()) {
      return response
          .withAddedHeader(K.HEADER.STRIPPER, K.Error.ERROR);
    }

    HttpResponse modified = response
        .withRemovedHeaders(response.headers())
        .withAddedHeaders(Utils.listToHttpHeaders(output.headers))
        .withStatusCode(output.statusCode)
        .withReasonPhrase(output.reasonPhrase);

    return modified
        .withBody(ByteArray.byteArray(output.body.getBytes(StandardCharsets.UTF_8)))
        .withAddedHeader(K.HEADER.STRIPPER, "true");
  }

  public static HashMap<String, PersistedList<String>> loadScope(PersistedObject extensionData) {
    HashMap<String, PersistedList<String>> output = new HashMap<>();

    PersistedList<String> stripperScope = extensionData.getStringList(K.KEYS.SCOPE_LIST);

    PersistedList<String> stripperBlackList = extensionData.getStringList(K.KEYS.BLACK_LIST);

    PersistedList<String> stripperForceIntercept =
        extensionData.getStringList(K.KEYS.FORCE_INTERCEPT_LIST);

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

  public static void setIssue(MontoyaApi montoyaApi, Map<String, String> issue,
                              String url, HttpRequest request, HttpResponse response
  ) {
    try {
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

  public static Annotations setAnnotation(
      String currentNote, String color, String note
  ) {

    Annotations annotation = Annotations.annotations();

    currentNote = currentNote == null || currentNote.isBlank() ? "" : currentNote;
    note = (note == null || note.isBlank()) ? "" : note;
    String separator = (currentNote.isBlank() || note.isBlank()) ? "" : ", ";

    if (!(color == null
        || color.isEmpty()
        || color.equalsIgnoreCase("NONE")
    )) {
      annotation
          .setHighlightColor(HighlightColor.highlightColor(color.toUpperCase())
      );
    }

    annotation.setNotes(currentNote + separator + note);
    return annotation;
  }

  public static int stringToInt(String value) {
    try {
      return Integer.parseInt(value);
    } catch (Exception e) {
      return  -1;
    }
  }

}
