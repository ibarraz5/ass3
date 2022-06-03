package TCP_ServerClient;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status. 
 * 
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 *     -> modal means that it opens the GUI and suspends background processes. Processing 
 *        still happens in the GUI. If it is desired to continue processing in the 
 *        background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * 
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 * 
 */
public class ClientGui extends Application {
	/**
	 * ClientGui class properties:
	 * 
	 */
	private static String stageTitle = "Guess the Character Quote?";
	private static Image defImage;
	private static ImageView imageSection;
	private static TextField answerField = new TextField();
	private static String answerToSend = "";
	private static submitButton submitButton;
	private static TextArea outputArea = new TextArea();
	private static int stageSizeX = 500;
	private static int stageSizeY = 800;
	
	/**
	 * start: shows the main stage of Who's that pokemon?
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Stage pokemonStage = new Stage();
        pokemonStage.setTitle(stageTitle);
        pokemonStage.setResizable(false);
        
        //Setup 'Who's that Pokemon?' picture
        imageSection = new ImageView(); // new image view
        // setNewImage(null);
        imageSection.setPreserveRatio(true); // preserve ratio of the image
        imageSection.setFitWidth(stageSizeX); // set height of imageView
        imageSection.setFitHeight(stageSizeY / 2); // set width of imageView
        BorderPane imagePane = new BorderPane(); // create new Pane to hold imageView
        imagePane.setCenter(imageSection); // add imageView to center
        imagePane.setPrefHeight(stageSizeY / 2); // imagePane pref size takes up half the screen
        
        // Setup input and 'submit' button
        answerField.setPrefWidth(stageSizeX * .8);
        answerField.setPrefHeight(stageSizeY * .05);
        answerField.setPromptText("Enter your answer here.");
        submitButton = new submitButton(stageSizeX * .2, stageSizeY * .05);
        HBox inputPane = new HBox(0); // new HBox with spacing = 0
        inputPane.getChildren().addAll(answerField, submitButton);
        
        // Setup output area
        outputArea.setPrefWidth(stageSizeX);
        outputArea.setPrefHeight(stageSizeY * .45);
        outputArea.setWrapText(true); // remove horizontal scrollbar
        
        // Joining the imagePane, inputPane and outputArea together
        BorderPane GameArea = new BorderPane();
        GameArea.setTop(imagePane);
        GameArea.setCenter(inputPane);
      	GameArea.setBottom(outputArea);
        
        Scene pokemonScene = new Scene(GameArea, stageSizeX, stageSizeY);
        pokemonStage.setScene(pokemonScene);
        
        Client.setRunning(true); // tell client logic that GUI is ready
        pokemonStage.show();
        
        pokemonStage.setOnCloseRequest((EventHandler<WindowEvent>) new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing");
                Client.setRunning(false);
                pokemonStage.close();
                System.exit(0);
            }
        });
	}
	
	/**
	 * setNewTitle: this method sets a new title on the Stage
	 * @param t: text to be set as new title
	 */
	public static void setNewTitle(String t) {
		stageTitle = t;
	}
	
	/**
	 * setNewTitle: this method sets a new image for the Stage
	 * @param t: text to be set as new title
	 */
	public static void setNewImage(Image img) {
		if (img == null) {
			String imgURL = "https://i.imgflip.com/p1dt4.jpg?a448272";
			defImage = new Image(imgURL); // default Image with imgURL
			imageSection.setImage(defImage);
		} else imageSection.setImage(img);
	}
	
	/**
	 * setOutputText this method sets a new text status on the Stage
	 * @param t: text to be appended
	 */
	public static void setOutputText(String t) {
		outputArea.appendText(t + "\n");
	}
	
	/**
	 * getInputFieldText: this method gets the text in the Stage answerField
	 * @param t: text to be appended
	 */
	private static String getInputFieldText() {
		return answerField.getText();
	}
	
	/**
	 * clearTextField: clears the answerField
	 */
	private static void clearTextField() {
		answerField.clear();
	}
	
	/**
	 * setAnswer: set method
	 */
	private static void setAnswer(String s) {
		answerToSend = s;
	}
	
	/**
	 * getAnswer: get method
	 */
	public static String getAnswer() {
		return answerToSend;
	}
	
	/**
	 * submitButton: nested class creates submit Button object that is to be displayed in the GUI
	 */
	private class submitButton extends Button {
		
		/**
		 * submitButton Constructor: creates a submitButton object
		 * @param X: button width
		 * @param Y: button height
		 */
		public submitButton(double X, double Y) {
			this.setPrefWidth(X);
	        this.setPrefHeight(Y);
			this.setText("Submit");
			this.setOnMouseClicked(e -> handleMouseClick());
		}
		
		/**
		 * handleMouseClick: this method handles what happens when a button is clicked
		 */
		public void handleMouseClick() {
			if (getInputFieldText() == null || getInputFieldText().contentEquals("")) {
				setOutputText("Error: please type something...");
			}
			else {
				setAnswer(getInputFieldText());
				clearTextField();
				Client.setWaiting(false);
			}
		}
	}
}
