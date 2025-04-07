import burp.api.montoya.MontoyaApi;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public final class Executor {

  public static ExecutorResponse execute(
      MontoyaApi api,
      String action,
      String source,
      HashMap<String, String> request
  ) {
    StringBuilder output = new StringBuilder();
    String decodedOutput = "";
    String scriptToExecute;
    String command;

    ExecutorResponse response = new ExecutorResponse();

    request.put("action", action);
    String argumentJSON = new Gson().toJson(request);

    if ("request".equals(source)) {
      scriptToExecute = api.persistence().extensionData().getString(Constants.REQUEST_SCRIPT_PATH);
    } else {
      scriptToExecute = api.persistence().extensionData().getString(Constants.RESPONSE_SCRIPT_PATH);
    }

    command = Utils.getCommandFromPath(
        api.persistence(),
        scriptToExecute);

    if (!Utils.checkFileExists(scriptToExecute)) {
      response.setError(scriptToExecute + " is not a file");
      return response;
    }

    if (command == null) {
      response.setError("The selected script: "
          + scriptToExecute
          + " does not have a valid extension");
      return response;
    }

    try {
      ProcessBuilder processBuilder = new ProcessBuilder(
          command,
          scriptToExecute,
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
        response.setError(String.format(
            Constants.STRIPPER_ERROR_TEMPLATE,
            command,
            scriptToExecute,
            api.utilities().base64Utils().encodeToString(argumentJSON),
            "Script's output is empty or null",
            ""
            ));
        return  response;
      }

      return new Gson()
          .fromJson(decodedOutput, ExecutorResponse.class);
    } catch (IOException  | IllegalStateException | JsonSyntaxException |
        IllegalArgumentException e) {
      response.setError(String.format(
          Constants.STRIPPER_ERROR_TEMPLATE,
          command,
          scriptToExecute,
          api.utilities().base64Utils().encodeToString(argumentJSON),
          e.toString(),
          decodedOutput
      ));
      return  response;
    }
  }
}
