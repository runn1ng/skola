package eva2010.cv2;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.Vector;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.StandardPostSelector;
import org.jgap.impl.*;
import org.jgap.*;
import eva2010.StatsLogger;

public class Hromadky {

	/**
	 * @param args
	 */
	static int maxGen;
	static int popSize;
	static String logFilePrefix;
	static String resultsFile;
	static int repeats;
	static int K;
	static Vector<Double> weights;
	static String progFilePrefix;
	static String progFile;
	static String bestPrefix;

	public interface ReturnSelectorFromConf {
		public NaturalSelector which(Configuration wat) throws InvalidConfigurationException;
	}

	static ReturnSelectorFromConf firstSelector;
	static ReturnSelectorFromConf secondSelector;

//	static int 

	public static void main(String[] args) {

		Properties prop = new Properties();
		try {
			InputStream propIn = new FileInputStream("ga-cv2.properties");
			prop.load(propIn);
			propIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		maxGen = Integer.parseInt(prop.getProperty("max_generations", "20"));
		K = Integer.parseInt(prop.getProperty("bins", "10"));
		popSize = Integer.parseInt(prop.getProperty("population_size", "30"));
		progFilePrefix = prop.getProperty("prog_file_prefix", "prog.log");
		progFile = prop.getProperty("prog_file_results", "progress.log");
		bestPrefix = prop.getProperty("best_ind_prefix", "best");
		logFilePrefix = prop.getProperty("log_filename_prefix", "ga.log");
		resultsFile = prop.getProperty("results_filename", "ga.log");
		repeats = Integer.parseInt(prop.getProperty("repeats", "10"));
		
		String inputFile = prop.getProperty("input_file" );
	//	if (inputFile == null) {
			System.out.println(prop);
	//		System.exit(0);
	//	}


		int firstSelType = Integer.parseInt(prop.getProperty("first_selector_type"));
		String firstSelConf = prop.getProperty("first_more");
		if (inputFile == null || firstSelConf==null) {
			System.out.println(prop);
			System.exit(0);
		}

		if (firstSelType == 1) {
			firstSelector = new ReturnSelectorFromConf(){
				@Override
				public NaturalSelector which(Configuration wat)  throws InvalidConfigurationException{
					return new WeightedRouletteSelector(wat);
				}
			};
		} else if (firstSelType ==2) {
			final String[] conf = firstSelConf.split("-");
			final int tourSize = Integer.parseInt(conf[0]);
			final double prob = Double.parseDouble(conf[1]);
			firstSelector = new ReturnSelectorFromConf(){
				@Override
				public NaturalSelector which(Configuration wat)  throws InvalidConfigurationException{
					return new TournamentSelector(wat, tourSize, prob);
				}
			};
		} else if (firstSelType==3) {
			final double prob = 1/Integer.parseInt(firstSelConf);
			firstSelector = new ReturnSelectorFromConf(){
				@Override
				public NaturalSelector which(Configuration wat)  throws InvalidConfigurationException{
					return new BestChromosomesSelector(wat, prob);
				}
			};
		} else {
			System.out.println("NOOOOOO");
			System.exit(0);
		}

		int secondSelType = Integer.parseInt(prop.getProperty("second_selector_type"));
		String secondSelConf = prop.getProperty("second_more");

		if (secondSelType == 1) {
			secondSelector = new ReturnSelectorFromConf(){
				@Override
				public NaturalSelector which(Configuration wat) throws InvalidConfigurationException {
					return new WeightedRouletteSelector(wat);
				}
			};
		} else if (secondSelType ==2) {
			final String[] conf = secondSelConf.split("-");
			final int tourSize = Integer.parseInt(conf[0]);
			final double prob = Double.parseDouble(conf[1]);
			secondSelector = new ReturnSelectorFromConf(){
				@Override
				public NaturalSelector which(Configuration wat)  throws InvalidConfigurationException{
					return new TournamentSelector(wat, tourSize, prob);
				}
			};
		} else if (secondSelType==3) {
			final double prob = 1/Integer.parseInt(secondSelConf);
			secondSelector = new ReturnSelectorFromConf(){
				@Override
				public NaturalSelector which(Configuration wat)  throws InvalidConfigurationException{
					return new BestChromosomesSelector(wat, prob);
				}
			};
		} else if (secondSelType==4) {
			secondSelector = new ReturnSelectorFromConf(){
				@Override
				public NaturalSelector which(Configuration wat)  throws InvalidConfigurationException{
					return new StandardPostSelector(wat);
				}
			};
		}



		weights = new Vector<Double>();
		try {
			File f = new File("dalsi_inputy/cv2/"+inputFile);
			if (!f.exists()) {
				System.out.println("Neexistuje!");
				
				System.exit(0);
			} else {
				System.out.println("EXISTUJE!");
			}
			BufferedReader in = new BufferedReader(new FileReader(f));
			String line;
			while ((line = in.readLine()) != null) {
				weights.add(Double.parseDouble(line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		int sort = Integer.parseInt(prop.getProperty("sort"));
		if (sort == 0) {
			//nic
		} else if (sort == 1) {		
			java.util.Collections.sort(weights);
		} else {
			System.out.println("NOOOOOOO!");
			System.exit(0);
		}
				
		System.out.println("ALL OK");
		//System.exit(0);

		for (int i = 0; i < repeats; i++) {
			run(i);
		}

		processResults(logFilePrefix, resultsFile);
		processResults(progFilePrefix, progFile);

	}

	
	static void processResults(String logPrefix, String resultsName) {
		Vector<Vector<Double>> bestFitnesses = new Vector<Vector<Double>>();
		try {
			for (int i = 0; i < repeats; i++) {
				Vector<Double> column = new Vector<Double>();

				BufferedReader in = new BufferedReader(new FileReader(
						logPrefix + "." + i));
				String line = null;
				while ((line = in.readLine()) != null) {
					double best = Double.parseDouble(line.split(" ")[0]);
					column.add(best);
				}

				bestFitnesses.add(column);
			}

			FileWriter out = new FileWriter(resultsName);

			for (int j = 0; j < maxGen; j++) {
				double min = Double.MAX_VALUE;
				double max = -Double.MAX_VALUE;
				double sum = 0;
				for (int i = 0; i < repeats; i++) {
					double current = bestFitnesses.get(i).get(j);

					min = Math.min(current, min);
					max = Math.max(current, max);
					sum += current;
				}

				double avg = sum / repeats;

				out.write(min + " " + avg + " " + max
						+ System.getProperty("line.separator"));

			}
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void run(int number) {

		Configuration conf = new DefaultConfiguration();
		Configuration.reset();

		try {

			Gene[] sampleGenes = new Gene[weights.size()];

			for (int i = 0; i < weights.size(); i++) {
				sampleGenes[i] = new IntegerGene(conf, 0, K-1);
			}

			Chromosome sampleChromosome = new Chromosome(conf, sampleGenes);

			conf.setSampleChromosome(sampleChromosome);
			conf.setFitnessFunction(new HromadkyFitness(weights, K));
			conf.setPopulationSize(popSize);
			conf.addNaturalSelector(new TournamentSelector(conf, 50, 0.1), true);
			conf.removeNaturalSelectors(false);
			conf.addNaturalSelector(new StandardPostSelector(conf), false);

			Genotype pop = Genotype.randomInitialGenotype(conf);

			System.out.println("Generation -1: "
					+ pop.getFittestChromosome().toString());

			OutputStreamWriter out = new OutputStreamWriter(
						new FileOutputStream(logFilePrefix + "." + number));
			
			OutputStreamWriter progOut = new OutputStreamWriter(
					new FileOutputStream(progFilePrefix + "." + number));

			for (int i = 0; i < maxGen; i++) {
				pop.evolve();
				Double diff = (Double)pop.getFittestChromosome().getApplicationData();
				System.out.println("Generation " + i + ": " + diff);

				progOut.write(diff.toString() + System.getProperty("line.separator"));
				
				StatsLogger.log(pop, out);

			}
			
			OutputStreamWriter bestOut = new OutputStreamWriter(
					new FileOutputStream(bestPrefix + "." + number));
			
			IChromosome bestInd = pop.getFittestChromosome(); 
			
			for (int i = 0; i < bestInd.getGenes().length; i++) {
				bestOut.write(weights.get(i) + " " + bestInd.getGene(i).getAllele() + System.getProperty("line.separator"));
			}
			
			out.close();
			progOut.close();
			bestOut.close();
			
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
