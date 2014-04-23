/*
 * $Id: Sequence.java,v 1.12 2005/04/14 14:44:45 ahmed Exp $
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jaligner;

import java.io.Serializable;
import java.util.ArrayList;

import diversity.BitVector;

/**
 * A basic (nucleic or protein) sequence. It's a wrapper to
 * {@link java.lang.String}.
 * 
 * @author Ahmed Moustafa (ahmed@users.sf.net)
 * added some qualifications for diversity
 */

public class Sequence implements Serializable {
	
	private String scoreString = "";
	private String positivesString;
	private String identitiesString;
	private String expectString;
	private String result;
	private String tostr;
	private BitVector bitsequence;
	private int length;
	private int coveragePercent = 0;
	private int count;

	public BitVector getBitsequence() {//bit version of sequence
		return bitsequence;
	}
	

	public ArrayList<Integer> getGaplistonQuery() {
		return gaplistonQuery;
	}

	public void setGaplistonQuery(int[] gaplistonQuery) {
		for (int i = 0; i < gaplistonQuery.length; i++)
			this.gaplistonQuery.add(gaplistonQuery[i]);
	}

	private ArrayList<Integer> gaplistonQuery = new ArrayList<Integer>();
	

	private static final long serialVersionUID = 3256721801357898297L;

	/**
	 * Sequence type nucleic.
	 */
	public static final int NUCLEIC = 0;

	/**
	 * Sequence type protein.
	 */
	public static final int PROTEIN = 1;

	/**
	 * Sequence
	 */
	private String sequence;

	/**
	 * Sequence id.
	 */
	private String id = null;

	/**
	 * Sequence description.
	 */
	private String description = null;

	/**
	 * Sequence type.
	 */
	private int type = PROTEIN;

	//blast query properties
	private double evalue;
	private int identities;
	private double positives;
	private double score;
	private double scoreRate;

	/**
	 * Constructor
	 */
	public Sequence() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param sequence
	 */
	public Sequence(String sequence) {
		super();
		setSequence(sequence);
	}

	/**
	 * Constructor
	 * 
	 * @param sequence
	 * @param id
	 * @param description
	 * @param type
	 */
	public Sequence(String sequence, String id, String description, int type) {
	
		this.id = id;
		this.description = description;
		this.type = type;
		setSequence(sequence);
	}

	public char get(int i) {
		return sequence.charAt(i);
	}

	/**
	 * Returns the sequence string
	 * 
	 * @return Returns the sequence
	 */
	public String getSequence() {
		return sequence;
	}

	/**
	 * Sets the sequence string
	 * 
	 * @param sequence
	 *            The sequence to set
	 */
	public void setSequence(String sequence) {
		this.sequence = sequence;
		setBitSequence(sequence);
	}

	/**
	 * Returns the sequence id
	 * 
	 * @return Returns the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the sequence id
	 * 
	 * @param id
	 *            The id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * Returns the sequence description
	 * 
	 * @return Returns the description
	 */
	public String getDescription() {
		return description;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * Sets the sequence description
	 * 
	 * @param description
	 *            The description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the sequence type (nucleic or protein)
	 * 
	 * @return Returns the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the sequence type (nucleic or protein)
	 * 
	 * @param type
	 *            The type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Returns the length of the sequence
	 * 
	 * @return sequence length
	 */
	public int length() {
		return this.sequence.length();
	}

	private void setBitSequence(String sequence) {
		int length = sequence.length();
		bitsequence = new BitVector(length, id);
		for (int i = 0; i < length; i++) {
			if ((sequence.charAt(i) == '#'))
				bitsequence.setBit(i, 0);
			else {
				bitsequence.setBit(i, 1);
				coveragePercent++;
			}

		}
		coveragePercent = (int) ((((double) coveragePercent) / length) * 100);

	}

	public String getScoreString() {
		return scoreString;
	}

	public void setScoreString(String scoreString) {
		this.scoreString = scoreString;
		score = Double.parseDouble(scoreString.substring(
				scoreString.indexOf('(') + 1, scoreString.indexOf(')')));
	}


	public double getEvalue() {
		return evalue;
	}

	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getPositivesString() {
		return positivesString;
	}

	public int getCoveragePercent() {
		return coveragePercent;
	}

	public void setCoveragePercent(int coveragePercent) {
		this.coveragePercent = coveragePercent;
	}

	public void setPositivesString(String positivesString) {
		this.positivesString = positivesString;
	}

	public String getIdentitiesString() {
		return identitiesString;
	}

	public void setIdentitiesString(String identitiesString) {
		this.identitiesString = identitiesString;
		identities = Integer.parseInt(identitiesString.substring(
				identitiesString.indexOf('(') + 1,
				identitiesString.indexOf('%')));
	}

	public String getExpectString() {
		return expectString;
	}

	public void setExpectString(String expectString) {
		this.expectString = expectString;
		// System.out.println(expectString+"d");
		if (expectString.startsWith("e"))
			evalue = Double.parseDouble("1" + expectString);
		else
			evalue = Double.parseDouble(expectString);
	}

	public int getIdentities() {
		return identities;
	}

	public void setIdentities(int identities) {
		this.identities = identities;
	}

	/**
	 * Returns a subsequence
	 * 
	 * @param index
	 *            start index
	 * @param length
	 *            length of subsequence
	 * @return subsequence
	 */
	public String subsequence(int index, int length) {
		// System.out.println(index+" other "+length);
		return this.sequence.substring(index, index + length);
	}

	/**
	 * Returns the acid at specific location in the sequence
	 * 
	 * @param index
	 * @return acid at index
	 */
	public char acidAt(int index) {
		return this.sequence.charAt(index);
	}

	/**
	 * Returns the sequence as an array of characters.
	 * 
	 * @return array of chars.
	 */
	public char[] toArray() {
		return this.sequence.toCharArray();
	}

	public int getLength() {
		return sequence.length();
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getTostr() {
		return tostr;
	}

	public void setTostr(String tostr) {
		this.tostr = tostr;
	}

}