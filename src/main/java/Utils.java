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
}
