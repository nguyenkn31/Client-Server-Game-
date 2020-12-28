package nonGUI_755;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

class Server_LRG {
    static String loggedInUser;

    public static void main(String[] args) throws Exception {
        ServerSocket welcomeSocket = new ServerSocket(3110);
        /* This line is just for debugging purpose */
        System.out.println("The server is now running on " + welcomeSocket.getLocalSocketAddress());

        process: while(true) {
        	/* We are trying to prepare the socket, also the receiver and sender channels to the server */
            Socket connectionSocket = welcomeSocket.accept();
            System.out.println("Connected to " + connectionSocket.getInetAddress());
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            loggedInUser = "";
            
            while(true) {
            	String response = "";
            	String request = inFromClient.readLine();
                if(request == null) {
                	/* This line here currently does not work - I will need a way to fix it */
                	response = "Invalid Some fields are blank. Please note that all fields are required";
                	continue process;
                }
                /* This is to decrypt the request from the client side */
                request = AES.decrypt(request);

                /* This line here means that we are splitting the request from the user into chunks of strings, that we
                 * will use to process in our server later in the code */
                String[] requestSplit = request.split("\t");

                switch(requestSplit[0]) {
                /* If the user's request contains register request then we return the response related to register */
                case "Register":
                	response = registerResponse(requestSplit[1], requestSplit[2], requestSplit[3], requestSplit[4], requestSplit[5]);
                	break;
                
                /* If the user's request contains register request then we return the register related to register */
                case "Login":
                	response = loginResponse(requestSplit[1], requestSplit[2]);
                	if(response.contains("LoginSuccess")) {
                		loggedInUser = requestSplit[1];
                		}
                	break;
                
                /* If the user's request contains play game request then we return the response related to play game */	
                case "PlayGame":
                	response = playGame(requestSplit[1]);
                	break;
                	
                /* If the user's request contains logout request then we return the response related to play game */	
                case "Logout":
                	loggedInUser = "";
                	response = "LogoutSuccess Thank you for playing Now You See Me! I hope to see you soon.";
                	} // End of the switch case

                /* This is to encrypt the response then sends it back to the client slide */
                response = AES.encrypt(response) + "\n";
                outToClient.writeBytes(response);
            }
        }
    }

    /* This method here handles the register's request from the user */
    public static String registerResponse(String firstName, String lastName, String email, String password, String retypedPassword) throws SQLException {
    	String returnStatement = "";
    	Connection connection = null;
		connection = establishConnection();
		
		/* This code here scans through the database to see whether the email already exists or not */
		final String queryCheck = "SELECT * from usersdata WHERE email = ?";
		final PreparedStatement ps = connection.prepareStatement(queryCheck);
		ps.setString(1, email);
		final ResultSet resultSet = ps.executeQuery();
    	
    	try {
    		/*********************** THESE 3 LINES CURRENTLY ARE NOT WORKING - I WILL FIX IT LATER ***********************/
    		/* First, we check whether any of the field that the user enters is blank or not */
    		//if(firstName == null || lastName == null || email == null || password == null || retypedPassword == null) {
                //returnStatement = "RegisterFailure Some fields are left blank. Please note that all fields are required. Please try again";
                //}
    		
    		/* Second, you check whether the user has an account in the database - refer to lines 80 to 83 for more information */
    		if(resultSet.isBeforeFirst()) {
    			connection.close();
    			returnStatement = "RegisterFailure This email is already registered. If you are the owner of this accont, please proceed to login!";
    		}
    		
    		/* Third, we check whether the email is valid or not */
    		else if(emailValidator(email) == false) {
    			returnStatement = "RegisterFailure It seems that you have entered an invalid email. Please try again!";
    		}
    		
    		/* Fourth, we check whether the password is valid or not */
    		else if(passwordValidator(password, retypedPassword) != "Valid") {
    			returnStatement = passwordValidator(password, retypedPassword);
    		}
    		
    		/* If no condition above is satisfied then everything is fine - we will add the information to the database */
    		else {
				connection = null;
				connection = establishConnection();
				String query = "INSERT INTO `usersdata`(`firstName`, `lastName`, `email`, `password`, `retypedPassword`) VALUES (?,?,?,?,?)";
				PreparedStatement pst = connection.prepareStatement(query);
				pst.setString(1, firstName);
				pst.setString(2, lastName);
				pst.setString(3, email);
				pst.setString(4, password);
				pst.setString(5, retypedPassword);
				pst.executeUpdate();
				pst.close();

				returnStatement = "RegisterSuccess You are now registered! You will redirected to the login page now. ";
			}
    	}catch(Exception e) {}
    	return returnStatement;
    }

