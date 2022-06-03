package TCP_ServerClient;

import java.net.*;
import java.awt.image.BufferedImage;
import java.io.*;
import org.json.*;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.ImageIO;
import org.json.JSONException;
import java.util.Random;
/**
 * TCP Server class
 */
public class Server {
	
	public static Boolean clientOn;
	
	public static void main (String args[]) {
		
		int port = 8080; // default port
		
		if (args.length != 1 && args.length != 0) {
			System.out.println("Use 'gradle runServerTCP -Pport=[]'");
			System.out.println("Or 'gradle runServerTCP' for port=8080");
			System.exit(0);
		}
		try {
			if (args.length == 1) // change port num if it was provided
				port = Integer.parseInt(args[0]);
		} catch (NumberFormatException nfe) {
			System.out.println("[Pport] must be an integer!");
			nfe.printStackTrace();
			System.exit(1);
		}
		
		serverStart(port);
	}
	
	public static void serverStart(int p) {
		try {
			// New server socket
			ServerSocket serverSocket = new ServerSocket(p);
			System.out.println("READY AT PORT: " + p);
			int numMatches = 0;
			
			while(true) {
				try {
					System.out.println("WAITING FOR CONNECTIONS...");
					Socket clientSocket = serverSocket.accept();
					
					// create an input stream to receive data
					InputStream fromClient = clientSocket.getInputStream();
					// create an output stream to send data
					OutputStream toClient = clientSocket.getOutputStream();
					
					String cName = "unkown"; // client name string
					int numQuestions = 0; // client numQuestions int
					
					System.out.println("SERVER CONNECTED TO CLIENT!");
					
					JSONsend(toClient, JSONtext("Connection established."));
					JSONsend(toClient, JSONtext("Welcome to 'Who's That Pokémon!'"));
					JSONsend(toClient, JSONimage("pokemon-default.jpg")); // send image 'Who's That Pokémon!'
					
					JSONObject clientName = questionManage(toClient, fromClient, JSONquestion("What is your name?"), "NONE");
					cName = clientName.getString("data");
					
					JSONsend(toClient, JSONtext(cName + "! How many Pokémon would you like to find?"));
					JSONObject clientQ = questionManage(toClient, fromClient, JSONquestion("Enter a number."), "NUMERIC");
					numQuestions = Integer.parseInt((clientQ.getString("data")));
					
					JSONsend(toClient, JSONtext(cName + ", you will have to find " + numQuestions + " Pokémon!"));
					
					// new thread for this client's match
					numMatches++;
					new Thread(new Match(clientSocket, fromClient, toClient, cName, numQuestions, numMatches)).start();
				} catch (IOException IOex) {
					System.out.println("IOException: CONNECTION FAILED. RESTARTING SERVER.");
				} catch (JSONException Jex) {
					System.out.println("JSONException: Bad JSON. RESTARTING SERVER.");
				} catch (Exception e) {
					System.out.println("SERVER EXCEPTION. RESTARTING SERVER.");
				}
			}
		} catch (IOException IOex) {
			System.out.println("IOException: CONNECTION FAILED. RESTARTING SERVER.");
			// System.exit(1);
		} catch (JSONException Jex) {
			System.out.println("JSONException: Bad JSON. RESTARTING SERVER.");
			// System.exit(1);
		} catch (Exception e) {
			System.out.println("SERVER EXCEPTION. RESTARTING SERVER.");
			// System.exit(1);
		}
	}
	
	/**
	 * Match: nested class that Server uses to deal with a 'Who's that Pokemon?' match
	 */
	static class Match implements Runnable {
		/**
		 * Pokemon: nested class to be used in a match.
		 *
		 */
		private static class Pokemon {
			/**
			 * Pokemon properties
			 */
			private String name;
			private String image;
			private int number;
			public enum names {
				MEW, BULBASAUR, BLASTOISE, PIKACHU, NINETALES, DIGLETT, MEOWTH, PSYDUCK, ARCANINE,
				ABRA, PONYTA, GASTLY, GENGAR, RHYDON, SCYTHER, GYARADOS, EEVEE, SNORLAX, MEWTWO
			}
			
