package JavaFiles;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class RequiredMethods {
	String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	Random rnd = new Random();

	//generates random string for request id
	public String randomString() throws IOException {

		int len = 10;
		StringBuilder sb = new StringBuilder(len);
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream("outfiles.txt")));
		String line = br.readLine();
		while (line != null) {
			list.add(line);

			line = br.readLine();
		}

		String s;
		br.close();
		do {
			for (int i = 0; i < len; i++)
				sb.append(AB.charAt(rnd.nextInt(AB.length())));
			s = sb.toString();
		} while (list.contains(s));
		DataOutputStream writer = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream("outfiles.txt")));
		String out = "";
		for (String st : list) {
			out += st + "\n";
		}
		out += s + "\n";
		writer.write(out.getBytes());//TODO kontrol
		writer.close();
		return s;
	}
	
	public boolean includes(String r) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream("outfiles.txt")));
		String line = br.readLine();
		while (line != null) {
			list.add(line);

			line = br.readLine();
		}

		br.close();
		
		return list.contains(r.toUpperCase());

	}
	
	//for controlling ex saved request ids
	public String[] parameters(String r) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(r.toUpperCase()+".inf")));
		String line = br.readLine();
		while (line != null) {
			list.add(line);
			line = br.readLine();
		}

		String[] arr=new String[list.size()];
		br.close();
		System.out.println(arr.length);
		for(int i=0;i<list.size();i++){
			arr[i]=list.get(i);
			System.out.print(arr[i]);
		}
		return arr;

	}
}
