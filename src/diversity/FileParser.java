package diversity;

import jaligner.Sequence;
import jaligner.util.SequenceParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

public class FileParser {
	private ArrayList<Sequence> db;
	private Sequence query;
	int seqLength;
	String tempSeq;
	private String id;
	private ArrayList<Sequence> list;
	private String queryId;
	private String desc;
	private String moleculeType;
	private String length;
	private String dbname;
	private String program;
	private String version;

	// a class for parsing blast web service outputs.
	public FileParser(String out) throws IOException, SequenceParserException,
			SAXException, DocumentException {
		parse(out + ".out.txt", out + ".sequence.txt");
		parseXml(out + ".xml.xml");

	}

	private void parse(String dbfile, String queryfile) throws IOException,
			SequenceParserException {

		query = parseFasta(new File(queryfile));
		tempSeq = "";
		for (int i = 0; i < query.length(); i++) {

			tempSeq += '#';
		}
		db = assignDB(dbfile, query);

	}

	public void parseXml(String out) throws IOException, SAXException,
			DocumentException {
		SAXReader reader = new SAXReader();
		Document doc = reader.read(out);
		Element tmp = (Element) doc.selectSingleNode("//*[name() = 'program']");
		program = (String) tmp.attribute(0).getData();
		version = (String) tmp.attribute(1).getData();
		tmp = (Element) doc.selectSingleNode("//*[name() = 'sequence']");
		desc = (String) tmp.attribute(1).getData();
		moleculeType = (String) tmp.attribute(2).getData();
		length = (String) tmp.attribute(3).getData();
		tmp = (Element) doc.selectSingleNode("//*[name() = 'database']");
		dbname = (String) tmp.attribute(1).getData();
		List<Node> nodes = doc.selectNodes("//*[name() = 'hit']");
		String temp = "";
		int i = 0;
		for (Node n : nodes) {
			temp = (String) ((Element) n).attribute("description").getData();
			db.get(i).setDescription(temp);
			i++;
		}

	}

