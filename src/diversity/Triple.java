package diversity;

public class Triple implements Comparable<Triple>{
	private int indexOfNotChosen;
	private int indexOfChosen;
	private double result;
	public int getIndexOfNotChosen() {
		return indexOfNotChosen;
	}
	
	public Triple(int indexOfNotChosen, int indexOfChosen, double result) {
		super();
		this.indexOfNotChosen = indexOfNotChosen;
		this.indexOfChosen = indexOfChosen;
		this.result = result;
	}

	public void setIndexOfNotChosen(int indexOfNotChosen) {
		this.indexOfNotChosen = indexOfNotChosen;
	}
	public int getIndexOfChosen() {
		return indexOfChosen;
	}
	public void setIndexOfChosen(int indexOfChosen) {
		this.indexOfChosen = indexOfChosen;
	}
	public double getResult() {
		return result;
	}
	public void setResult(double result) {
		this.result = result;
	}

	@Override
	public int compareTo(Triple o) {
		if(result>o.result)
			return 1;
		else if(result==o.result)
			return 0;
		else
			return -1;
	}
	

}
