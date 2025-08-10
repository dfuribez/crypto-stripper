import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ByteArray;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.event.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

public class InsertDialog extends JDialog {
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JRadioButton randomContentRadioButton;
  private JTextField textRepeat;
  private JPanel generationPanel;
  private JTextField textLenght;
  public JRadioButton base64RadioButton;
  public JRadioButton URLEncodeRadioButton;
  private JRadioButton plainTextRadioButton;
  private JRadioButton base64RadioButton1;
  private JButton insertFileButton;
  private JPanel filePanel;
  private JButton insertRandomButton;

  static byte[] selectedText;
  MontoyaApi montoyaApi;

  JFileChooser fileChooser = new JFileChooser();
  private static final String URL_SAFE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
  private static final Random RANDOM = new Random();


  public InsertDialog(MontoyaApi montoyaApi) {
    super(montoyaApi.userInterface().swingUtils().suiteFrame(), "Insert payload", false);
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    generationPanel.setBorder(BorderFactory.createCompoundBorder(
        new TitledBorder(BorderFactory.createEtchedBorder(), "Strings:"),
        emptyBorder
    ));

    filePanel.setBorder(BorderFactory.createCompoundBorder(
        new TitledBorder(BorderFactory.createEtchedBorder(), "File:"),
        emptyBorder
    ));

    buttonOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onOK();
      }
    });

    buttonCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    });

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });


    contentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    setLocationRelativeTo(getParent());
    insertFileButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        int response = fileChooser.showOpenDialog(montoyaApi.userInterface().swingUtils().suiteFrame());

        if (response == JFileChooser.APPROVE_OPTION) {
          String path = fileChooser.getSelectedFile().getAbsolutePath();

          try {
            selectedText = Files.readAllBytes(Paths.get(path));
          } catch (Exception e) {
            montoyaApi.logging().logToError("Could not open file: " + path);
            montoyaApi.logging().logToError(e.toString());

          }
        }
        dispose();
      }
    });
    insertRandomButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        int length;

        try {
          length = Integer.parseInt(textLenght.getText());
        } catch (NumberFormatException e) {
          length = 0;
        }

        if (length > 0) {
          selectedText = generate(length).getBytes(StandardCharsets.UTF_8);
        }

        dispose();
      }
    });
  }

  private void onOK() {
    int length;
    String toRepeat = textRepeat.getText();

    try {
      length = Integer.parseInt(textLenght.getText());
    } catch (NumberFormatException e) {
      length = 0;
    }

    if (length > 0) {
      StringBuilder builder = new StringBuilder(length);

      while (builder.length() + toRepeat.length() <= length) {
        builder.append(toRepeat);
      }

      int remaining = length - builder.length();
      if (remaining > 0) {
        builder.append(toRepeat, 0, remaining);
      }

      selectedText = builder.toString().getBytes(StandardCharsets.UTF_8);
    }
    dispose();
  }


  private static String generate(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int index = RANDOM.nextInt(URL_SAFE_CHARS.length());
      sb.append(URL_SAFE_CHARS.charAt(index));
    }
    return sb.toString();
  }

  private void onCancel() {
    dispose();
  }
}
