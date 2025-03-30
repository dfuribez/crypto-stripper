import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.params.ParsedHttpParameter;

import java.util.ArrayList;
import java.util.List;

public class Utils {

  public static ArrayList<String> burpListToArray(
      List object) {


    ArrayList<String> o = new ArrayList<String>();

    object.forEach((item) -> o.add(item.toString()));

    return o;
  }
}
