# TCP movie character quote guessing game
This is for the TCP version of the movie quote guessing game.  


## Description

This program is  actually converting all the data to a byte[] and not just sending over the String and letting Java do the rest.

Client connects to server. Server asks Client for their name. The game then starts by providing the player or client a picutre of the first quote from a randomized character selection. The server is on port 8080 and the client has to connect to the server for the game to start. The game is also then displayed on a GUI where the server and client commmunicate. The goal of the game is to guess the movie character that said the quote. The player gets to choose another quote to guess or the quote changes after they get it wrong. If the player is able to guess all of the quotes within the time given (1 minute), they win. If they aren't able to do that, they lose.

For more details see code and/or video.


## Running the game

`cd tcp/`

`For best results run gradle for server before client:`

`gradle runServer`

`gradle runClient`

`type "start" when prompted to start game`

`when entering a guess type in the box and click the submit button`

`The answers show up on the screen that ran the server`

`to end game simply click X at the top right corner of GUI`

# TCP


### Requirements Fullfilled

*	When the user starts up it should connect to the server. The server will
	reply by asking for the name of the player.

*	The user should send their name and the server should receive it and greet
	the user by name.
	
*	The user should be presented a choice between seeing a leader board or
	playing the game (make the interface easy so a user will know what to do).	

*	If the user chooses to start the game, the server will then send over a first
	quote of a character – you need to print the intended answer in the server terminal
	to simplify grading for us (this will be worth some points).

* The user can then do one of three things; enter a guess, e.g. "Jack
  Sparrow", type "more", or type "next". See what the more and next options do in
  the later contraints respectively.
  
* Users can always enter "next" which will make the server send a new
  quote for a new character. If there are no more characters available you can show
  one of the old ones or inform the user and quit the round. You may implement other
  options but do not let things crash.  

*	The user enters a guess and the server must check the guess and respond
	accordingly. If the answer is correct then they will get a new picture with a new
	quote (or they might win - see later). If the answer is incorrect they will be informed
	that the answer was incorrect and can try again.

*	If the server receives 3 correct guesses and the timer did not run out (1
	minute), then the server will send a "winner" image (display in UI or open frame
	when using terminal).

*	If the server receives a guess and the timer ran out the user lost and will
	get a "loser" image and message (display in UI or open frame when using terminal).
  
* We also want a point system so that you get more points for answering
  without asking for more quotes. The point system is to be maintained on the server!
  If you answer on the first quote you get 5 points, answer on the second 4 points, on
  the third 3 points and on the last one 1 point. If the user types "next" they loose
  2 points (overall points for a round can be negative). Current points should always
  be displayed on the users GUI (or in the terminal).  
  
* At the end of a game (if lost or won) display how many points the client
  got. If the user lost, the leader board does not change. If they won add their new
  points to their old points on the leader board. You can assume that their name
  always identifies them.  

*	Your protocol must be robust. If a command that is not understood
	is sent to the server or an incorrect parameterization of the command, then the
	protocol should define how these are indicated. Your protocol must have headers
	and optionally payloads. This means in the context of one logical message the
	receiver of the message has to be able to read a header, understand the metadata
	it provides, and use it to assist with processing a payload (if one is even present).

*	Your programs must be robust. If errors occur on either the client or server
	or if there is a network problem, you have to consider how these should be handled
	in the most recoverable and informative way possible. Implement good general error
	handling and output. Your client/server should not crash even when invalid inputs
	are provided by the user.

*	After the player wins/loses they can start a new game by entering their
	name again or they can quit by typing "quit". After entering their name they can
	choose start or the leader board again.

*	If the server receives enough correct answers (based on num questions)
	and the timer did not run out, then the server will send a "winner" image,

*	Images are only know by and handled on the server.

*	Evaluations of the answer happen on the server side, the client
	does not know the questions and their answers.  
   
## Issues in the code that were not included

* If the user enters "more" then they will get another quote from the same
  movie character. However, If they enter "more" when the final unique image was
  already displayed for this character, then they need to be informed that there are
  no more pictures (quotes) for this character and the image should not change.

# UML Diagram
![](img/jpg/uml.jpg)


# Screencast: 
- Network: https://www.youtube.com/watch?v=3pspc90CBoo
- Game Running: https://www.youtube.com/watch?v=4wXUVHlr7TA