    /* The method handles the login's request from the user */
    public static String loginResponse(String email, String password) throws Exception{
    	String returnStatement = "";
    	Connection connection = null;
		connection = establishConnection();
		
		/* Similar to lines 80 to 83, we check whether the email and password match to those we have in the database */
		final String queryCheck_email = "SELECT * from usersdata WHERE email = ?";
		final String queryCheck_password = "SELECT * from usersdata WHERE password = ?";
		
		final PreparedStatement ps_email = connection.prepareStatement(queryCheck_email);
		final PreparedStatement ps_password = connection.prepareStatement(queryCheck_password);
		
		ps_email.setString(1, email);
		ps_password.setString(1, password);
		
		final ResultSet resultSet_email = ps_email.executeQuery();
		final ResultSet resultSet_password = ps_password.executeQuery();
		
		try {
			/* First, if we cannot find the user's email, we return this statement */
			if(!resultSet_email.isBeforeFirst()) {
				connection.close();
    			returnStatement = "LoginFailure We cannot find any account associated with that email. Please try again!";
			}
			
			/* Second, if we can find the email but the password do not match then we return that the password is incorrect */
			else if(!resultSet_password.isBeforeFirst()) {
				connection.close();
    			returnStatement = "LoginFailure The password that you entered is incorrect. Please try again!";
			}
			
			/* Else, then probably the user has entered valid information */
			else {
				returnStatement = "LoginSuccess You are logged in!";
			}
		}catch(Exception e) {}
		return returnStatement;
    }
    
    
    /* This method will connect to MySQL database >> userdata */
    public static Connection establishConnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/userdata","root","");
			return connection;
		}catch(Exception e)
		{return null;}
	}
    
    
    /* This method checks whether the password is valid or not */
	public static String passwordValidator(String password, String passwordRetyped){
    	String returnStatement = "";
    	String correctPattern = "^[a-zA-Z0-9]{8,}$";
    	
    	int characterCount = 0;
        int numuricCount = 0;
        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (is_Numeric(ch)) {
            	numuricCount++;
            }
            else if (is_Letter(ch)) {
            	characterCount++;
            }
        }
    	
		/* First, we check whether the password is at least 15-character long */
        if (password.length() < 15) {
        	returnStatement = "RegisterFailure Your password is too short - it needs at least 15 characters. Please try again! ";
        }
        
        /* If the password is indeed longer than 15 characters then proceed */
        /* Second, we check whether the password is mixed between character and numeric */
        else if(!(numuricCount > 0 && characterCount > 0)) {
        	returnStatement = "RegisterFailure Your password must be mixed between words and numbers. Please try again!";
        }
        else if(!password.matches(correctPattern)) {
        	returnStatement = "RegisterFailure Your password contains special characters, which are not allowed. Please try again!";
        }
        
        else if(passwordCompare(password, passwordRetyped) != 0) {
        	returnStatement = "RegisterFailure Your password and confirmation password do not match. Please try again!";
        }
        else {
        	returnStatement = "Valid";
        }
        return returnStatement;
    }

	
	/* This method checks whether a character is a letter */
	public static boolean is_Letter(char character){
		character = Character.toUpperCase(character);
        return (character >= 'A' && character <= 'Z');
    }
	
	
	/* This method checks whether a character is numeric */
    public static boolean is_Numeric(char character) {
        return (character >= '0' && character <= '9');
    }
    
    
    /* This method checks whether the password and the passwordRetyped are the same */
    public static int passwordCompare(String password, String passwordRetyped){ 
        int passwordLength = password.length(); 
        int passwordRetypedLength = passwordRetyped.length(); 
        int lmin = Math.min(passwordLength, passwordRetypedLength); 
  
        for (int i = 0; i < lmin; i++) { 
            int str1_ch = (int)password.charAt(i); 
            int str2_ch = (int)passwordRetyped.charAt(i); 
  
            if (str1_ch != str2_ch) { 
                return str1_ch - str2_ch; 
            } 
        } 

        if (passwordLength != passwordRetypedLength) { 
            return passwordLength - passwordRetypedLength; 
        } 
  
        // If none of the above conditions is true, 
        // it implies both the strings are equal 
        else { 
            return 0;
            } 
        }
    
    
    /* This method is to check validity of the email */
    /* I borrowed this from https://howtodoinjava.com/regex/java-regex-validate-email-address/ */
    public static boolean emailValidator(String email) {
    	String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    	Pattern pattern = Pattern.compile(regex);
    	Matcher matcher = pattern.matcher(email);
    	if(matcher.matches()) {
    		return true;
    	}
    	return false;
    }
    
    
    /********************************** THIS SECTION IS FOR THE GAME *********************************/
	
    /* First, we create the list of Choices for the human and NPC to choice from 
     * And also the rules of the game, specifically which Choices wins against which Choices */
    public static enum Choices {
	    ROCK, PAPER, SCISSORS, LIZARD, SPOCK;

	    public List<Choices> losesTo;

	    public boolean losesTo(Choices other) {
	      return losesTo.contains(other);
	    }

	    static {
	    	SCISSORS.losesTo = Arrays.asList(ROCK, SPOCK);
	    	ROCK.losesTo = Arrays.asList(PAPER, SPOCK);
	    	PAPER.losesTo = Arrays.asList(SCISSORS, LIZARD);
	    	SPOCK.losesTo = Arrays.asList(PAPER, LIZARD);
	    	LIZARD.losesTo = Arrays.asList(SCISSORS, ROCK);
	    }
	  }
    
    
    /* Initialize section: we initialize any static variables here */
    private static DecimalFormat DECIMAL_FORMATTER = new DecimalFormat(".##");
    public static final Random RANDOM = new Random();
    
    /* These are the statistics of the game (human's win / human's tie / human's lost)*/
    private static int[] stats = new int[] {0, 0, 0};
    private static int[][] markovChain;
    private static int currentTurn = 0;
    private static Choices last = null;
    /* End of initialize static variables section */
    
    
    /* Second, we initialize the game */
    private static void gameInitialize() {
        int length = Choices.values().length;
        markovChain = new int[length][length];

        for (int i = 0; i < length; i++) {
          for (int j = 0; j < length; j++) {
            markovChain[i][j] = 0;
          }
        }
      }
    
    /* This method is used to calculate the best move for the AI based on the users' previous move */
    private static Choices calculateNextMove(Choices prev) {
    	/* Obviously, if there is no turn before the first one so the AI cannot predict anything */
    	/* Hence, we simply randomize its move */
    	if (currentTurn < 1) {
          return Choices.values()[RANDOM.nextInt(Choices.values().length)];
        }

    	/* Otherwise, we are going to set up a calculation for our AI */
        int nextIndex = 0;

        for (int i = 0; i < Choices.values().length; i++) {
          int prevIndex = prev.ordinal();

          if (markovChain[prevIndex][i] > markovChain[prevIndex][nextIndex]) {
            nextIndex = i;
          }
        }

        // Choices highly likely played by the user is in nextIndex
        Choices predictedNext = Choices.values()[nextIndex];

        // We choose amongst Choices for which this probably Choices loses
        List<Choices> losesTo = predictedNext.losesTo;
        return losesTo.get(RANDOM.nextInt(losesTo.size()));
      }
    
    /* This method is used simply to update the Markov Chain based on the user's previous choice */
    private static void updateMarkovChain(Choices prev, Choices next) {
        markovChain[prev.ordinal()][next.ordinal()]++;
      }
    
    /* Here is the important part of the game */
    public static String playGame(String humanInput) {
        String returnStatement = "";
        int counter = 0;
    	gameInitialize();

        Choices choice = null;
        process: while(humanInput.toUpperCase().equals("ROCK") || humanInput.toUpperCase().equals("PAPER") || humanInput.toUpperCase().equals("SCISSORS")|| humanInput.toUpperCase().equals("SPOCK")|| humanInput.toUpperCase().equals("LIZARD")|| humanInput.toUpperCase().equals("STOP")) {
            if ("STOP".equals(humanInput.toUpperCase())) {
            	 return displayStatistics();
            }
          
          try {
            choice = Choices.valueOf(humanInput.toUpperCase());
          } catch (Exception e) {
            System.out.println("You have put in an invalid choice. Please try again.");
            continue process;
          }

          Choices aiChoice = calculateNextMove(last);
          currentTurn++;

          /* We update the Markov Chain here */
          if (last != null) {
            updateMarkovChain(last, choice);
          }
          last = choice;

          if (aiChoice.equals(choice)) {
            returnStatement = "The computer choice is: " + aiChoice + ". Wow! It is a tie!\n";
            stats[1]++;
          } else if(aiChoice.losesTo(choice)) {
        	returnStatement = "The computer choice is: " + aiChoice + ". Congratulations! You win! \n";
            stats[0]++;
          } else {
            returnStatement = "The computer choice is: " + aiChoice + ". Oh no...you lost. :( \n";
            stats[2]++;
            }
          counter++;
          if(counter == 1) {
        	  break;
        	  }
          }
        
        return "PlayGame " + returnStatement;
        }        
    
    
    /* This section displays the game statistics */
    public static String displayStatistics() {
    	String returnStatement = "";
    	int total = stats[0] + stats[1] + stats[2];
    	returnStatement = "Game Statistics:\n"
    			+ "The total number of rounds that you played: " + total + "\n"
    			+ "The number of rounds you won: " + stats[0] + " (" + DECIMAL_FORMATTER.format(stats[0] / (float) total * 100f) + "%) \n"
    			+ "The number of rounds tie games: " + stats[1] + " (" + DECIMAL_FORMATTER.format(stats[1] / (float) total * 100f) + "%) \n"
    			+ "The number of rounds you lost: " + stats[2] + " (" + DECIMAL_FORMATTER.format(stats[2] / (float) total * 100f) + "%)";

        return "EndGame " + returnStatement;
    }
}