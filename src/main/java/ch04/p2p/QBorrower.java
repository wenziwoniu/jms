package ch04.p2p;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.springframework.jms.core.JmsTemplate;

public class QBorrower {
	
	private QueueConnection qConnect = null;
	private QueueSession qSession = null;
	private Queue responseQ = null;
	private Queue requestQ = null;
	
	public QBorrower (String queuecf, String requestQueue, String responseQueue) {
		
		try {
			
			Context ctx = new InitialContext();
			QueueConnectionFactory qFactory = (QueueConnectionFactory) ctx.lookup(queuecf);
			qConnect = qFactory.createQueueConnection();
			
			qSession = qConnect.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			
			requestQ = (Queue) ctx.lookup(requestQueue);
			responseQ = (Queue) ctx.lookup(responseQueue);
			
			qConnect.start();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	private void sendLoanRequest(double salary, double loanAmt) {
		
		try {
			MapMessage msg = qSession.createMapMessage();
			msg.setDouble("Salary", salary);
			msg.setDouble("LoanAmount", loanAmt);
			msg.setJMSReplyTo(responseQ);
			
			QueueSender qSender = qSession.createSender(requestQ);
			qSender.send(msg);
			
			System.out.println("借款消息已经发送");
//			Thread.sleep(5000);
			
			String fileter = "JMSCorrelationID = '" + msg.getJMSMessageID() + "'";
			QueueReceiver qReceiver = qSession.createReceiver(responseQ, fileter);
			TextMessage tmsg = (TextMessage) qReceiver.receive(30000);
			System.out.println("收到借款响应");
			if (tmsg == null) {
				System.out.println("QLender not responding");
			} else {
				System.out.println("Loan request was " + tmsg.getText());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void exit() {
		try {
			qConnect.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public static void main(String[] args) {

		String queueCF = null;
		String requestq = null;
		String responseq = null;
		if (args.length == 3) {
			queueCF = args[0];
			requestq = args[1];
			responseq = args[2];
		} else {
			System.out.println("Invalid arguments. Should be:");
			System.out.println("java QBorrower factory requestQueue responseQueue");
			System.exit(0);
		}
		
		QBorrower borrower = new QBorrower(queueCF, requestq, responseq);
		try {
			
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("QBorrower Application Started");
			System.out.println("Please enter to quit application");
			System.out.println("Enter:Salary, Loan_Amount");
			System.out.println("\ne.g 5000, 12000");
			while (true) {
				
				System.out.println("> ");
				String loanRequest = stdin.readLine();
				if (loanRequest == null || loanRequest.trim().length() <=0) {
					borrower.exit();
				}
				
				StringTokenizer st = new StringTokenizer(loanRequest, ",");
				double salary = Double.valueOf(st.nextToken().trim()).doubleValue();
				double loanAmt = Double.valueOf(st.nextToken().trim()).doubleValue();
				
				borrower.sendLoanRequest(salary, loanAmt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
