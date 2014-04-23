package diversity;

import jaligner.Sequence;
import jaligner.matrix.Matrix;
import jaligner.matrix.MatrixLoader;
import jaligner.matrix.MatrixLoaderException;

import java.util.ArrayList;
import java.util.Collections;

public class Diversity_entropybased {
	private ArrayList<Sequence> notChosenList;
	private ArrayList<Sequence> chosenList;
	private Sequence query;
	private Matrix matrix;
	private int topKNumber;
	private double diversityRate;
	private double similarityRate;
	private double max;
	private double maxScore;
	private char[] charlist = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
			'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'Y',
			'Z', 'X', '-', '#' };

	public enum type {
		bitwise, character, mix
	};

	private type type;

	private ArrayList<Double> resultlist = new ArrayList<Double>();

	public Diversity_entropybased(ArrayList<Sequence> db, Sequence query,
			int k, double similarityRate, double diversityRate)
			throws MatrixLoaderException {
		notChosenList = db;
		chosenList = new ArrayList<Sequence>();
		this.query = query;
		if (k >= db.size())
			topKNumber = db.size();
		else
			topKNumber = k;
		matrix = MatrixLoader.load("BLOSUM62");
		this.diversityRate = diversityRate;
		this.similarityRate = similarityRate;
		this.type = type.mix; 
		maxScore = calculateMaxScore();//for diversity similarity tradeoff
	}

	public ArrayList<Double> getResultlist() {
		return resultlist;
	}

	public void setResultlist(ArrayList<Double> resultlist) {
		this.resultlist = resultlist;
	}

	private double entropyBasedSimilarity(Sequence sequence,
			ArrayList<Sequence> chosenList2, double m) {
		calculateMaxScore(m, type);
		return entropy(sequence, chosenList2)  * diversityRate
				+ similarityRate * sequence.getScore() / maxScore;
	}

	private double entropy(Sequence sequence, ArrayList<Sequence> chosenList2) {
		//three types, in experiments mix is used. 
		if (type.equals(type.character))
			return entropyBased(chosenList2, sequence);
		else if (type.equals(type.bitwise))
			return entropyBasedBitwise(chosenList2, sequence);
		else 
			return (entropyBased(chosenList2, sequence)
					+ entropyBasedBitwise(chosenList2, sequence))/2.0;

	}

	//main method for choosing diversified set from original set.
	public ArrayList<Sequence> findTopKDiverseSet() {
		int numberOfResults = 1;
		chosenList.add(notChosenList.remove(0));// first assign (greedy)
		if (topKNumber == 1) {
			resultlist.add(0.0);
			return chosenList;
		}

		double tempDif;
		ArrayList<Triple> couplelist;
		do {
			couplelist = new ArrayList<Triple>();
			for (int i = 0; i < notChosenList.size(); i++) {
				tempDif = value(notChosenList.get(i), chosenList,
						numberOfResults);

				couplelist.add(new Triple(i, 0, tempDif));
			}

			Collections.sort(couplelist);//sort by diversity

			chosenList.add(notChosenList.remove(couplelist.get(
					couplelist.size() - 1).getIndexOfNotChosen()));
			
			numberOfResults++;
			System.out.println(couplelist.get(couplelist.size() - 1)
					.getResult() + " k");
			resultlist.add(couplelist.get(couplelist.size() - 1).getResult());
		} while (numberOfResults < topKNumber);
		for(int i=0;i<chosenList.size();i++)
			chosenList.get(i).setCount(i+1);
		return chosenList;
	}
	
		
	public double[] evaluate(ArrayList<Sequence> notChosenList) {
		double[] eva = new double[topKNumber];
		int numberOfResults = 1;
		ArrayList<Sequence> chosenList = new ArrayList<Sequence>();
		chosenList.add(notChosenList.get(0));// first assign
		double tempDif;

		while (numberOfResults < topKNumber) {

			tempDif = value(notChosenList.get(numberOfResults), chosenList,
					numberOfResults);

			chosenList.add(notChosenList.get(numberOfResults));

			eva[numberOfResults] = tempDif;

			numberOfResults++;
		}
		numberOfResults++;

		return eva;
	}

	private double entropyBasedBitwise(ArrayList<Sequence> seqlist, Sequence seq) {

		seqlist.add(seq);
		int i = 0;
		int j = seqlist.size() - 1;

		int seqlength = seqlist.get(0).length();
		int[][] counter = new int[seqlength][2];
		double intervalLength = j - i + 1;

		for (int k = 0; k < 2; k++) {
			for (int l = 0; l < seqlength; l++) {
				counter[l][k] = 0;
			}
		}

		for (int k = i; k < j + 1; k++) {
			for (int l = 0; l < seqlength; l++) {
				counter[l][seqlist.get(k).getBitsequence().getBit(l)]++;
			}
		}

		double entropy = 0;
		for (int l = 0; l < seqlength; l++) {
			for (int k = 0; k < 2; k++) {
				if (counter[l][k] != 0)

					entropy -= (counter[l][k] / intervalLength)
							* Math.log((counter[l][k] / intervalLength));
			}
		}
		seqlist.remove(seq);

		return entropy;
	}

	private double entropyBased(ArrayList<Sequence> seqlist, Sequence seq) {

		seqlist.add(seq);
		int i = 0;
		int j = seqlist.size() - 1;

		int seqlength = seqlist.get(0).length();// bu calısıyo muydu hatrlamıom
		int[][] counter = new int[seqlength][charlist.length];
		double intervalLength = j - i + 1;

		for (int k = 0; k < charlist.length; k++) {
			for (int l = 0; l < seqlength; l++) {
				counter[l][k] = 0;
			}
		}

		for (int k = i; k < j + 1; k++) {
			for (int l = 0; l < seqlength; l++) {
				counter[l][findIndex(seqlist.get(k).get(l))]++;
			}
		}

		double entropy = 0;
		for (int l = 0; l < seqlength; l++) {
			for (int k = 0; k < charlist.length; k++) {
				if (counter[l][k] != 0)
					entropy -= (counter[l][k] / intervalLength)
							* Math.log((counter[l][k] / intervalLength));
			}
		}
		seqlist.remove(seq);

		return entropy;
	}

	private int findIndex(char ch) {
		for (int i = 0; i < charlist.length; i++) {
			if (ch == charlist[i])
				return i;
		}
		return -1;

	}
	
	private int countOnes(int[] bits) {
		int sum = 0;
		for (int i = 0; i < bits.length; i++) {
			if (bits[i] == 1)
				sum++;
		}

		return sum;
	}
	
	private double value(Sequence notchosen, ArrayList<Sequence> chosen,
			double m) {
		double totallength=countOnes(notchosen.getBitsequence().getBitvector());
		for(Sequence s:chosen){
			totallength+=countOnes(s.getBitsequence().getBitvector());
		}		
		return entropyBasedSimilarity(notchosen, chosen, m) * diversityRate * chosen.size() / totallength
				+ similarityRate * notchosen.getScore() / maxScore;
	}

	private void calculateMaxScore(double m, type type) {
		m = (double) m;
		if (type == type.character)
			max = -query.getSequence().length()
					* Math.ceil(m / (double) charlist.length)
					* Math.log(Math.ceil(m / (double) charlist.length) / m);
		if (type == type.bitwise)
			max = -query.getSequence().length() * Math.ceil(m / 2)
					* Math.log(Math.ceil(m / 2.0) / (double) m);
		else
			max = (-query.getSequence().length()
					* Math.ceil(m / (double) charlist.length)
					* Math.log(Math.ceil(m / (double) charlist.length) / m) + -query
					.getSequence().length()
					* Math.ceil(m / (double) charlist.length)
					* Math.log(Math.ceil(m / (double) charlist.length) / m)) / 2.0;
	}

	private double calculateMaxScore() {
		double sum = 0;
		String queryStr = query.getSequence();
		for (int i = 0; i < query.length(); i++) {
			sum += matrix.getScore(queryStr.charAt(i), queryStr.charAt(i));
		}
		return sum;
	}

	public int getTopKNumber() {
		return topKNumber;
	}

	public void setTopKNumber(int topKNumber) {
		this.topKNumber = topKNumber;
	}

	public double getDiversityRate() {
		return diversityRate;
	}

	public void setDiversityRate(double diversityRate) {
		this.diversityRate = diversityRate;
	}

	public double getSimilarityRate() {
		return similarityRate;
	}

	public void setSimilarityRate(double similarityRate) {
		this.similarityRate = similarityRate;
	}

	public type getType() {
		return type;
	}

	public void setType(type type) {
		this.type = type;
	}
	
	

}
