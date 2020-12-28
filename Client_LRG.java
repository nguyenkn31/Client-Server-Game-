package nonGUI_755;

import java.io.*;
import java.net.*;
import java.util.*;

class Client_LRG {
    public static Scanner keyboard = new Scanner(System.in);

    public static void main(String argv[]) throws Exception {        
        /* The first thing we do is to prepare the socket, also the receiver and sender channels to the server */
        Socket clientSocket = new Socket("localhost", 3110);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String status = "Initialize";

        while (true) {
            /* We will send the user's request to the server and then receives the server's responses based on that request */
            String request = "";
            switch(status) {
            /* If the user fails to login, we will ask the user to login again (until he/she successfully logins) */
            case "LoginFailure":
            	request = loginRequest();
            	break;
            
            /* If the user successfully logins, we will redirect him/her to the main menu */	
            case "LoginSuccess":
            	request = mainMenu();
            	break;
             
            /* If the user just turns on the program or just success */	
            case "Initialize":
            case "RegisterSuccess":
            	request = initializeRequest();
            	break;

            /* If the user fails to register for an account (refer to the server side for the requirements), we asks him to register again */
            case "RegisterFailure":
            	request = registerRequest();
                break;
            
            /* If the user chooses to play the game, we will redirect him/her to the gaming section */    
            case "PlayGame":
            	request = gamePlay();
            	break;
            
            /* If the user chooses to end the game, we will redirect him/her to the main menu again, to either play a new game or logout */
            case "EndGame":
            	request = mainMenu();
            	break;
                
            /* If the user chooses to logout, we close the connection of course, logs him/her out */
            case "LogoutSuccess":
            	default:
            		clientSocket.close();
            		return;
            } // End of the switch-statement

            
            /* This is to encrypt the request then sends it to the server */
            request = AES.encrypt(request) + "\n";
            outToServer.writeBytes(request);

            /* This is to decrypt the response from the server */
            String response = inFromServer.readLine();
            response = AES.decrypt(response);

            /* Since this is very dynamic (based on the user's request),
             * our status must dynamically change with the request/response as well */
            status = response.split(" ")[0];

            /* This code is to display the server's response (on the user's request) */
            System.out.println(response.substring(response.indexOf(status) + status.length()).trim());
        } // End of the while-loop
    } // End of the main method
    
    /* This method here is used as the default window when the user first opens up the program.
     * We either asks him/her to login or to register for an account */
    public static String initializeRequest() {
        System.out.print("Welcome to Now You See Me! Please enter your choice:\n(1) Login\n(2) Register\n> ");
        int choice = keyboard.nextInt();
        keyboard.nextLine();

        switch(choice) {
        /* Choice 1 means the user wants to login, so we redirects the user the login part */
        case 1:
        	return loginRequest();
        	
        /* Choice 2 means the user wants to register, so we redirects the user the register part */
        case 2:
        	return registerRequest();
        	
        default:
        	System.out.println("Invalid input. Please try again!");
        	return initializeRequest();
        }
    } // End of initializeRequest

    /* This method is used to handle the users' login request */
    public static String loginRequest() {
        System.out.print("Please enter your email and password here (press Enter after you are done typing each field):\n> ");
        String email = keyboard.nextLine(), password = keyboard.nextLine();
        /* We have the word "Login" at the beginning, so that when we send the request to the server, it will know that the user 
         * is trying to "Login" */
        return String.format("Login\t%s\t%s", email, password);
    } // end of loginRequest()

    /* This method is used to handle the users' register request */
    public static String registerRequest() {
        System.out.print("Please register by entering your first name, your last name, your email, your password, "
        		+ "and retyping your password (Press Enter each time you are finished typing a field):\n> ");
        String firstName = keyboard.nextLine(), lastName = keyboard.nextLine(), email = keyboard.nextLine(), password = keyboard.nextLine(), retypedPassword = keyboard.nextLine();
        /* We have the word "Register" at the beginning, so that when we send the request to the server, it will know that the user 
         * is trying to "Register" */
        return String.format("Register\t%s\t%s\t%s\t%s\t%s", firstName, lastName, email, password, retypedPassword);
    } // end of registerRequest()
    
    /* This method serves as the main menu when the user has successfully logged in */
    public static String mainMenu() {
        System.out.print("Please enter your choice:\n(1) Play the game\n(2) Logout\n> ");
        int choice = keyboard.nextInt();
        keyboard.nextLine();
        switch (choice) {
        /* Choice 1 means that the user wants to play the game, so we redirects the user to the gaming section */
        case 1:
        	return gamePlay();
        
        /* Choice 2 means that the user wants to logout, so we log him/her out */
        case 2:
        	return "Logout";
        
        /* Otherwise, the input is invalid */
        default:
        	System.out.println("Invalid input.");
        	return mainMenu();
        }
    } // end of mainMenu()

    
    /* This method allows the user to play the game */
    public static String gamePlay() {
    	System.out.println("Here is the rule of the game: ");
        System.out.println("Scissors cuts paper, paper covers rock, rock crushes lizard, lizard poisons Spock, ");
        System.out.println("Spock smashes scissors, scissors decapitates lizard, lizard eats paper, paper disproves Spock, ");
        System.out.println("Spock vaporizes rock, and as it always has, rock crushes scissors. Good luck!");
        System.out.println("Please make a choice: ");
    	String humanChoice = keyboard.nextLine();  
    	
    	/* We have the word "PlayGame" at the beginning, so that when we send the request to the server, it will know that the user 
         * is trying to "PlayGame" */
    	return String.format("PlayGame\t%s", humanChoice);
    } // end of gamePlay()
}