package genBot2;

public interface Recombination {
/*
 * performs a recombination (like a crossover)
 * @param population the old population on which the recombination should be performed
 * @param newPopulationSize the resulting cocktailpopulation's size
 */
	public CocktailGeneration recombine(CocktailGeneration population, int newPopulationSize, boolean[] booleanAllowedIngredients) throws FitnessNotSetException, MaxAttemptsToMeetPriceConstraintException;
	
	public double getMutationStdDeviation();
	
	public void setMutationStdDeviation(double stdDeviation);
	
	public double getMaxPricePerLiter();
	
	public void setMaxPricePerLiter(double maxPricePerLiter);
}