			public Pokemon(String name, int num) {
				this.name = name;
				this.number = num;
				this.image = "pokemon-" + number + ".jpg";
			}
			
			public static Pokemon[] allPokes() {
				Pokemon[] pokeArray = new Pokemon[19];
				for (names p: names.values()) {
					pokeArray[p.ordinal()] = new Pokemon(p.toString(), p.ordinal());
					// System.out.println("This pokemon is " + pokeArray[p.ordinal()].name + pokeArray[p.ordinal()].number);
				}
				return pokeArray;
			}
			
			public String getName() {
				return this.name;
			}
			
			public String getImage() {
				return this.image;
			}
		}
		/*
		 * properties of the Match class
		 */
		private Socket client;
		private InputStream fromClient;
		private OutputStream toClient;
		private String clientName;
		private int numQuestions;
		private int matchID;
		private int time;
		Pokemon[] allPokemon;
		Pokemon[] questionPokemon;
		private int totalPokemon = 19;
		
		/**
		 * Match's constructor creates a new match with a socket
		 */
		public Match(Socket c, InputStream in, OutputStream out, String n, int q, int i) {
			this.client = c;
			this.fromClient = in;
			this.toClient = out;
			this.clientName = n;
			this.numQuestions = q;
			this.matchID = i;
			this.time = q * 20;
		}
		
		/**
		 * run: deals with sending and receiving data from client
		 */
		@Override
		public void run() {
			try {
				
				JSONsend(toClient, JSONtext("You will have " + time + " seconds to answer!"));
				JSONsend(toClient, JSONtext("Randomizing pokemon..."));
				
				allPokemon = Pokemon.allPokes();
				questionPokemon = new Pokemon[numQuestions]; 
				
				Random randomizer = new Random();
				for (int i = 0; i < numQuestions; i++) {
					questionPokemon[i] = allPokemon[randomizer.nextInt(totalPokemon)];
				}
				
				JSONsend(toClient, JSONtext(clientName + ", type 'START' to begin match!"));
				questionManage(toClient, fromClient, JSONquestion("Would you like to start?"), "START");
				JSONsend(toClient, JSONtext("Find " + numQuestions + " Pokemon!"));
				
				Calendar cal = Calendar.getInstance();
		        Date startTime = cal.getTime();
		        cal.add(Calendar.SECOND, time);
		        Date finishTime = cal.getTime();
		        Calendar cal2;
				Date currentTime;
				int correctAnswers = 0;
				
				for (int i = 0; i < numQuestions; i++) {
					
					JSONsend(toClient, JSONimage(questionPokemon[i].getImage()));
					
					System.out.println(questionPokemon[i].getName());
					questionManage(toClient, fromClient, JSONquestion("Who's That Pokémon!?"), questionPokemon[i].getName());
					
					correctAnswers++; JSONsend(toClient, JSONtext("Number of correct answers: " + correctAnswers));
					cal2 =  Calendar.getInstance();
					currentTime = cal2.getTime();
					JSONsend(toClient, JSONtext("Current Time: " + currentTime));
					JSONsend(toClient, JSONtext("Time to Finish: " + finishTime));
					if (currentTime.after(finishTime)) {
						JSONsend(toClient, JSONtext("TIMES UP! Sorry, you lost!"));
						JSONsend(toClient, JSONimage("failure.jpg"));
						break;
					}
				}
				
				if (correctAnswers == numQuestions) {
					JSONsend(toClient, JSONimage("success.jpg")); // send success image
					JSONsend(toClient, JSONtext("CONGRATULATIONS! You're a Pokémon master!"));
				}
				
				JSONsend(toClient, JSONtext("Press the [X] button to finish."));
				
			} catch (IOException IOex) {
				System.out.println("CLIENT DISCONNECTED");
			} catch (JSONException Jex) {
				System.out.println("JSONException: CLIENT DISCONNECTED.");
			}
		}
	}
	
