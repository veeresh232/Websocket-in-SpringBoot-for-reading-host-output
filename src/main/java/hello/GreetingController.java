package hello;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.HtmlUtils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


@Controller
public class GreetingController {
	
	@Autowired
	private SimpMessagingTemplate template;
	private static String USERNAME ="ussnggsb"; // username for remote host
	 private static String PASSWORD ="Backfromvacati0n"; // password of the remote host
	 private static String host = "nggapppg01.mmm.com"; // remote host address
	 private static int port=22;


    
    @MessageMapping("/hello")
    public void greeting(HelloMessage message) throws Exception {
    	System.out.println("Inside greeting controller");
    	List<String> result = new ArrayList<String>();
        try
        {

            /**
            * Create a new Jsch object
            * This object will execute shell commands or scripts on server
            */
            JSch jsch = new JSch();

            /*
            * Open a new session, with your username, host and port
            * Set the password and call connect.
            * session.connect() opens a new connection to remote SSH server.
            * Once the connection is established, you can initiate a new channel.
            * this channel is needed to connect to remotely execution program
            */
            Session session = jsch.getSession(USERNAME, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(PASSWORD);
            session.connect();

            //create the excution channel over the session
            ChannelExec channelExec = (ChannelExec)session.openChannel("exec");
            

            // Gets an InputStream for this channel. All data arriving in as messages from the remote side can be read from this stream.
            InputStream inOutput = channelExec.getInputStream();
            InputStream inErr=channelExec.getErrStream();
            

            // Set the command that you want to execute
            // In our case its the remote shell script
            channelExec.setCommand(message.getName());

            // Execute the command
            channelExec.connect();

            // Read the output from the input stream we set above
            BufferedReader reader = new BufferedReader(new InputStreamReader(inOutput));
            BufferedReader readerErr=new BufferedReader(new InputStreamReader(inErr));
            String line="";
            String err="";
            
            //Read each line from the buffered reader and add it to result list
            // You can also simple print the result here 
            while ((line = reader.readLine()) != null || (err=readerErr.readLine())!=null)
            {
           	 
           	 if(err!=null && err!="") {
           		 System.out.println(err);
           	 this.template.convertAndSend("/topic/greetings",new Greeting(err.toString()));
           	 }
           	 
           	 if(line!=null && line!="") {
           		System.out.println(line);
           		 this.template.convertAndSend("/topic/greetings", new Greeting(line.toString()));
           	 }
           	 //out.flush();
                result.add(line);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
          

            //retrieve the exit status of the remote command corresponding to this channel
            int exitStatus = channelExec.getExitStatus();

            //Safely disconnect channel and disconnect session. If not done then it may cause resource leak
            channelExec.disconnect();
            session.disconnect();

            if(exitStatus < 0){
               // System.out.println("Done, but exit status not set!");
            }
            else if(exitStatus > 0){
               // System.out.println("Done, but with error!");
            }
            else{
               // System.out.println("Done!");
            }

        }
        catch(Exception e)
        {
            System.err.println("Error: " + e);
        }
        //Thread.sleep(1000); // simulated delay
    	//this.template.convertAndSend("/topic/greetings", new Greeting("Hello: "+message.getName()));
        
        
    }

}
