import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
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

  public static HashMap<String, String> prepareForExecutor(
      HttpRequest request
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
    HttpRequest modified = HttpRequest.httpRequest();

    modified = modified.withService(request.httpService());
    modified = modified.withPath(request.pathWithoutQuery());
    modified = modified.withAddedHeaders(
        listToHttpHeaders(output.getHeaders()));

    if (output.getError() != null && !output.getError().isEmpty()) {
      modified = modified.withHeader(Constants.STRIPPER_HEADER, "error");
      modified = modified.withBody(output.getError());
      return modified;
    }

    modified = modified.withBody(output.getBody());
    modified = modified.withAddedParameters(
        listToUrlParams(output.getUrlParameters()));
    return modified;
  }



}
