import burp.api.montoya.MontoyaApi;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public final class Executor {

  public static ExecutorResponse execute(
      MontoyaApi api,
      String action,
      String scriptToExecute,
      String body,
      String headers,
      String urlParameters
  ) {
    StringBuilder output = new StringBuilder();
    String decodedOutput = "";

    HashMap<String, String> argumentMap = new HashMap<String, String>();
    ExecutorResponse response = new ExecutorResponse();

    argumentMap.put("body", body);
    argumentMap.put("headers", headers);
    argumentMap.put("urlparams", urlParameters);

    String argumentJSON = new Gson()
        .toJson(argumentMap);

    try {
      ProcessBuilder processBuilder = new ProcessBuilder(
          "node",
          scriptToExecute,
          action,
          api.utilities().base64Utils().encodeToString(argumentJSON)
      );

      processBuilder.redirectErrorStream(false);

      Process process = processBuilder.start();

      BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream())
      );

      String line;

      while ((line = reader.readLine()) != null) {
        output.append(line);
      }

      decodedOutput = api
          .utilities()
          .base64Utils()
          .decode(output.toString())
          .toString();

      if (decodedOutput.isEmpty()) {
        response.setError(scriptToExecute  + "Script's output is null");
        return  response;
      }

      return new Gson()
          .fromJson(decodedOutput, ExecutorResponse.class);
    } catch (IOException  | IllegalStateException e) {
      response.setError(e.toString());
      return  response;
    }
  }
}
