import burp.api.montoya.MontoyaApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
    StringBuilder stdErr = new StringBuilder();

    ExecutorResponse response = new ExecutorResponse();

    request.put("action", action);

    if ("request".equals(source)) {
      scriptToExecute = api.persistence().extensionData().getString(
          Constants.REQUEST_SCRIPT_PATH_KEY);
    } else {
      scriptToExecute = api.persistence().extensionData().getString(
          Constants.RESPONSE_SCRIPT_PATH_KEY);
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
      File temp = File.createTempFile("stripper_", ".json");

      try (Writer writer = new FileWriter(temp, StandardCharsets.UTF_8)) {
        Gson gson = new GsonBuilder().create();
        gson.toJson(request, writer);
      }

      ProcessBuilder processBuilder = new ProcessBuilder(
          command,
          scriptToExecute,
          temp.getAbsolutePath()
      );

      processBuilder.redirectErrorStream(false);

      Process process = processBuilder.start();

      BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
      );

      BufferedReader errorReader = new BufferedReader(
          new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8)
      );

      String line;

      while ((line = reader.readLine()) != null) {
        output.append(line);
      }

      while ((line = errorReader.readLine()) != null) {
        stdErr.append(line + "\n");
      }

      decodedOutput = new String(api
          .utilities()
          .base64Utils()
          .decode(output.toString()).getBytes(),
          StandardCharsets.UTF_8
      );

      if (decodedOutput.isEmpty()) {
        response.setError(String.format(
            Constants.STRIPPER_ERROR_TEMPLATE,
            command,
            scriptToExecute,
            api.utilities().base64Utils().encodeToString(
                new Gson().toJson(request)),
            "Script's output is empty or null",
            ""
            ));
        response.setStdErr(stdErr.toString());
        return  response;
      }

      response = new  Gson()
          .fromJson(decodedOutput, ExecutorResponse.class);

      response.setStdErr(stdErr.toString());

      temp.delete();
      return response;
    } catch (IOException  | IllegalStateException | JsonSyntaxException |
        IllegalArgumentException e) {
      response.setError(String.format(
          Constants.STRIPPER_ERROR_TEMPLATE,
          command,
          scriptToExecute,
          api.utilities().base64Utils().encodeToString(
              new Gson().toJson(request)),
          e.toString(),
          decodedOutput
      ));
      response.setStdErr(stdErr.toString());
      return  response;
    }
  }
}
