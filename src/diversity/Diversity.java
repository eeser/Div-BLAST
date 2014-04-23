package diversity;

import jaligner.Sequence;
import jaligner.matrix.Matrix;
import jaligner.matrix.MatrixLoader;
import jaligner.matrix.MatrixLoaderException;

import java.util.ArrayList;
import java.util.Collections;

public class Diversity {
	private ArrayList<Sequence> notChosenList;
	private ArrayList<Sequence> chosenList;
	private Sequence query;
	private Matrix matrix;
	private int topKNumber;
	private double diversityRate;
	private double similarityRate;
	private double max;
	private double min;
	private double mincover;
	private double maxcover;
	
	private ArrayList<Double> resultlist = new ArrayList<Double>();
	private char[] charlist = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
			'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'Y',
			'Z', 'X', '-', '#' };

	//different versions are available but for paper average is chosen.
	public enum type {
		rated, minimum, average, maximum
	};

	//that was used in evaluating this method in terms of entropic approach
	public enum entropytype {
		bitwise, character, mix
	};

	private type type;

	private double maxScore;
	private double minScore;

	public Diversity(ArrayList<Sequence> db, Sequence query, int k,
			double similarityRate, double diversityRate)
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
		calculateMaxScoreCover();
		
		this.type = type.average;// type;
		findmaxmin(db);

	}

	public double[] evaluateWithEntropy(ArrayList<Sequence> notChosenList,
			entropytype entType) {
		double[] eva = new double[topKNumber];
		int numberOfResults = 1;
		ArrayList<Sequence> chosenList = new ArrayList<Sequence>();
		chosenList.add(notChosenList.get(0));// first assign
		double tempDif;

		while (numberOfResults < topKNumber) {

			tempDif = entropyvalue(notChosenList.get(numberOfResults),
					chosenList, numberOfResults, entType);

			chosenList.add(notChosenList.get(numberOfResults));

			eva[numberOfResults] = tempDif;

			numberOfResults++;
		}
		numberOfResults++;

		return eva;
	}

	private double entropyvalue(Sequence notchosen, ArrayList<Sequence> chosen,
			double m, entropytype enttype) {
		return entropyBasedSimilarity(notchosen, chosen, m, enttype)
				* diversityRate + similarityRate * notchosen.getScore()
				/ maxScore;
	}

	private double entropyBasedSimilarity(Sequence sequence,
			ArrayList<Sequence> chosenList2, double m, entropytype enttype) {
		calculateMaxScoreEntropy(m, enttype);
		return entropy(sequence, chosenList2, enttype) / max * diversityRate
				+ similarityRate * sequence.getScore() / maxScore;
	}

	private double entropy(Sequence sequence, ArrayList<Sequence> chosenList2,
			entropytype enttype) {
		if (enttype.equals(entropytype.character))
			return entropyBased(chosenList2, sequence);
		else if (enttype.equals(entropytype.bitwise))
			return entropyBasedBitwise(chosenList2, sequence);
		else
			
			return (entropyBased(chosenList2, sequence) + entropyBasedBitwise(
					chosenList2, sequence)) / 2.0;

	}

	private void calculateMaxScoreEntropy(double m, entropytype type) {
		m = (double) m;
		if (type == entropytype.character)
			max = -query.getSequence().length()
					* Math.ceil(m / (double) charlist.length)
					* Math.log(Math.ceil(m / (double) charlist.length) / m);
		if (type == entropytype.bitwise)
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

	private double entropyBased(ArrayList<Sequence> seqlist, Sequence seq) {

		seqlist.add(seq);
		int i = 0;
		int j = seqlist.size() - 1;

		int seqlength = seqlist.get(0).length();
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

	//main method for choosing diversified set from original set.
	public ArrayList<Sequence> findTopKDiverseSet() {
		int numberOfResults = 1;
		chosenList.add(notChosenList.remove(0));// first assign
		if (topKNumber == 1)
			return chosenList;
		double tempDif;
		ArrayList<Triple> temptripleList;
		ArrayList<Triple> couplelist;
		Triple tempTriple;
		double av = 0;
		
		do {
			couplelist = new ArrayList<Triple>();
			for (int i = 0; i < notChosenList.size(); i++) {
				temptripleList = new ArrayList<Triple>();
				for (int j = 0; j < chosenList.size(); j++) {
					tempDif = value(notChosenList.get(i), chosenList.get(j));
					System.out.print("  val  "+tempDif);
					tempTriple = new Triple(i, j, tempDif);
					temptripleList.add(tempTriple);
					 System.out.println(notChosenList.get(i).getId());
				}
				//
				if (type.equals(type.average)) {
					av = 0;

					for (Triple t : temptripleList) {
						av += t.getResult();
					}

					av = av / temptripleList.size();
					
					couplelist.add(new Triple(i, 0, av));
				}
				
				else if (type.equals(type.rated)) {
					av = 0;
					double beta = 2 / (double) (chosenList.size() + 1);

					for (int j = 0; j < temptripleList.size(); j++) {
						av += temptripleList.get(j).getResult() * beta
								* (j + 1);
					}
					av = av / temptripleList.size();
					couplelist.add(new Triple(i, 0, av));
				}

				else if (type.equals(type.minimum)) {
					Collections.sort(temptripleList);// min
					couplelist.add(temptripleList.get(0));// min

				}
				else {
					Collections.sort(temptripleList);// min
					couplelist
							.add(temptripleList.get(temptripleList.size() - 1));// max

				}

			}

			Collections.sort(couplelist);

			chosenList.add(notChosenList.remove(couplelist.get(
					couplelist.size() - 1).getIndexOfNotChosen()));
			
			resultlist.add(couplelist.get(couplelist.size() - 1).getResult());
			numberOfResults++;

		} while (numberOfResults < topKNumber);
		for (int i = 0; i < chosenList.size(); i++)
			chosenList.get(i).setCount(i + 1);
		return chosenList;
	}

	private double value(Sequence notchosen, Sequence chosen) {
		return normalizedDifference(notchosen, chosen) * diversityRate
				+ similarityRate * ((double)notchosen.getCoveragePercent())/100.0;
				
	}

	private void calculateMaxScoreCover() {
		double sum = 0;
		String queryStr = query.getSequence();
		for (int i = 0; i < query.length(); i++) {
			sum += matrix.getScore(queryStr.charAt(i), queryStr.charAt(i));
		}
		maxScore= sum;
		double maxc=Integer.MIN_VALUE;
		double tempc;
		
		for(int i=1;i<notChosenList.size();i++){
			tempc=((double)notChosenList.get(i).getCoveragePercent())/100.0;
			if(tempc>maxc)
				maxc=tempc;
		}
		
		maxcover=maxc;
		
	}

	private double difference(Sequence notchosen, Sequence chosen) {
		int[] bv1 = notchosen.getBitsequence().getBitvector();

		int[] bv2 = chosen.getBitsequence().getBitvector();

		int[] result = new int[bv1.length];
		int[] union=new int[bv1.length];
		
		for (int i = 0; i < bv1.length; i++)
			union[i] = bv1[i] | bv2[i];
		
		for (int i = 0; i < bv1.length; i++)
			result[i] = bv1[i] ^ bv2[i];
		
		return (countOnes(result) / ((double)countOnes(union)));
	}

	//not used in divblast
	public double[] evaluate(ArrayList<Sequence> notChosenList) {// aynı query
																	// olmalı
		double[] eva = new double[topKNumber];
		int numberOfResults = 1;
		ArrayList<Sequence> chosenList = new ArrayList<Sequence>();
		chosenList.add(notChosenList.get(0));// ilk assign

		double tempDif;// algoritmaya bakarak gozden geciir
		ArrayList<Triple> temptripleList;
		Triple tempTriple;
		double av = 0;

		while (numberOfResults < topKNumber) {

			temptripleList = new ArrayList<Triple>();

			for (int j = 0; j < chosenList.size(); j++) {

				tempDif = value(notChosenList.get(numberOfResults),
						chosenList.get(j));
				tempTriple = new Triple(numberOfResults, j, tempDif);
				temptripleList.add(tempTriple);
			}
			if (type.equals(type.average)) {
				av = 0;

				for (Triple t : temptripleList) {
					av += t.getResult();
				}
				av = av / temptripleList.size();
			}
		
			else if (type.equals(type.rated)) {
				av = 0;
				double beta = 2 / (double) (chosenList.size() + 1);

				for (int j = 0; j < temptripleList.size(); j++) {
					av += temptripleList.get(j).getResult() * beta * (j + 1);
				}
				av = av / temptripleList.size();
			}

			else {
				Collections.sort(temptripleList);// min
				av = temptripleList.get(0).getResult();// min

			}
			
			eva[numberOfResults] = av;
			chosenList.add(notChosenList.get(temptripleList.get(0)
					.getIndexOfNotChosen()));
			numberOfResults++;
		}
		numberOfResults++;

		return eva;
	}

	public ArrayList<Double> evaluateInOrder(ArrayList<Double> eva) {
		double[] eval = new double[eva.size()];
		for (int i = 0; i < eval.length; i++)
			eval[i] = eva.get(i);
		double sum;
		for (int i = 0; i < eva.size(); i++) {
			sum = 0;
			for (int j = 0; j < i + 1; j++) {
				sum += eval[j];
			}
			eva.set(i, sum / (i + 1));
		}
		return eva;
	}

	public double[] evaluateInOrder(double[] eva) {
		double[] evatemp = new double[eva.length];
		for (int i = 0; i < eva.length; i++)
			evatemp[i] = eva[i];

		double sum;
		for (int i = 0; i < eva.length; i++) {
			sum = 0;
			for (int j = 0; j < i + 1; j++) {
				sum += evatemp[j];
			}
			eva[i] = sum / (i + 1);
		}
		return eva;
	}

	private int countOnes(int[] bits) {
		int sum = 0;
		for (int i = 0; i < bits.length; i++) {
			if (bits[i] == 1)
				sum++;
			// else
			// sum--;
		}

		return sum;
	}

	private double normalizedDifference(Sequence notchosen, Sequence chosen) {
		return normalize(difference(notchosen, chosen));
	}

	private void findmaxmin(ArrayList<Sequence> seqlist) {
		if (seqlist.size() < 2)
			return;
		
		double tempmax = Integer.MIN_VALUE;
		double tempmin = Integer.MAX_VALUE;
		double tempminc = Integer.MAX_VALUE;
		double tempminscore = Integer.MAX_VALUE;
		double temp,temps,tempc;
		for (int i = 0; i < seqlist.size(); i++) {
			
			tempc=seqlist.get(i).getCoveragePercent();
			temps=seqlist.get(i).getScore();
			temp = difference(query, seqlist.get(i));
			if(temps<tempminscore){
				tempminscore=temps;
			}
			if(tempc<tempminc){
				tempminc=tempc;
			}
			if (temp > tempmax) {
				tempmax = temp;
				
			}
			if (temp < tempmin)
				tempmin = temp;
			// }
		}
		max = tempmax;
		min = tempmin;
		minScore=tempminscore;
		mincover=tempminc/100.0;
	}

	private double normalize(double difference) {
		
//		return (difference - min) / (max - min);
		return difference;
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

	public ArrayList<Double> getResultlist() {
		return resultlist;
	}

	public void setResultlist(ArrayList<Double> resultlist) {
		this.resultlist = resultlist;
	}

}
