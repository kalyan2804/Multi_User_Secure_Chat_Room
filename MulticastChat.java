import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
public class MulticastChat extends Panel
{
	TextArea receivedText;
	private GridBagConstraints c;
	private GridBagLayout gridBag;
	private Frame frame;
  	private Label label;
	private TextField sendText;
	private String hostname;
	private String username;
	public static final int MCAST_PORT=9013;
	public static final int DGRAM_BUF_LEN=512;
	public MulticastSocket socket;
	public DatagramPacket hi;
	public InetAddress group;	
	public static void main(String args[])
	{
		if(args.length!=2)
		{
			System.out.println("\n\n\t\tFormat is : java MulticastChat <user name> <group address>");
			return;
		}
		Frame f=new Frame(args[0]);
		MulticastChat chat=new MulticastChat(f,args[0],args[1]);
		f.add("Center",chat);
		f.setSize(350,200);
		f.show();
		chat.process();
	}
	public MulticastChat(Frame f,String user,String host)
	{
		frame=f;
		frame.addWindowListener(new WindowExitHandler());
		username=user;
		hostname=host;
		Insets insets=new Insets(10,20,5,10);
		gridBag=new GridBagLayout();
		setLayout(gridBag);
		c=new GridBagConstraints();
		c.insets=insets;
		c.gridx=0;
		c.gridy=0;
		label=new Label("Text to send:");
		gridBag.setConstraints(label,c);
		add(label);
		c.gridx=1;
		sendText=new TextField(20);
		sendText.addActionListener(new TextActionHandler());
		gridBag.setConstraints(sendText,c);
		add(sendText);
		c.gridx=0;
		c.gridy=1;
		label=new Label("Text Received:");
		gridBag.setConstraints(label,c);
		add(label);
		c.gridx=1;
		receivedText=new TextArea(3,20);
		gridBag.setConstraints(receivedText,c);
		add(receivedText);
	}	
	public void process()
	{
		try
		{
			group=null;
			group=InetAddress.getByName(hostname);
		}
		catch(UnknownHostException u)
		{
			u.printStackTrace();
		}
		try
		{
			socket=new MulticastSocket(MCAST_PORT);
			socket.joinGroup(group);
			new TestReceive(this).start();
		}
		catch(Exception z)
		{
			System.out.println("This is wrong, "+z);
		}
	}
	protected void finalize() throws Throwable
	{
		try
		{
			
			if(socket!=null)
				socket.close();
		}
		catch(Exception x)
		{
			System.out.println(x);
		}
		super.finalize();
	}
	class WindowExitHandler extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			Window w=e.getWindow();
			w.setVisible(false);
			w.dispose();
			System.exit(0);
		}
	}
	class TextActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			try
			{ 
				String msg=sendText.getText();
				//System.out.println("Sending message :"+msg);
				hi = new DatagramPacket(msg.getBytes(), msg.length(),group, MCAST_PORT);
			        socket.send(hi);
				sendText.setText("");
			}
			catch(Exception x)
			{
				x.printStackTrace();
			}
		}
	}
}
class TestReceive extends Thread
{
	public MulticastChat chat;
	TestReceive(MulticastChat chat)
	{
		this.chat=chat;
	}
	public synchronized void run()
	{
		try
		{
			while(true)
			{
				byte[] buf = new byte[chat.DGRAM_BUF_LEN];
				DatagramPacket recv = new DatagramPacket(buf, buf.length);
				chat.socket.receive(recv);
				byte[] data = recv.getData();
				//System.out.println("Receiving message :"+new String(buf));
				//String s=data.toString();
				chat.receivedText.setText(new String(buf));
			}
			
		}
		catch(Exception e1)
		{
			e1.printStackTrace();	
		}
	}
}