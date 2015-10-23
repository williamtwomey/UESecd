import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UESecd {

  private static int port=19876, maxConnections=0;

  // Listen for incoming connections and handle them
  public static void main(String[] args) {
    int i=0;

    try{
      ServerSocket listener = new ServerSocket(port);
      Socket server;

      while((i++ < maxConnections) || (maxConnections == 0)){
        doComms connection;

        server = listener.accept();
        doComms conn_c= new doComms(server);
        Thread t = new Thread(conn_c);
        t.start();
      }
    } catch (IOException ioe) {
      System.out.println("IOException on socket listen: " + ioe);
      ioe.printStackTrace();
    }
  }

}

class doComms implements Runnable {
    private Socket server;
    private String line,input;

    doComms(Socket server) {
      this.server=server;
    }


     public String checkSMS(String line)
{
try {
String[] tmp;
tmp = line.split(" ");
//System.out.println("0: " + tmp[0]);
//System.out.println("2: " + tmp[2]);
if ( tmp[0].equals("SMS") )
{
return tmp[2];
}
else
{
return null;
}

}
catch (Exception e)
{
return null;
}
}


    public void run () {

      input="";

      try {
        // Get input from the client
        DataInputStream in = new DataInputStream (server.getInputStream());
        PrintStream out = new PrintStream(server.getOutputStream());

        while((line = in.readLine()) != null && !line.equals(".")) {

          input=input + line;
	//Sends command to client
          //out.println("I got:" + line);


	System.out.println(server.getInetAddress() + " " + line);

	String smsphone = checkSMS(line);
	System.out.println("smsphone: " + smsphone);
	if ( smsphone != null )
	{

	//MySQL
	Connection con = null;
	PreparedStatement pStmnt = null;
	try {
		String url = "jdbc:mysql://localhost:3306/uesec";
		String user = "uesec";
		String password = "red4go";

		con = DriverManager.getConnection(url, user, password);
		String q = "INSERT INTO User (phone) VALUES (?)";
		pStmnt = con.prepareStatement(q);
		pStmnt.setString(1, smsphone);
		pStmnt.executeUpdate();

        if (pStmnt != null)
        {
                pStmnt.close();
        }
        if ( con != null ) {
                con.close();
        }


	}
	catch (SQLException e1)
	{
		e1.printStackTrace();
	}
	catch (Exception e2)
	{
		e2.printStackTrace();
	}


	String ftmp =  "/home/will/UESec/sms/" + smsphone;
	File f2 = new File(ftmp);
	if (!f2.exists()) {
		f2.createNewFile();
	}
	 FileWriter fstream = new FileWriter(ftmp,true);
	 BufferedWriter outfile = new BufferedWriter(fstream);
         outfile.write(input+"\n\n");
         outfile.close();

	}	

        }

        // Now write to the client

        //System.out.println("Overall message is:" + input);
	String filename = "/home/will/UESec/"+server.getInetAddress();

	File f = new File(filename);
	if (!f.exists()) {
	f.createNewFile();
	}

	FileWriter fstream = new FileWriter(filename,true);
	BufferedWriter outfile = new BufferedWriter(fstream);
        outfile.write(input+"\n\n");
	outfile.close();

        out.println("Overall message is:" + input);

        server.close();
      } catch (IOException ioe) {
        System.out.println("IOException on socket listen: " + ioe);
        ioe.printStackTrace();
      }
    }
}

