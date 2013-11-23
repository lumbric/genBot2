package genBot2;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.LinkedList;

import serialRMI.SerialRMIException;

public class TestAllFunctions {

	public static void main(String[] args) {
		Ingredient[] alleZutaten = IngredientArray.getInstance().getAllIngredients();
		Ingredient[] erlaubteZutaten1 = {alleZutaten[2], alleZutaten[3], alleZutaten[4]};
		Ingredient[] erlaubteZutaten2 = {alleZutaten[0], alleZutaten[3], alleZutaten[4], alleZutaten[5], alleZutaten[6]};
		Ingredient[] erlaubteZutaten3 = alleZutaten;

		CocktailQueue queue = new CocktailQueue();
		
		QueueManager queueManager;
		
		try {
			queueManager = new QueueManager(queue, "", "", 250);
			queueManager.start();
			RemoteOrderInterface remoteOrderImpl = new RemoteOrderImpl(queueManager);
			remoteOrderImpl.generateEvolutionStack("testStack1", erlaubteZutaten1, 15, 3, 2, "datenbank", false, "EfficientCocktail", "MutationAndIntermediateRecombination", 0.001, "eigenschaften");
			remoteOrderImpl.generateEvolutionStack("testStack2", erlaubteZutaten2, 15, 3, 2, "datenbank", false, "EfficientCocktail", "MutationAndIntermediateRecombination", 0.001, "eigenschaften");
			remoteOrderImpl.generateEvolutionStack("testStack3", erlaubteZutaten3, 15, 3, 2, "datenbank", false, "EfficientCocktail", "MutationAndIntermediateRecombination", 0.001, "eigenschaften");
			
			CocktailWithName[] testStack1 = remoteOrderImpl.getNamedPopulation("testStack1");
			CocktailWithName[] testStack2 = remoteOrderImpl.getNamedPopulation("testStack2");
			CocktailWithName[] testStack3 = remoteOrderImpl.getNamedPopulation("testStack3");
			
			for (int i = 0; i < testStack1.length; i++) {
				queue.addCocktail("testStack1", testStack1[i].getName());
			}
			
			LinkedList<CocktailWithName> queueContent = queue.getLinkedList();
			for (CocktailWithName actQueue : queueContent) {
				remoteOrderImpl.setCocktailFitness("testStack1", actQueue.getName(), 8);
				System.out.println(actQueue.toString());
			}
			
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SerialRMIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotEnoughRatedCocktailsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Now finished!");
	}
}