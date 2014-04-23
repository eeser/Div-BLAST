package diversity;

public class BitVector
{
	private int[] bitsequence;
	private int length;
	private String ID;
	
	public BitVector(int length,String id) {
		super();
		this.length=length;
		bitsequence=new int[length];

		ID=id;
	}
	
	public void increaseBit(int i){
		bitsequence[i]=1;
	}
	
	public void setBit(int index,int value){
		bitsequence[index]=value;
	}
	
	public int getBit(int i){
		return bitsequence[i];
	}

	public int[] getBitvector() {
		return bitsequence;
	}

	public void setBitvector(int[] bitvector) {
		this.bitsequence = bitvector;
	}
	

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
	
	@Override
	public String toString(){
		String s="";
		for(int i=0;i<length;i++){
			s+=bitsequence[i];
		}
		return s;
	}
	

}
