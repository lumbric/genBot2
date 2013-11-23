package genBot2;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;






import serialRMI.SerialRMIException;
import serialRMI.SerialRMIInterface; 
public class QueueManager extends Thread {

	private CocktailQueue queue;
	private GenBotProtocol protocol;
	private SerialRMIInterface serial;
	
	private int cocktailSizeMilliliter;
	
	private CocktailWithName currentlyPouring;
	
	private enum Status {
		unknown,
		ready,
		waitingForCup,
		error,
		waitingForWaitingForCup, 
		waitingForReady,
		waitingForEnjoy
	}
	
	private Status status;

	public QueueManager(CocktailQueue queue, String server, String portName, int cocktailSizeMilliliter) throws MalformedURLException, RemoteException, NotBoundException, SerialRMIException {
		setDaemon(true);
		
		this.queue = queue;
		this.protocol = GenBotProtocol.getInstance();
		
		this.cocktailSizeMilliliter = cocktailSizeMilliliter;
		
		if (!(server.equals("") | portName.equals(""))) {
			this.serial = (SerialRMIInterface) Naming.lookup(server);
//			serial.connect(portName);
		} else {
			this.serial = null;
		}
		
		this.status = Status.unknown;
	}

	@Override
	public void run() {
		while (true) {
			try {
				//Thread.sleep(200);
				if (serial != null) {
					processSerialInput();
				}

				if(serialIsReady()) {
					processQueue();
					//Thread.sleep(200);
				}
				
				
			} catch (Exception | SerialRMIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
	
	public void processQueue() throws RemoteException, SerialRMIException {
		if (!queue.isEmpty()) {
			pourCocktail();
		}
	}
	
	private void processSerialInput() throws SerialRMIException {
		
		try {
			//String[] sA = {new String("READY")};
			String[] sA = serial.readLines();
			if(sA.length == 0)
				return;

			GenBotMessage[] message = protocol.read(sA);
			
			for (GenBotMessage me : message) {
				//System.out.println("GOT COMMANT " + me.raw);
				switch (me.command) {
				case "READY":
					// THIS IF IS ONLY NEEDED FOR TESTING
					currentlyPouring = null;
					if(status != Status.waitingForWaitingForCup)
						status = Status.ready;
					break;
				case "WAITING_FOR_CUP":
					status = Status.waitingForCup;
					System.out.println("Wort ma am Becher!");
				case "ENJOY":
					status = Status.waitingForReady;
				default:
					status = Status.unknown;
					break;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArduinoProtocolException e) {
			// TODO Auto-generated catch block
			System.out.print("ArduinoProtocolException: ");
			System.out.println(e.getMessage());
		}
	}

	public void pourCocktail() throws RemoteException, SerialRMIException {
		CocktailWithName toBePoured  = queue.getAndRemoveFirstCocktail();
		
		Cocktail pourCocktail = toBePoured.getCocktail();
		
		String codedPourCocktail = codePour(pourCocktail);
		//System.out.println("WRITING POUR");
		serial.writeLine(codedPourCocktail);
		currentlyPouring = toBePoured;
		
		pourCocktail.setQueued(false);
		pourCocktail.setPouring(true); // .setPouredTrue();
		status = Status.waitingForWaitingForCup;
	}
	
	private String codePour(Cocktail pourCocktail) {
		Ingredient[] ings = IngredientArray.getInstance().getAllIngredients();
		
		int[] milliLiters = new int[ings.length];
		for (int i = 0; i < milliLiters.length; i++) {
			milliLiters[ings[i].getArduinoOutputLine()] = (int) Math.round(pourCocktail.getAmount(ings[i]) * cocktailSizeMilliliter);
		}
		
		GenBotMessage m = new GenBotMessage("POUR", milliLiters);
		return m.raw;
	}
	
	public boolean serialIsReady() {
		if (status == Status.ready) {
			return true;
		} else {
			return false;
		}
	}

	public CocktailQueue getQueue() {
		return queue;
	}

}
