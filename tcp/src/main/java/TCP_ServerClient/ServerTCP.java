package TCP_ServerClient;

import Utilities.JsonUtility;
import Utilities.NetworkUtility;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


@SuppressWarnings({"TryFinallyCanBeTryWithResources"})
public class ServerTCP {

	private static class PictureGuessGame {
		private final String playerName;
		private int pictureCount;
		private int totalMilliseconds;
		private int correctGuess;
		private Timer timer;
		private boolean hasRemainingTime;
		private final int maxPictureCount;
		private ArrayList<String> picturePath;
		private ArrayList<String> answerKeys;
		public static final String PICTURE_PATH = "src/main/resources/images/";

		/**
		 * Constructor for the picture guess game
		 *
		 * @param playerName   client's name
		 * @param pictureCount client's total pictures wanting to guess
		 */
		public PictureGuessGame(String playerName, int pictureCount) {

			this.playerName = playerName;
			this.maxPictureCount = calculateMaxPictureCount();

			// Verify picture count range
			if (pictureCount > 0 && pictureCount <= this.maxPictureCount) {
				this.pictureCount = pictureCount;
				initializeGame();
			}
		}

		/**
		 * Initializes game
		 */
		private void initializeGame() {
			this.totalMilliseconds = calculateTime(pictureCount);
			this.hasRemainingTime = true;
			this.picturePath = generateRandPics();
			this.answerKeys = generateAnswerKey();
		}

		/**
		 * Calculate the max picture count based on the directories in resources
		 *
		 * @return integer value representing total folders
		 */
		private int calculateMaxPictureCount() {
			int numberOfFolders = 0;
			File dir;
			File[] listDir;

			// Find all directories and increment
			try {
				dir = new File(PICTURE_PATH);
				listDir = dir.listFiles();
				if (dir.listFiles() != null && listDir != null) {
					for (File file : listDir) {
						if (file != null) {
							if (file.isDirectory()) numberOfFolders++;
						}
					}
				}
			}
			catch (NullPointerException e) {
				System.out.println("Invalid file path");
			}

			return numberOfFolders;
		}

		/**
		 * Randomizes the pictures based on player picture count and total picture directories
		 *
		 * @return Arraylist of pictures selected
		 */
		private ArrayList<String> generateRandPics() {

			// Generate list of folders
			File file = new File(PICTURE_PATH);
			File[] files = file.listFiles();
			ArrayList<String> folders = new ArrayList<>(maxPictureCount);

			// Find all folders
			if (files != null) {
				for (File value : files) {
					if (value.isDirectory()) {
						folders.add(value.toString());
					}
				}
			}

			// Shuffle folders
			Collections.shuffle(folders);

			// Remove folders to meet the client input
			for (int i = folders.size(); i > pictureCount; i--) {
				folders.remove(i - 1);
			}

			return folders;
		}

		/**
		 * Calculate the total time client has to guess all pictures
		 *
		 * @param pictureCount input from the client
		 * @return integer value representing total time in milliseconds
		 */
		private int calculateTime(int pictureCount) {
			int MILLISECONDS_PER_PICTURE = 30;
			return pictureCount * MILLISECONDS_PER_PICTURE;
		}

