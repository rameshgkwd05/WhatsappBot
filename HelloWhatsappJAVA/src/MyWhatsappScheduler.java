/*
 * A Demo JAVA project for sending scheduled Whatsapp messages using www.twilio.com
 * PreRequite: 
 * 	1] Working whatsapp account number
 * 	2] Twilio account and sandbox setup done. Signup using your Whatsapp number
 *  3] Twilio account SID and Authentication Token
 *  
 */

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.Message.Status;
import com.twilio.type.PhoneNumber;

import java.time.LocalTime;

import java.time.temporal.ChronoUnit;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/*
 * Helper class to send whatsapp message
 */
class MessageSender {
	// if your yourNumber = "+911234567890" then
	static PhoneNumber myNumber = new PhoneNumber("whatsapp:911234567890");
	static PhoneNumber twilioDev = new PhoneNumber("whatsapp:+14155238886");

	public static Status sendMessage(String msg) {
		Message message = Message.creator(myNumber, twilioDev, msg).create();

		System.out.println(message.getSid());
		return message.getStatus();
	}
}

/*
 * A Helper class where you can configure your task in run() method
 */
class Helper extends TimerTask {
	public static int i = 0;

	private String[] gQuotes = new String[2];

	public Helper() {
		gQuotes[0] = "You are what you believe in. You become that which you believe you can become!\n - Bhagavad Gita";
		gQuotes[1] = "Amongst thousands of persons, hardly one strives for perfection; and amongst those who have achieved perfection,"
				+ " hardly one knows me in truth.\n -Bhagavad Gita";
	}

	public void run() {

		Date dt = new Date();
		System.out.println(dt);
		System.out.println("Sending Message #" + ++i);

		String msg = gQuotes[i - 1] + "\n\n[ _" + dt + "_ ]";
		MessageSender.sendMessage(msg);

		// You can change this threshold if you want to send more messages
		if (i == 2) {
			synchronized (MyWhatsappScheduler.objectForSync) {
				MyWhatsappScheduler.objectForSync.notify();
			}
		}
	}
}


/*
 * Main driver class
 */
public class MyWhatsappScheduler {
	public static final String ACCOUNT_SID = "GET THIS FROM YOUR TWILIO PROFILE";
	public static final String AUTH_TOKEN = "AND THIS TOKEN TOO";

	static final Object objectForSync = new Object();

	public static void main(String[] args) {
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

		Timer timer = new Timer();
		TimerTask task = new Helper();

		// timer.schedule(task, 2000, 2000);
		int hour = 21;
		int minute = 57;
		int seconds = 30;
		String receiverName = "Friend";

		long delay = ChronoUnit.MILLIS.between(LocalTime.now(), LocalTime.of(hour, minute, seconds));

		timer.scheduleAtFixedRate(task, delay, 10000);

		String welcomeMsg = "Hi " + receiverName + ", \n\nHope you are had your dinner.\n"
				+ "You will receive 2 nice messages from " + hour + ":" + minute + ":" + seconds
				+ " and after 30 seconds" + "\n\n-Ramesh";
		System.out.println(welcomeMsg);
		
		MessageSender.sendMessage(welcomeMsg);
		
		synchronized (objectForSync) {
			// make the main thread wait
			try {
				objectForSync.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("waiting..");
			}

			// once timer has scheduled the task 4 times,
			// main thread resumes
			// and terminates the timer
			timer.cancel();

			// purge is used to remove all cancelled
			// tasks from the timer'stak queue
			System.out.println(timer.purge());
		}

		System.out.println("End Of messaging");

	}
}

