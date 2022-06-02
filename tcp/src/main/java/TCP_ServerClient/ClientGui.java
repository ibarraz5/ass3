package TCP_ServerClient;

import Utilities.JsonUtility;
import Utilities.NetworkUtility;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status.
 * <p>
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 * -> modal means that it opens the GUI and suspends background processes. Processing
 * still happens in the GUI. If it is desired to continue processing in the
 * background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * <p>
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 */
public class ClientGui implements OutputPanel.EventHandlers {
	// GUI
	private final JDialog frame;
	private final PicturePanel picturePanel;
	private final OutputPanel outputPanel;

	// Reading & writing streams to server
	private static OutputStream out;
	private static InputStream in;
	private static JSONObject jsonFromServer;
	private static JSONObject request;

	/**
	 * Constructor for the client user interface.
	 *
	 * @param host ip address
	 * @param port port value
	 */
	public ClientGui(String host, int port) {

		setSockets(host, port);
		setJsonObjects();

		frame = new JDialog();
		frame.setLayout(new GridBagLayout());
		frame.setMinimumSize(new Dimension(500, 500));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		// setup the top picture frame
		picturePanel = new PicturePanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0.25;
		frame.add(picturePanel, c);

		// setup the input, button, and output area
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0.75;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		outputPanel = new OutputPanel();
		outputPanel.addEventHandlers(this);
		frame.add(outputPanel, c);
	}


	/**
	 * Shows the current state in the GUI
	 *
	 * @param makeModal - true to make a modal window, false disables modal behavior
	 */
	public void show(boolean makeModal) {
		frame.pack();
		frame.setModal(makeModal);
		frame.setVisible(true);
	}


	/**
	 * Creates a new game and set the size of the grid
	 *
	 * @param dimension - the size of the grid will be dimension x dimension
	 */
	public void newGame(int dimension) {

		picturePanel.newGame(dimension);
		outputPanel.appendOutput("Connection has been established.");

		// Receive first message from server.
		try {
			byte[] bytesFromServer = NetworkUtility.Receive(in);
			jsonFromServer = JsonUtility.fromByteArray(bytesFromServer);
			if (jsonFromServer.has("sequence")) {
				int seq = jsonFromServer.getInt("sequence");
				if (seq == 1) { outputPanel.appendOutput(jsonFromServer.getString("data")); }
			}
			else if (jsonFromServer.has("error")) {
				outputPanel.appendOutput(jsonFromServer.getString("data"));
			}
		}
		catch (IOException e) { e.printStackTrace();}
	}

	/**
	 * Set the socket values
	 *
	 * @param host ip address
	 * @param port port value
	 */
	private static void setSockets(String host, int port) {

		try {
			Socket socket = new Socket(host, port);
			out = socket.getOutputStream();
			in = socket.getInputStream();
		}
		catch (IOException e) { e.printStackTrace();}
	}

	/**
	 * Initialize the JSON objects for read/write
	 */
	private static void setJsonObjects() {
		jsonFromServer = new JSONObject();
		request = new JSONObject();
	}


	/**
	 * Submit button handling
	 * <p>
	 * Change this to whatever you need
	 */
	@Override
	public void submitClicked() {
		String input = outputPanel.getInputText();

		// Check the sequence value and send message
		if (input.length() > 0 && jsonFromServer.has("sequence")) {
			switch (jsonFromServer.getInt("sequence")) {
				case (1) -> request = ClientRequest.reqName(input);
				case (2) -> request = ClientRequest.reqPicCount(input);
				case (3) -> request = ClientRequest.reqChoice(input);
				case (4) -> request = ClientRequest.reqReady(input);
				default -> System.out.println("Bad sequence");
			}
		}
		// Check for errors sent to the server and send message
		else if (jsonFromServer.has("error sequence")) {
			switch (jsonFromServer.getInt("error sequence")) {
				case (1) -> request = ClientRequest.reqName(input);
				case (2) -> request = ClientRequest.reqPicCount(input);
				case (3) -> request = ClientRequest.reqChoice(input);
				case (4) -> request = ClientRequest.reqReady(input);					
				default -> System.out.println("Bad sequence");
			}
		}

		// Validate request and send to server
		if (request != null) {
			try {
				NetworkUtility.Send(out, JsonUtility.toByteArray(request));
				outputPanel.appendOutput(input);
				outputPanel.setInputText("");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Receive message from server and display on gui
		try {
			byte[] responseBytes = NetworkUtility.Receive(in);
			jsonFromServer = JsonUtility.fromByteArray(responseBytes);

			// Error messages
			if (jsonFromServer.has("error")) {
				outputPanel.appendOutput(jsonFromServer.getString("error"));
				outputPanel.setInputText("");
			}
			// Images
			else if (jsonFromServer.has("datatype") && jsonFromServer.getString("datatype").equalsIgnoreCase("image")) {
				byte[] decodeImg = Base64.getDecoder().decode(jsonFromServer.getString("data"));
				picturePanel.insertImage(new ByteArrayInputStream(decodeImg), 0, 0);
				outputPanel.setInputText("");
			}
			else {
				jsonFromServer = JsonUtility.fromByteArray(responseBytes);
				outputPanel.appendOutput(jsonFromServer.getString("data"));
			}
		}
		catch (IOException | PicturePanel.InvalidCoordinateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Key listener for the input text box
	 * <p>
	 * Change the behavior to whatever you need
	 */
	@Override
	public void inputUpdated(String input) {
	}

	/**
	 * Inner Class: JSON request objects used to send data to server
	 */
	private static class ClientRequest {

		/**
		 * Request with client name
		 *
		 * @param name client's name
		 * @return JSON object with data
		 */
		public static JSONObject reqName(String name) {
			JSONObject request = new JSONObject();
			request.put("sequence", 1);
			request.put("datatype", "name");
			request.put("data", name);
			return request;
		}

		/**
		 * Request with picture count
		 *
		 * @param totalPictures value represents the clients choice for pictures to guess
		 * @return JSON object with data
		 */
		public static JSONObject reqPicCount(String totalPictures) {
			JSONObject request = new JSONObject();
			request.put("sequence", 2);
			request.put("datatype", "picture count");
			request.put("data", totalPictures);
			return request;
		}

		/**
		 * Request for final configuration.
		 *
		 * @param confirm "ready" input starts the game
		 * @return JSON object with data
		 */
		public static JSONObject reqChoice(String confirm) {
			JSONObject json = new JSONObject();
			json.put("sequence", 3);
			json.put("datatype", "choice");
			json.put("data", confirm);
			return json;
		}

		/**
		 * Request for client's guess
		 *
		 * @param guess client guess for current image
		 * @return JSON object with data
		 */
		public static JSONObject reqReady(String ready) {
			JSONObject json = new JSONObject();
			json.put("sequence", 4);
			json.put("datatype", "ready");
			json.put("data", ready);
			return json;
		}
	}

	/**
	 * Validates the input from CLI
	 *
	 * @param args arguments from the main
	 */
	public static void checkConnection(String[] args) {
		if (args.length <= 0 || args.length > 2) {
			System.out.println("Client Error: Arguments were not passed.");
			System.exit(1);
		}

		try { Integer.parseInt(args[0]); }
		catch (NumberFormatException e) {
			System.out.println("Client Error: Port value could not be convert to integer.");
			System.exit(1);
		}
	}

	/**
	 * Main method
	 * @param args arguments from command line
	 */
	public static void main(String[] args) {
		checkConnection(args);
		ClientGui game = new ClientGui(args[1], Integer.parseInt(args[0]));
		game.newGame(2);
		game.show(true);
	}
}