		/**
		 * Starts the timer for the game
		 */
		public void startTimer() {
			this.timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				int i = totalMilliseconds;

				public void run() {
					i--;
					if (i < 0) {
						timer.cancel();
						hasRemainingTime = false;
					}
				}
			}, 0, 1000);
		}

		/**
		 * Checks if time is still available in current game
		 *
		 * @return true=time still available | false=time elapsed
		 */
		public boolean hasRemainingTime() {
			return hasRemainingTime;
		}

		/**
		 * Checks the client's current guess against the current picture
		 *
		 * @param guess client's guess
		 * @param key   current picture answer
		 * @return true=correct guess | false=incorrect guess
		 */
		public boolean guess(String guess, String key) {
			if (guess.equals(key)) {
				correctGuess++;
				return true;
			}

			return false;
		}


		/**
		 * Generate the answer key
		 *
		 * @return answer key based on the random pictures selected
		 */
		private ArrayList<String> generateAnswerKey() {

			String PATH = "src\\main\\resources\\images\\";
			ArrayList<String> randomPictures = getPicturePath();
			ArrayList<String> returnKeys = new ArrayList<>(randomPictures.size());

			for (String randomPicture : randomPictures) {
				String filename = randomPicture.replace(PATH, "");
				returnKeys.add(filename);
			}

			return returnKeys;
		}

		/**
		 * Checks if the client has won the game
		 *
		 * @return true=client won | false=client did not win
		 */
		public boolean win() {
			if (correctGuess == pictureCount) {
				timer.cancel();
				return true;
			}

			return false;
		}

		/**
		 * Gets the picture paths for randomized pictures
		 *
		 * @return Arraylist of picture paths
		 */
		public ArrayList<String> getPicturePath() {
			return picturePath;
		}

		/**
		 * Gets the answer keys
		 *
		 * @return Arraylist of answers
		 */
		public ArrayList<String> getAnswerKeys() {
			return answerKeys;
		}

		/**
		 * Getter for client's name
		 *
		 * @return clients name
		 */
		public String getPlayerName() {
			return playerName;
		}
	}

	/**
	 * Inner Class: JSON response objects used to send data to server
	 */
	private static class ServerResponse {

		/**
		 * Response for client name
		 *
		 * @return JSON object with data
		 */
		public static JSONObject nameRequest() {
			JSONObject json = new JSONObject();
			json.put("sequence", 1);
			json.put("datatype", "name");
			json.put("data", "Server-> Welcome to Movie Quotes! Please submit your name.");
			json.put("image", "");
			return json;
		}

		/**
		 * Response for picture count
		 *
		 * @param clientName used in the message
		 * @return JSON object with data
		 */
		public static JSONObject quantityRequest(String clientName) {
			JSONObject json = new JSONObject();
			json.put("sequence", 2);
			json.put("datatype", "picture count");
			json.put("data", "Server-> Hello " + clientName + ", how many movie characters to guess?");
			return json;
		}

		/**
		 * Response for game initialization
		 *
		 * @return JSON object with data
		 */
		public static JSONObject choiceRequest() {
			JSONObject json = new JSONObject();
			json.put("sequence", 3);
			json.put("datatype", "config");		
			json.put("data", "Server-> Type [leader board] or [ready] to play.");
			return json;
		}		
		public static JSONObject readyRequest() {
			JSONObject json = new JSONObject();
			json.put("sequence", 5);
			json.put("datatype", "config");
			json.put("data", "Server-> Type [ready] when you're ready to play.");
			return json;
		}
		
		static String leaderBoard= "John 5 points";	
		static String readyPlay= " , Type [ready] to play.";				
		
		public static JSONObject leaderboardDisplay() {
			JSONObject json = new JSONObject();
			json.put("sequence", 4);
			json.put("datatype", "config");		
			json.put("data", "Server-> Leader Board: " + leaderBoard + readyPlay);
			return json;
		}			
		/**
		 * Response with image
		 *
		 * @param filepath images location
		 * @return JSON object with data
		 */
		public static JSONObject image(String filepath) throws IOException {
			JSONObject json = new JSONObject();
			json.put("sequence", 6);
			json.put("datatype", "image");

			File file = new File(filepath);
			if (!file.exists()) {
				System.err.println("Server-> Cannot find file: " + file.getAbsolutePath());
				System.exit(1);
			}
			// Read in image
			BufferedImage img = ImageIO.read(file);
			byte[] bytes;
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				ImageIO.write(img, "png", out);
				bytes = out.toByteArray();
				Base64.Encoder encoder = Base64.getEncoder();
				json.put("data", encoder.encodeToString(bytes));
				return json;
			}
			catch (Exception e) {
				return error("Server-> Unable to save image to byte array", 6);
			}
		}

		/**
		 * Response for errors
		 *
		 * @param err      Error message
		 * @param errorSeq Error sequence
		 * @return JSON object with data
		 */
		public static JSONObject error(String err, int errorSeq) {
			JSONObject json = new JSONObject();
			json.put("error sequence", errorSeq);
			json.put("error", "Server->" + err);
			return json;
		}
	}

	/**
	 * Validates the input from CLI
	 *
	 * @param args arguments from the main
	 */
	public static boolean checkArgs(String[] args) {

		if (args.length <= 0) {
			System.out.println("Server Error: Arguments were not passed.");
			return false;
		}
		else {

			try {
				int port = Integer.parseInt(args[0]);
				if (port < 1) return false;
			}
			catch (NumberFormatException e) {
				System.out.println("Server Error: Port value could not be convert to integer.");
			}
		}

		return true;
	}

	/**
	 * Main method
	 *
	 * @param args Arguments from CLI
	 * @throws IOException throws io exceptions
	 */
	public static void main(String[] args) throws IOException {

		// Check arguments
		if (!checkArgs(args)) {
			System.exit(1);
		}

		// Set port
		int port = Integer.parseInt(args[0]);
		ServerSocket serverSocket = null;

		// Initialize variables
		String name = "";
		int count = 0;
		boolean init = false;


		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Server waiting for client to connect...");
			Socket socket = null;

			try {
				socket = serverSocket.accept();
				System.out.println("Client " + socket.getLocalAddress() + " is connected.");
				OutputStream out = socket.getOutputStream();
				InputStream in = socket.getInputStream();

				JSONObject jsonToClient = null;
				JSONObject jsonFromClient;

				// Initial welcome and name request
				NetworkUtility.Send(out, JsonUtility.toByteArray(ServerResponse.nameRequest()));

				// Loop until initial request/responses have been made
				while (!init) {

					// Receive client
					byte[] bytesFromClient = NetworkUtility.Receive(in);
					jsonFromClient = JsonUtility.fromByteArray(bytesFromClient);

					// Server response
					if (jsonFromClient.has("sequence")) {
						int sequence = jsonFromClient.getInt("sequence");
						switch (sequence) {
							// Client name
							case (1) -> {
								name = jsonFromClient.getString("data");
								jsonToClient = ServerResponse.quantityRequest(name);
							}
							// Number of pictures
							case (2) -> {
								try {
									count = Integer.parseInt(jsonFromClient.getString("data"));
									if (count < 1 || count > 6) {
										count = 0;
										System.out.println("Acceptable range is 1 - 6.");
										jsonToClient = ServerResponse.error("Acceptable range is 1 - 6.", 2);
									}
									else {
										jsonToClient = ServerResponse.choiceRequest();
									}
								}
								catch (NumberFormatException e) { jsonToClient = ServerResponse.error("Invalid Request", 2);}
							}
							
							// Choice   
							case (3) -> {
								String choice = jsonFromClient.getString("data");
								if(choice.equals("leader board")){
										jsonToClient = ServerResponse.leaderboardDisplay();
								}else if(choice.equals("ready")){
									init = true;
								}
								else{
									jsonToClient = ServerResponse.error("Type [ready] when you're ready to play.", 3);									
								}
							}							
							case (4) -> {
								String ready = jsonFromClient.getString("data");
							        if(ready.equals("ready")){
									init = true;
								}
								else{
									jsonToClient = ServerResponse.error("Type [ready] when you're ready to play.", 4);									
								}
							}								
							default -> {
								if (jsonToClient != null) {
									int seq = jsonToClient.getInt("sequence");
									jsonToClient = ServerResponse.error("Resend your last command.", seq);
								}
							}
						}
					}

					if (!init && jsonToClient != null) {
						NetworkUtility.Send(out, JsonUtility.toByteArray(jsonToClient));
					}
				}

				// Initialize variables for game
				PictureGuessGame game = new PictureGuessGame(name, count);
				ArrayList<String> picturePath = game.getPicturePath();
				ArrayList<String> pictureName = game.getAnswerKeys();

				// Start game timer
				game.startTimer();

				// Loop until timer has elapsed for player wins
				while (game.hasRemainingTime()) {

					// Start with first element in picture path
					for (int i = 0; i < picturePath.size(); i++) {
						String key = pictureName.get(i);

						for (int j = 1; j < 5; j++) {

							String fullFilePath = picturePath.get(i) + "\\" + (pictureName.get(i) + j) + ".png";
							jsonToClient = ServerResponse.image(fullFilePath);
							NetworkUtility.Send(out, JsonUtility.toByteArray(jsonToClient));

							byte[] bytesFromClient = NetworkUtility.Receive(in);
							jsonFromClient = JsonUtility.fromByteArray(bytesFromClient);

							if (jsonFromClient.getInt("sequence") == 5 && game.guess(jsonFromClient.getString("data"), key)) {
								break;
							}
						}

						if (game.win()) {
							String fullFilePath = "src/main/resources/images/win.jpg";
							jsonToClient = ServerResponse.image(fullFilePath);
							NetworkUtility.Send(out, JsonUtility.toByteArray(jsonToClient));
							break;
						}
					}
				}
				if (!game.win()) {
					String fullFilePath = "src/main/resources/images/lose.jpg";
					jsonToClient = ServerResponse.image(fullFilePath);
					NetworkUtility.Send(out, JsonUtility.toByteArray(jsonToClient));
				}
			}
			catch (Exception e) {
				System.out.println("Client disconnect");
			}
			finally {
				if (socket != null) {
					socket.close();
				}
			}
		}
		catch (IOException e) {
			System.out.println("Server Socket Error: Server Exiting.");
		}
		finally {
			if (serverSocket != null) serverSocket.close();
		}
	}
}

