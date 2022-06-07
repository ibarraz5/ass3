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
	
	static int characterNum=1;
	static Random randomizer = new Random();
	static int character= randomizer.nextInt(7);

	
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
					System.out.println("Server waiting for client to connect...");
					Socket clientSocket = serverSocket.accept();
					
					// create an input stream to receive data
					InputStream fromClient = clientSocket.getInputStream();
					// create an output stream to send data
					OutputStream toClient = clientSocket.getOutputStream();
					
					String cName = "unkown"; // client name string
					int numQuestions = 0; // client numQuestions int
					
					System.out.println("SERVER CONNECTED TO CLIENT!");
					
					JSONsend(toClient, JSONtext("Connection established."));
					JSONsend(toClient, JSONtext("Welcome to Movie Quotes!"));
					JSONsend(toClient, JSONimage("movies-default.jpg")); 
					
					JSONObject clientName = questionManage(toClient, fromClient, JSONquestion("What is your name?"), "NONE");
					cName = clientName.getString("data");
					
					JSONsend(toClient, JSONtext(cName + "! How many movies quotes would you like to guess?"));
					JSONObject clientQ = questionManage(toClient, fromClient, JSONquestion("Enter a number."), "NUMERIC");
					numQuestions = Integer.parseInt((clientQ.getString("data")));
					
					JSONsend(toClient, JSONtext(cName + ", you will have to guess " + numQuestions + " quotes!"));
					
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
	

	static class Match implements Runnable {
		/**
		 * Quotes: nested class to be used in a match.
		 *
		 */
		private static class Quotes {

			private String name;
			private String image;
			private int number;
			public enum names {
				WOLVERINE, BATMAN, IRONMAN, SUPERMAN, JOKER, THANOS, 
				THOR
			}
			
			public Quotes(String name, int num) {
				this.name = name;
				this.number = num;
				this.image = "character-" + number + ".jpg";
			}
			
			public static Quotes[] allQuotes() {
				Quotes[] QuotesArray = new Quotes[7];
				for (names p: names.values()) {
					QuotesArray[p.ordinal()] = new Quotes(p.toString(), p.ordinal());
				}
				return QuotesArray;
			}
			
			public String getName() {
				return this.name;
			}
			
			public String getImage() {
				return this.image;
			}
			public int getInt(){
				return this.number;
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
		Quotes[] allQuotes;
		Quotes[] questionQuotes;
		private int totalQuotes = 7;
		
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
			this.time = 60; //set time for 1 minute max
		}
	
		/**
		 * run: deals with sending and receiving data from client
		 */
		@Override
		public void run() {
			try {
				
				JSONsend(toClient, JSONtext("You will have " + time + " seconds to answer!"));
				JSONsend(toClient, JSONtext("Getting character..."));
				
				allQuotes = Quotes.allQuotes();
				questionQuotes = new Quotes[numQuestions]; 
				
				for (int i = 0; i < numQuestions; i++) {
					questionQuotes[i] = allQuotes[randomizer.nextInt(totalQuotes)];
					int characterNum= questionQuotes[i].getInt();
				}
				
				String choose= "none";
				
				JSONsend(toClient, JSONtext(clientName + ", type 'START' to begin game! or 'LEADERBOARD' for leaderboard!"));
				JSONObject clientChoice = questionManage(toClient, fromClient, JSONquestion("What is your choice?"), "NONE");				
				choose = clientChoice.getString("data");	
				if(choose.equals("start")){
					questionManage(toClient, fromClient, JSONquestion("Type 'start' to confirm?"), "START");

				}else if(choose.equals("leaderboard")){
					questionManage(toClient, fromClient, JSONquestion("Type 'leaderboard' to confirm??"), "LEADERBOARD");
				}				
				JSONsend(toClient, JSONtext("Guess " + numQuestions + " quotes!"));
				
				Calendar cal = Calendar.getInstance();
		        Date startTime = cal.getTime();
		        cal.add(Calendar.SECOND, time);
		        Date finishTime = cal.getTime();
		        Calendar cal2;
				Date currentTime;
				int correctAnswers = 0;
				int points=0;
				
				for (int i = 0; i < numQuestions; i++) {
					
					JSONsend(toClient, JSONimage(questionQuotes[i].getImage()));
					
					System.out.println(questionQuotes[i].getName());
					questionManage(toClient, fromClient, JSONquestion("Who's That Character's Quote?"), questionQuotes[i].getName());
					
					correctAnswers++; JSONsend(toClient, JSONtext("Number of correct answers: " + correctAnswers));
					cal2 =  Calendar.getInstance();
					currentTime = cal2.getTime();
					JSONsend(toClient, JSONtext("Current Time: " + currentTime));
					JSONsend(toClient, JSONtext("Time to Finish: " + finishTime));
					if (currentTime.after(finishTime)) {
						JSONsend(toClient, JSONtext("TIMES UP! Sorry, you lost!"));
						if(correctAnswers==1){
							points=5;
							JSONsend(toClient, JSONtext("You scored "+points+" points!"));
						}else if(correctAnswers==2){
							points=points+4;
							JSONsend(toClient, JSONtext("You scored "+points+" points!"));
						}else if(correctAnswers==3){
							points=points+3;
							JSONsend(toClient, JSONtext("You scored "+points+" points!"));
						}else{
							JSONsend(toClient, JSONtext("You scored no points!"));
						}	
						JSONsend(toClient, JSONimage("lose.jpg"));
						break;
					}
				
				}

				
				if (correctAnswers == numQuestions) {
					JSONsend(toClient, JSONimage("win.jpg")); // send success image
					JSONsend(toClient, JSONtext("CONGRATULATIONS! You're a Winner!"));
					JSONsend(toClient, JSONtext("You scored 13 points!"));					
				}
				
				JSONsend(toClient, JSONtext("Press the [X] button to finish."));
				
			} catch (IOException IOex) {
				System.out.println("CLIENT DISCONNECTED");
			} catch (JSONException Jex) {
				System.out.println("JSONException: CLIENT DISCONNECTED.");
			}
		}
	}
		public static int take(int s){
			return s;
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
			
		}else if(expected.equalsIgnoreCase("LEADERBOARD")){
			JSONsend(out, JSONtext("Leaderboard: 1)Ivan 13 points  2)d 0 points"));
			return clientJson;			
		}else if (expected.equalsIgnoreCase("START")) {
			String answer = clientJson.getString("data");
			Boolean check = answer.equalsIgnoreCase("START");
			while(check == false) {
				JSONsend(out, jsonQuestion);
				clientJson = JSONreceive(in);
				answer = clientJson.getString("data");
				check = answer.equalsIgnoreCase(expected);
			}
			JSONsend(out, JSONtext("GAME START!"));
			return clientJson;
		} else {
			String answer = clientJson.getString("data");
			Boolean check = answer.equalsIgnoreCase(expected);
			int next = 1;
			String nextS= "next";
			String more="more";
			Boolean check2 = answer.equalsIgnoreCase(nextS);
			Boolean check3 = answer.equalsIgnoreCase(more);
			
			
			for(int i=1; i<3; i++){ 
			if(check == false) {
				JSONsend(out, JSONtext("Wrong answer! Please try again."));
				JSONsend(out, jsonQuestion);
				clientJson = JSONreceive(in);
				answer = clientJson.getString("data");
				check = answer.equalsIgnoreCase(expected);
				JSONsend(out, JSONimage("character-"+characterNum+"-"+next+".jpg"));
				characterNum++;
			}else if(check==true){
			JSONsend(out, JSONtext("CORRECT! It's " + expected + "!"));
			return clientJson;
			}
			}
			JSONsend(out, JSONtext("Wrong you're out of guesses! It's " + expected + "!"));			
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

