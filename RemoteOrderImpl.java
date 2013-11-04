package genBot2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Scanner;

public class RemoteOrderImpl implements RemoteOrderInterface {
	
	private EvolutionStackContainer evolutionStackController;
	private Scanner scanner;
	private PrintWriter out;

	public RemoteOrderImpl() {
		this.evolutionStackController = EvolutionStackContainer.getInstance();
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
	public void generateEvolutionStack(String evolutionStackName, CheckFitness fitnessCheck,
			Recombination recombination, boolean dbReset, String propPath)
			throws RemoteException, SQLException {
		//TODO fitnessCheck is now hardcoded - it would be better as a String argument
		//TODO fitnessCheck is now hardcoded - it would be better as a String argument
		CheckFitness wasZahlst = new EfficientCocktail();
		Recombination fortpflanzung = new MutationAndIntermediateRecombination(0.25, 0.005);

		evolutionStackController.addEvolutionAlgorithmManager(evolutionStackName, wasZahlst, fortpflanzung, dbReset, propPath);
	}

	@Override
	public void generateEvolutionStack(String evolutionStackName,
			Ingredient[] allowedIngredients, int populationSize,
			int truncation, int elitism, String dbDriverPath, boolean dbReset,
			CheckFitness fitnessCheck, Recombination recombination, double stdDeviation,
			String propPath) throws RemoteException, SQLException {
		evolutionStackController.addEvolutionAlgorithmManager(evolutionStackName, allowedIngredients, populationSize, truncation, elitism, dbDriverPath, dbReset, fitnessCheck, recombination, stdDeviation, propPath);
	}

	@Override
	public String[] listEvolutionStacks() throws RemoteException {
		return evolutionStackController.listEvolutionStacks();
	}

	@Override
	public boolean containsEvolutionStack(String evolutionStackName) throws RemoteException {
		return evolutionStackController.containsEvolutionStack(evolutionStackName);
	}

	@Override
	public String getProps(String evolutionStackName) throws RemoteException, FileNotFoundException {
		scanner = new Scanner(new File(evolutionStackController.getEvolutionAlgorithmManager(evolutionStackName).getPropFile()));
		return scanner.useDelimiter("\\Z").next();
	}

	@Override
	public void setProps(String evolutionStackName, String props)
			throws RemoteException, FileNotFoundException {
		out = new PrintWriter(evolutionStackController.getEvolutionAlgorithmManager(evolutionStackName).getPropFile());
		out.println(props);
	}
}