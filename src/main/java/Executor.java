import burp.api.montoya.MontoyaApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public final class Executor {

  public static ExecutorOutput execute(
      MontoyaApi api, String action, String source, HashMap<String, String> request) {
    StringBuilder output = new StringBuilder();
    String decodedOutput = "";
    String scriptToExecute;
    String command;
    StringBuilder stdErr = new StringBuilder();
    StringBuilder error = new StringBuilder();

    int numberOfOutputLines = 0;
    ExecutorOutput response = new ExecutorOutput();

    request.put("action", action);

    if ("request".equals(source)) {
      scriptToExecute = api.persistence().extensionData().getString(
          Constants.REQUEST_SCRIPT_PATH_KEY);
    } else {
      scriptToExecute = api.persistence().extensionData().getString(
          Constants.RESPONSE_SCRIPT_PATH_KEY);
    }

    command = Utils.getCommandFromPath(api.persistence(), scriptToExecute);

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

    File temp = null;

    try {
      temp = File.createTempFile("stripper_", ".json");

      try (Writer writer = new FileWriter(temp, StandardCharsets.UTF_8)) {
        Gson gson = new GsonBuilder().create();
        gson.toJson(request, writer);
      }
      File workingPath = new File(scriptToExecute);
      ProcessBuilder processBuilder =
          new ProcessBuilder(command, scriptToExecute, temp.getAbsolutePath());

      processBuilder.redirectErrorStream(false);
      processBuilder.directory(workingPath.getParentFile());

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
        numberOfOutputLines += 1;
      }

      while ((line = errorReader.readLine()) != null) {
        stdErr.append(line + "\n");
      }

      decodedOutput =
          new String(
              api.utilities().base64Utils().decode(output.toString()).getBytes(),
              StandardCharsets.UTF_8
          );

      if (!temp.delete()) {
        api.logging().logToError("Could not delete: " + temp.getAbsolutePath());
      }

      if (decodedOutput.isEmpty()) {
        response.setError("Script's output was empty or null");
        response.setStdErr(stdErr.toString());
        return  response;
      }

      response = new Gson().fromJson(decodedOutput, ExecutorOutput.class);
      response.setStdErr(stdErr.toString());

      if (response.getEventLog() != null && !response.getEventLog().isEmpty()) {
        api.logging().raiseInfoEvent(response.getEventLog());
      }

      return response;
    } catch (IOException  | IllegalStateException | JsonSyntaxException |
        IllegalArgumentException e) {
      response.setError(String.format(
          Constants.STRIPPER_ERROR_TEMPLATE,
          command, scriptToExecute, "", e.toString(), decodedOutput));

      error.append("--------- Extension errors ---------" + "\n");
      error.append("[+] " + e.toString() + "\n");

      if (numberOfOutputLines > 1) {
        error.append("\n[+] more than one line detected in stdout,"
            + "please make sure you are not using console.log/print"
            + "debug only by printing to stderr");
      }

      if (temp != null) {
        if (!temp.delete()) {
          api.logging().logToError("Could not delete: " + temp.getAbsolutePath());
        }
      }

      response.setStdErr(stdErr.toString());
      response.setError(error.toString());
      return  response;
    }
  }
}
