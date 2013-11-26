package genBot2;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class RemoteOrderImpl implements RemoteOrderInterface {
	
	private EvolutionStackContainer evolutionStackController;
	private QueueManager queueManager;
	
	public RemoteOrderImpl(QueueManager queueManager) {
		this.evolutionStackController = EvolutionStackContainer.getInstance();
		this.queueManager = queueManager;
	}

	@Override
	public CocktailWithName[] getNamedPopulation(String evolutionStackName) throws RemoteException {
		return evolutionStackController.getEvolutionAlgorithmManager(evolutionStackName).getGenManager().getNamedCocktailGeneration();
	}

	@Override
	public void setCocktailFitness(String evolutionStackName, String name, double fitnessInput) throws RemoteException, SQLException, NotEnoughRatedCocktailsException {
		EvolutionAlgorithmManager evoAlgMngr = evolutionStackController.getEvolutionAlgorithmManager(evolutionStackName);		

		evoAlgMngr.setFitness(name, fitnessInput);
		
		if (evoAlgMngr.getGenManager().getUnRatedNamedCocktailGeneration().length == 0) {
			evoAlgMngr.evolve();
		}
	}

	@Override
	public boolean canEvolve(String evolutionStackName) throws RemoteException {
		return evolutionStackController.getEvolutionAlgorithmManager(evolutionStackName).canEvolve();
	}

	@Override
	public void evolve(String evolutionStackName) throws RemoteException, SQLException, NotEnoughRatedCocktailsException {
		evolutionStackController.getEvolutionAlgorithmManager(evolutionStackName).evolve();
	}

	@Override
	public void generateEvolutionStack(String evolutionStackName, String fitnessCheckName,
			String recombinationName, boolean resetDbTable, String propPath, double stdDeviation)
			throws RemoteException, SQLException {
		CheckFitness fitnessCheck = new EfficientCocktail();
		if (!fitnessCheckName.equals("EfficientCocktail")) {
			fitnessCheck = null;
		}
		Recombination recombination = new MutationAndIntermediateRecombination(0.25, stdDeviation);
		if (recombinationName.equals("StandardMutation")) {
			recombination = new StandardMutation(stdDeviation);
		} else if (recombinationName.equals("IntermediateRecombination")) {
			recombination = new IntermediateRecombination(0.25);
		}
		// variableArea is hard coded... but it should be 0.25

		evolutionStackController.addEvolutionAlgorithmManager(evolutionStackName, fitnessCheck, recombination, resetDbTable, propPath);
	}

	@Override
	public void generateEvolutionStack(String evolutionStackName,
			Ingredient[] allowedIngredients, int populationSize,
			int truncation, int elitism, String dbDriverPath,
			boolean resetDbTable, String fitnessCheckName, String recombinationName, double stdDeviation,
			String propPath) throws RemoteException, SQLException {
		CheckFitness fitnessCheck = new EfficientCocktail();
		if (!fitnessCheckName.equals("EfficientCocktail")) {
			fitnessCheck = null;
		}
		Recombination recombination = new MutationAndIntermediateRecombination(0.25, stdDeviation);
		if (recombinationName.equals("StandardMutation")) {
			recombination = new StandardMutation(stdDeviation);
		} else if (recombinationName.equals("IntermediateRecombination")) {
			recombination = new IntermediateRecombination(0.25);
		}
		// variableArea is hard coded... but it should be 0.25
		
		evolutionStackController.addEvolutionAlgorithmManager(evolutionStackName, allowedIngredients, populationSize, truncation, elitism, dbDriverPath, resetDbTable, fitnessCheck, recombination, stdDeviation, propPath);
	}

	@Override
	public void generateEvolutionStack(String evolutionStackName,
			Ingredient[] allowedIngredients) throws RemoteException,
			SQLException {
		String[] possibleNames = listPossibleEvolutionStacks();
		boolean containsName = false;
		
		for (int i = 0; i < possibleNames.length; i++) {
			if (possibleNames[i].equals(evolutionStackName)) {
				containsName = true;
			}
		}
		
		if (!containsName) {
			throw new IllegalArgumentException(evolutionStackName + " is not a .properties file in the folder");
		}
		
		generateEvolutionStack(evolutionStackName, allowedIngredients, 15, 3, 2, "cocktailDataBase", false, "EfficientCocktail", "", 0.05, evolutionStackName + "props");
	}
	
	@Override
	public String[] listPossibleEvolutionStacks() throws RemoteException {
		String path = "evolutionStackSettings/"; // make sure this is the same as in EvolutionAlgorithmManager.java
		
		String files;
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		
		ArrayList<String> fileNames = new ArrayList<String>();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				files = listOfFiles[i].getName();
				
				if (files.endsWith(".properties") || files.endsWith(".PROPERTIES")) {
					fileNames.add(files);
				}
			}
		}
		
		return fileNames.toArray(new String[fileNames.size()]);
	}

	@Override
	public void loadEvolutionStack(String evolutionStackName)
			throws RemoteException, SQLException {
		CheckFitness fitnessCheck = new EfficientCocktail();
		Recombination recombination = new MutationAndIntermediateRecombination(0.25, 0.05);
		
		evolutionStackController.addEvolutionAlgorithmManager(evolutionStackName, fitnessCheck, recombination, false, evolutionStackName);
	}

	@Override
	public String[] listLoadedEvolutionStacks() throws RemoteException {
		return evolutionStackController.listEvolutionStacks();
	}

	@Override
	public boolean containsEvolutionStack(String evolutionStackName) throws RemoteException {
		return evolutionStackController.containsEvolutionStack(evolutionStackName);
	}

	@Override
	public Properties getProps(String evolutionStackName) throws RemoteException, FileNotFoundException {
		return evolutionStackController.getEvolutionAlgorithmManager(evolutionStackName).loadProps();
	}

	@Override
	public void setProps(String evolutionStackName, int populationSize,
			int truncation, int elitism, double stdDeviation,
			String dbDriverPath, String booleanAllowedIngredientsString)
			throws RemoteException {
		evolutionStackController.getEvolutionAlgorithmManager(evolutionStackName).storeProps(evolutionStackName, populationSize, truncation, elitism, stdDeviation, dbDriverPath, booleanAllowedIngredientsString);
	}

	@Override
	public void queueCocktail(String evolutionStackName, String cocktailName)
			throws RemoteException {
		//evolutionStackController.getEvolutionAlgorithmManager(evolutionStackName).queue(cocktailName);
		queueManager.getQueue().addCocktail(evolutionStackName, cocktailName);
	}

	@Override
	public void queueCocktail(CocktailWithName cocktail) throws RemoteException {
		queueManager.getQueue().addCocktail(cocktail);	
	}

	@Override
	public void deleteCocktailFromQueue(String cocktailName) throws RemoteException {
		queueManager.getQueue().deleteCocktail(cocktailName);
	}
	
	@Override
	public void reorderQueue(String cocktailNameList[]) throws RemoteException {
		queueManager.getQueue().reorder(cocktailNameList);
	}

	@Override
	public CocktailGenerationManager getOldGeneration(String evolutionStackName, int generationNumber) throws RemoteException, SQLException {
		return evolutionStackController.getEvolutionAlgorithmManager(evolutionStackName).getOldGeneration(generationNumber);
	}

	@Override
	public CocktailQueue getQueue() throws RemoteException {
		return queueManager.getQueue();
	}

	@Override
	public CocktailGenerationManager readGenerationManager(
			String evolutionStackName) throws RemoteException {
		return evolutionStackController.getEvolutionAlgorithmManager(evolutionStackName).getGenManager();
	}

	@Override
	public CocktailWithName getCurrentlyPouringCocktail() throws RemoteException {
		return queueManager.getCurrentlyPouringCocktail();
	}

	@Override
	public int getCocktailSize() throws RemoteException {
		return queueManager.getCocktailSizeMilliliter();
	}

	@Override
	public void setCocktailSize(int milliLiters) throws RemoteException {
		queueManager.setCocktailSizeMilliliter(milliLiters);
	}
	
}
