package collector;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Collector {

	public static final String Collector_Work_Status_High = "High";
	public static final String Collector_Work_Status_Medium = "Medium";
	public static final String Collector_Work_Status_Low = "Low";
	public static final String Collector_Off = "Off";

	private final int Collecting_High_Limit;
	private final int Collecting_Low_Limit;
	private final int Number_Of_Threads_Per_Processor = 10;
	
	private boolean active = true;
	private int id;
	private Date last_seen_TS;
	private int status_id;
	
	public Collector(int id)
	{
		Runtime runTime = Runtime.getRuntime();
		this.Collecting_High_Limit = runTime.availableProcessors()*Number_Of_Threads_Per_Processor;
		this.Collecting_Low_Limit = Collecting_High_Limit*(int)(3.0f/4.0f);
		
		System.out.println("Starting simulator-thread...");
		new Thread(new Collecting()).start();
	}
	
	private void sleep()
	{
		//sleep 10000 milliseconds
		System.out.println("Waiting 1000 milliseconds...");
		long sleepTime = 1000l;
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		new Settings();
		AliveMessenger.getInstance();
		//new Collector();
	}
	
	public ArrayList<CollectingRequest> getCollectingRequests(int limit)
	{
		ArrayList<CollectingRequest> crawlerRequests = new ArrayList<CollectingRequest>();
		
		Connection connection = Settings.getDBC();
		
		try
		{
			String query = "SELECT ";
			PreparedStatement statement = connection.prepareStatement(query);
			ResultSet set = statement.executeQuery();
			
			while(set.next())
			{
				int id = set.getInt(1);
				int type_id = set.getInt(2);
				int longitude = set.getInt(3);
				int latitude = set.getInt(4);
				java.util.Date time_from = new Date(set.getLong(5));
				java.util.Date time_to = new Date(set.getLong(6));
				
				crawlerRequests.add(new CollectingRequest(id, type_id, longitude, latitude, time_from, time_to));
			}
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
		}
		
		return crawlerRequests;
	}
	
	private void validateCollectorStatus(int collectingId)
	{
		System.out.println("Validating status...");
		
		ThreadGroup threadRoot = Thread.currentThread().getThreadGroup();
		
		int activeThreadCount = threadRoot.activeCount();
		
		if(activeThreadCount > this.Collecting_High_Limit)
		{
			System.out.print("The workload is too high, waiting with processing new collection tasks");
		}
		else if(activeThreadCount <= this.Collecting_High_Limit && activeThreadCount > this.Collecting_Low_Limit)
		{
			System.out.println("The workload is average, will process new tasks");
			//this.updateSimulatorStatus(Collector.Collector_Work_Status_Medium,simulatorId);
		}
		else
		{
			System.out.println("The workload is low, will process new tasks");

		}
	}
	
	private class Collecting implements Runnable
	{
		public void run()
		{		
			int collecting_id = Settings.getSimulatorID();
			System.out.println("My collector-id is :"+collecting_id);
			
			while(active)
			{				
				validateCollectorStatus(collecting_id);
				sleep();
				collectData();
			}
			
			System.out.println("Turning off simulator...");
			
		}
		
		public void collectData()
		{
			System.out.println("Number of collecting threads running: "+Math.max(Thread.activeCount()-2,0));
			int limit = Math.max(0,Collecting_High_Limit - (Thread.activeCount() - 2));
			
			if(limit > 0)
			{	
				System.out.println("Can receive a workload of "+limit+" requests from database");
				System.out.println("Downloads a maximum of "+limit+" requests from database...");
				ArrayList<CollectingRequest> requests = getCollectingRequests(limit);
				
				if(requests.size() > 0)
				{
					System.out.println("Number of simulation requests recieved from database: "+requests.size());
					System.out.println("Starting "+requests.size()+" new simulation threads..");
					ExecutorService service = Executors.newFixedThreadPool(requests.size());
					
					for(int i = 0; i<requests.size(); i++)
					{
						CollectingRequest simulation = requests.get(i);
						service.execute(simulation);
					}
					service.shutdown();	
				}
			}
			else
			{
				System.out.println("Workload too high.. Cancelling downloading of new requests");
			}
		}
	}
}
