TCP Picture Guessing Game

a) Description:
	The picture guessing game is a single connection server/client model that adheres to the TCP protocol.
	ServerTCP.java should be executed prior to to the ClientGui.java.  The user has the option to execute 
	the server with a designated port value.  This can enter via the command line using by entering "-Pport=<Value>".
	If a custom port value is not enter the default port 8080 will be used.  Following the server, the ClientGui can be
	invoked within the command line.  The user can enter a custom host ip address and custom port value.  This 
	is accomplished by using entering "-Pport=<Value>" and "-Phost=<Value>".  If a custom port value is entered, ensure 
	that this value is the same as the server or the communication will fail to connected. 

	The purpose of this game is for the client to guess what a snipped image is with a given amount of time. Each 
	picture will contain exactly 3 snippets of the complete the picture. If the player is able to guess what the images
	within the alloted time then he or she will win the game.  The player will lose if the time exceeds before 
	they are able to guess all of the images.

	Requirements Met:
	1. When the clients starts up it should connect to the Server, the server will
	reply by asking for the name of the player.

	2. The client should send their name and the server should receive it and
	greet the client by name and ask for the number of "questions" (differnt pictures)
	the client wants to try to answer correctly in time.

	3. The client should enter a number. This number represents the number of
	images the client wants to guess.

	4. The server should use that number and the previous client name to tell
	them they are ready to play. If the number is larger than the number of images the
	server knows an appropriate response should be send back and it should be handles
	in a good way.

	6. clarified this a bit more After the server tells the client they are
	ready to play (see step #4, the server waits for a ’start’ input" from the client.

	7. When the server receives a "start" it will start a timer with 30 sec * num
	questions. This is the time the client has to guess the X pictures.

	8. The server will then send over a part of an image (display this either in
	the UI or in a pop up frame) – you need to print the answer in the server terminal
	to simplify grading for us.

	9. The guesses to the pictures are always one token answers. So assume the
	picture shows a car, then the answer would be "car", simple as that. You can choose
	any pictures you like, we give a couple of examples. You will need 3 version of each
	picture, each shows more details so the client has a better chance to guess. The
	client will not know the pictures or the answers! Images are ONLY on the server, so
	the client needs to receive them through communication with the server.

	10. The client enters a guess and the server must check the guess and respond
	accordingly. If the answer is correct then they will get a new picture if the answer
	is incorrect they will be informed that the answer was incorrect and can try again.
	If the client enteres "more" into the field then they will get a more detailed picture.
	If they enter "more" when the last image was already displayed for this picture then
	they need to be informed that there are no more pictures for this.

	12. If the server receives a guess and the timer ran out the client lost and will
	get a "looser" image and message (display in UI or open Frame when terminal).

	13. If the server receives enough correct guesses (based on num questions)
	and the timer did not run out, then the server will send a "winner" image (display
	in UI or open Frame when terminal).

	14. Evaluations of the input needs to happen on the server side, the client
	will not know the pictures and their answers. They are send and evaluated by the
	server.

	15. Your protocol must be robust. If a command that is not understood
	is sent to the server, or an incorrect parameterization of the command, then the
	protocol should define how these are indicated. Your protocol must have headers
	and optionally payloads. This means in the context of one logical message the
	receiver of the message has to be able to read a header, understand the metadata it
	provides, and use it to assist with processing a payload (if one is even present).

	16. Your programs must be robust. If errors occur on either the client or
	server, or there is a network problem, you have to consider how these should be
	handled in the most recoverable and informative way possible. Good general error
	handling and output. Your client/server should not crash even when wrong things
	are written in the input field.

b) How to Run:
	Step 1: open cli and navigate to "build.gradle" file path
	Step 2: gradle runServer [Optional -Pport=<custom port value>]
	Step 2: gradle runClient [Optional -Pport=<custom port value> -Phost=<custom ip address>]

c) Sequence Diagram
	!(sequence_diagram.png)

d) Protocol Description
	JSON objects are used to pass data between Server Client.  All JSON objects contain a sequence value witch allows the 
	receiver to confirm the proper sequence and expectation of data.  These objects also contain a datatype which takes validation
	one set further and limits errors from parsing.  Finally, there is the data itself which allows the program to progess.

	d.1) ServerTCP
	nameRequest
	Purpose: To establish the clients required for the game.
	Possible Response: Out of sequence error.

	quantityRequest
	Purpose: To establish the picture count required for the game.
	Possible Response: Errors responses for values other than integers, and an out of sequence error.

	readyRequest
	Purpose: Confirmation for the game to start the timer and send the first image.
	Possible Response: Errors responose for values other than "ready" (not case sensitive), and an out of sequence error.

	image
	Purpose: Send images to the client.
	Possible Response: Errors responos for unknown file path, unable to save image to byte array, and an out of sequence error.

	d.2) ClientGui
	reqName
	Purpose: Respond and send name to the server.
	Possible Response: Out of sequence error.

	reqPicCount
	Purpose: Respond and send picture count to the server.
	Possible Response: Errors responses for values other than integers, and an out of sequence error.

	reqConfig
	Purpose: Respond and send "ready" to the server.
	Possible Response: Errors responose for values other than "ready" (not case sensitive), and an out of sequence error.

	reqGuess
	Purpose: Respond and send guess for the current image send by the server.
	Possible Response: Out of sequence error.
