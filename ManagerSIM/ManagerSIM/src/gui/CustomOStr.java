package gui;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

public class CustomOStr extends OutputStream{
	private JTextArea textArea;

	/**
	 * Constructor for CustomOStr.
	 * @param textArea the text area where we will print msg info.
	 */
	public CustomOStr(JTextArea textArea) {
		this.textArea = textArea;
	}

	@Override
	public void write(int b) throws IOException {
		// redirects data to the text area
		textArea.append(String.valueOf((char)b));
		// scrolls the text area to the end of data
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}
}
