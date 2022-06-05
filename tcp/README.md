# TCP movie character quote guessing game
This is for the TCP version of the movie quote guessing game.  


## Description

This program is  actually converting all the data to a byte[] and not just sending over the String and letting Java do the rest.

Client connects to server. Server asks Client for their name. 

For more details see code and/or video.


## Running the game

`cd tcp/`

`gradle runServer`
`gradle runClient`

### Requirements Fullfilled

*	When the clients starts up connects to the Server, the server will
	reply by asking for the name of the player.

*	The client should send their name and the server should receive it and
	greet the client by name and ask for the number of questions the client wants to try
	to answer correctly in time.

*	The client should enter a number and the server should use that number and the
	previous client name to tell them they are ready to play.

*	After the user enters the name and num questions the server waits for a
	"start" input which will start the question round.

*	When the server receives a "start" it will start a timer with 5 sec * num
	questions. This is the time the client has to answer "num" questions correctly.

*	The server will then send over the first question with an image of the pokemon.
	The answer is printed in the server commandline.

*	The client must respond with the name of the pokemon. The client itself does not
	know the answers nor store the questions.

*	The client enters an answer and the server checks the answer and responds accordingly.
	The client can try as many times as they would like to give the correct answer.

*	After each question loop, the server checks the current time and compares it to how much
	time is left. If time has run out, the server send out "Time Out!" and a failure image

*	If the server receives enough correct answers (based on num questions)
	and the timer did not run out, then the server will send a "winner" image,

*	The server sends out an image at the start, for each pokemon, and for win/lose conditions

*	Images are only know by and handled on the server.

*	Evaluations of the answer happen on the server side, the client
	does not know the questions and their answers.   
   
## Issues in the code that were not included on purpose
The code is basically to show you how you can use a TCP connection to send over different data and interpret it on either side. It focuses on this alone and not on error handling and some nicer features.
It is suggested that you play with this and try to include some of the below for your own practice. 

- Not very robust, e.g. user enters String
- Second client can connect to socket but will not be informed that there is already a connection from other client thus the server will not response
	- More than one thread can solve this
	- can consider that client always connects with each new request
		- drawback if server is working with client A then client B still cannot connect, not very robust
- Protocol is very simple no header and payload, here we just used data and type to simplify things
- Error handling is very basic and not complete
- Always send the same joke, quote and picture. Having more of each and randomly selecting with also making sure to not duplicate things would improve things

# UML Diagram
![](img/jpg/uml.jpg)

# Screencast: 