	public static JSONObject JSONerror(String err) {
		JSONObject json = new JSONObject();
		json.put("datatype", 0);
		json.put("type", "error");
		json.put("data", err);
		return json;
	}
	
	public static JSONObject JSONtext(String s) {
		JSONObject json = new JSONObject();
		json.put("datatype", 1);
		json.put("type", "text");
		json.put("data", s);
		return json;
	}
	
	public static JSONObject JSONimage(String s) throws IOException {
		JSONObject json = new JSONObject();
		json.put("datatype", 2);
		json.put("type", "image");
		
		File imgFile = new File("img/jpg/" + s);
		if (!imgFile.exists()) {
			return JSONerror("ERROR: Server could not send image!");
		}
		
		BufferedImage img = ImageIO.read(imgFile);
		byte[] bytes = null;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			ImageIO.write(img, "jpg", out);
			bytes = out.toByteArray();
		}
		if (bytes != null) {
			Base64.Encoder encoder = Base64.getEncoder();
			json.put("data", encoder.encodeToString(bytes));
			return json;
		} return JSONerror("Unable to save image to byte array");
	}
	
	public static JSONObject JSONquestion(String q) {
		JSONObject json = new JSONObject();
		json.put("datatype", 3);
		json.put("type", "question");
		json.put("data", q);
		return json;
	}
	
	/** 
	 * JSONsend: Sends server JSON to client
	 * @throws IOException: exception thrown when Input/Output is not found
	 */
	public static void JSONsend(OutputStream out, JSONObject json) throws IOException {
        byte[] outputBytes = JsonUtils.toByteArray(json);
        NetworkUtils.Send(out, outputBytes);
    }
	
	/** 
	 * JSONreceive: Receives client JSON
	 * @throws IOException: exception thrown when Input/Output is not found
	 */
	public static JSONObject JSONreceive(InputStream in) throws IOException {
		byte[] inputBytes = NetworkUtils.Receive(in);
		JSONObject input = JsonUtils.fromByteArray(inputBytes);
		
		if (input.has("error")) {
			System.out.println(input.getString("error"));
		} else if (input.has("data")){
	        // System.out.println("Client data received!");
	    }
		return input;
	}
	
	/*
	 * questionManage: manages sending a question to client
	 */
	public static JSONObject questionManage(OutputStream out, InputStream in, JSONObject jsonQuestion, String expected) throws IOException {
		JSONsend(out, jsonQuestion);
		JSONObject clientJson = JSONreceive(in);
		
		if (expected.equalsIgnoreCase("NUMERIC")) {
			String answer = clientJson.getString("data");
			Boolean check = isInteger(answer);
			while(check == false) {
				JSONsend(out, JSONtext("Error: Please enter a whole number!"));
				JSONsend(out, jsonQuestion);
				clientJson = JSONreceive(in);
				answer = clientJson.getString("data");
				check = isInteger(answer);
			}
			return clientJson;
		} 
		else if (expected.equalsIgnoreCase("NONE")) {
			return clientJson;
			
		} else if (expected.equalsIgnoreCase("START")) {
			String answer = clientJson.getString("data");
			Boolean check = answer.equalsIgnoreCase("START");
			while(check == false) {
				JSONsend(out, jsonQuestion);
				clientJson = JSONreceive(in);
				answer = clientJson.getString("data");
				check = answer.equalsIgnoreCase(expected);
			}
			JSONsend(out, JSONtext("MATCH START!"));
			return clientJson;
		} else {
			String answer = clientJson.getString("data");
			Boolean check = answer.equalsIgnoreCase(expected);
			while(check == false) {
				JSONsend(out, JSONtext("Wrong answer! Please try again."));
				JSONsend(out, jsonQuestion);
				clientJson = JSONreceive(in);
				answer = clientJson.getString("data");
				check = answer.equalsIgnoreCase(expected);
			}
			JSONsend(out, JSONtext("CORRECT! It's " + expected + "!"));
			return clientJson;
		}
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
}