	public ArrayList<Sequence> assignDB(String fileName, Sequence sequence)
			throws IOException {
		int count = 1;
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(fileName))));
		String seq = "", fasta = "";
		ArrayList<Sequence> seqList = new ArrayList<Sequence>();
		String line, sequenceName, sequenceDescription = "", tostr = ">";
		do {
			line = reader.readLine();
			if (line == null)
				return seqList;
		} while (!line.startsWith(">"));

		while (line != null && line.startsWith(">")) {
			line = line.substring(1).trim();
			int index = 0;
			for (int i = 0; i < line.length() && line.charAt(i) != ' '
					&& line.charAt(i) != '\t'; i++, index++) {
				// Skip white spaces
			}

			sequenceName = line.substring(0, index);
			StringTokenizer stringTokenizer = new StringTokenizer(sequenceName,
					"|");
			while (stringTokenizer.hasMoreTokens()) {
				sequenceName = stringTokenizer.nextToken();
			}
			if (index + 1 > line.length())
				sequenceDescription = "";
			else {
				tostr += line + "\n";
				sequenceDescription += line.substring(index + 1);
				line = reader.readLine();
				while (!line.trim().startsWith("Score")) {
					tostr += line + "\n";
					sequenceDescription += "\n" + line;
					line = reader.readLine();

				}
			}

			while (line != null
					&& ((!line.startsWith(">")) && (!line.trim().startsWith(
							"Database")))) {
				tostr += line + "\n";
				fasta += line + " ";
				line = reader.readLine();
			}

			Sequence s = assignSequence(fasta);
			s.setDescription(sequenceDescription);
			s.setTostr(tostr);
			s.setCount(count);
			count++;
			int index1 = sequenceDescription.indexOf("Length") + 9;

			sequenceDescription = "";
			tostr = ">";
			s.setId(sequenceName);
			seqList.add(s);

			fasta = "";

		}

		return seqList;
	}

	private Sequence assignSequence(String fastaString) {
		int first = 0, last = 0;
		String partOfQuery;
		Sequence sequence = new Sequence();
		sequence.setResult(fastaString);

		StringBuffer sbuffer = new StringBuffer(tempSeq);
		int[] gaplist = null;
		boolean gapFlag = false;
		StringTokenizer tokenizer = new StringTokenizer(fastaString, " ");

		String token, score = "", expect, identities = "", positives = "", seq = "";
		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			if (token.equals("Score")) {
				tokenizer.nextToken();// =
				if (!sequence.getScoreString().equals(""))
					break;
				score += tokenizer.nextToken() + " " + tokenizer.nextToken()
						+ " " + tokenizer.nextToken();
				sequence.setScoreString(score);
			} else if (token.equals("Expect")) {
				tokenizer.nextToken();// =
				expect = tokenizer.nextToken();
				sequence.setExpectString(expect);
			} else if (token.equals("Identities")) {
				tokenizer.nextToken();// =
				identities += tokenizer.nextToken() + " "
						+ tokenizer.nextToken();
				sequence.setIdentitiesString(identities);
			}

			else if (token.equals("Positives")) {
				tokenizer.nextToken();// =
				positives += tokenizer.nextToken() + " "
						+ tokenizer.nextToken();
				sequence.setPositivesString(positives);
			} else if (token.startsWith("Query")) {
				first = Integer.parseInt(tokenizer.nextToken());
				partOfQuery = tokenizer.nextToken();
				if (partOfQuery.contains("-")) {
					gapFlag = true;
					char[] arr = partOfQuery.toCharArray();
					int count = 0;
					for (int i = 0; i < partOfQuery.length(); i++) {
						if (arr[i] == '-') {
							count++;
						}

					}
					gaplist = new int[count];
					int c = 0;
					for (int i = 0; i < partOfQuery.length(); i++) {

						if (arr[i] == '-') {
							gaplist[c] = i;
							c++;
						}
					}

					sequence.setGaplistonQuery(gaplist);

				}

				last = Integer.parseInt(tokenizer.nextToken());// last number

			} else if (token.startsWith("Sbjct")) {
				tokenizer.nextToken();// fisrt number
				seq = tokenizer.nextToken();
				if (!gapFlag) {
					sbuffer.replace(first - 1, last, seq);
				} else {
					String tmp = "";
					int bgn = -1, finish = gaplist[0];

					for (int k = 0; k < gaplist.length; k++) {
						finish = gaplist[k];
						tmp += seq.substring(bgn + 1, finish);
						bgn = finish;

					}
					tmp += seq.substring(bgn + 1);
					sbuffer.replace(first - 1, last, tmp);
					gapFlag = false;
				}
				tokenizer.nextToken();
			}

		}

		sequence.setSequence(sbuffer.toString());
		return sequence;
	}

	public Sequence parseFasta(File file) throws IOException,
			SequenceParserException {
		InputStream inputStream = new FileInputStream(file);
		StringBuffer buffer = new StringBuffer();
		int ch;
		while ((ch = inputStream.read()) != -1) {
			buffer.append((char) ch);
		}
		return parseFasta(buffer.toString());
	}

	public Sequence parseFasta(String sequence) throws SequenceParserException {
		if (sequence == null) {
			throw new SequenceParserException("Null sequence");
		}

		if (sequence.trim().length() == 0) {
			throw new SequenceParserException("Empty sequence");
		}

		sequence = sequence.replaceAll("\r\n", "\n");

		String sequenceName = null;
		String sequenceDescription = null;

		if (sequence.startsWith(">")) {
			// FASTA format
			int index = sequence.indexOf("\n");

			if (index == -1) {
				throw new SequenceParserException("Invalid sequence format");
			}

			String first = sequence.substring(1, index);
			sequence = sequence.substring(index);

			index = 0;
			for (int i = 0; i < first.length() && first.charAt(i) != ' '
					&& first.charAt(i) != '\t'; i++, index++) {
				// Skip white spaces
			}
			sequenceName = first.substring(0, index);
			StringTokenizer stringTokenizer = new StringTokenizer(sequenceName,
					"|");
			while (stringTokenizer.hasMoreTokens()) {
				sequenceName = stringTokenizer.nextToken();
			}
			sequenceDescription = index + 1 > first.length() ? "" : first
					.substring(index + 1);
		} else {
			// Plain format ... nothing to do here
		}

		Sequence s = new Sequence(prepare(sequence), sequenceName,
				sequenceDescription, Sequence.PROTEIN);
		seqLength = sequence.length();
		return s;
	}

	public ArrayList<Sequence> getDb() {
		return db;
	}

	public void setDb(ArrayList<Sequence> db) {
		this.db = db;
	}

	public Sequence getQuery() {
		return query;
	}

	public void setQuery(Sequence query) {
		this.query = query;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<Sequence> getList() {
		return list;
	}

	public void setList(ArrayList<Sequence> list) {
		this.list = list;
	}

	public String getQueryId() {
		return queryId;
	}

	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getMoleculeType() {
		return moleculeType;
	}

	public void setMoleculeType(String moleculeType) {
		this.moleculeType = moleculeType;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	private static String prepare(String sequence)
			throws SequenceParserException {
		StringBuffer buffer = new StringBuffer();
		String copy = sequence.trim().toUpperCase();

		for (int i = 0, n = copy.length(); i < n; i++) {
			switch (copy.charAt(i)) {
			// skip whitespaces
			case 9:
			case 10:
			case 13:
			case 32:
				break;

			// add a valid character
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'G':
			case 'H':
			case 'I':
			case 'K':
			case 'L':
			case 'M':
			case 'N':
			case 'P':
			case 'Q':
			case 'R':
			case 'S':
			case 'T':
			case 'U':
			case 'V':
			case 'W':
			case 'Y':
			case 'Z':
			case 'X':
			case '#':

			case '-':
			case '*':
				buffer.append(copy.charAt(i));
				break;

			// throw an exception for anything else
			default:
				throw new SequenceParserException(
						"Invalid sequence character: '" + copy.charAt(i) + "'");
			}
		}
		return buffer.toString();
	}

}
