package JavaFiles;

import jaligner.Sequence;

import java.io.FileNotFoundException;
import java.util.Enumeration;

import org.apache.tomcat.jni.File;
import org.zkoss.zk.ui.util.*;

import javax.mail.Session;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Label;
import org.zkoss.zul.Timer;

import com.lowagie.text.pdf.hyphenation.TernaryTree;

import diversity.Diversity;
import diversity.Diversity_entropybased;
import diversity.FileParser;

//thread running with timer in the wait page
public class SampleWorkingThread extends Thread {

	public boolean done = false;
	org.zkoss.zk.ui.Session session1;
	Object arg;
	boolean statusdiv = false;
	boolean error = false;

	public SampleWorkingThread(Object arg, org.zkoss.zk.ui.Session session) {
		this.arg = arg;
		this.session1 = session;

	}

	@Override
	public synchronized void start() {
		done = false;

	};

	@Override
	public void run() {
		super.run();

		try {
			if (!((Boolean) session1.getAttribute("byrequest"))) {
				if (session1.getAttribute("program").equals("blastp"))
					(new JavaFiles.NCBIBlastClient()).main((String[]) arg);
				else
					(new JavaFiles.PSIBlastClient()).main((String[]) arg);
			}

			session1.setAttribute("divstatus", new Boolean(true));

			String div = (String) session1.getAttribute("divtype");
			FileParser fparser = new diversity.FileParser(
					(String) session1.getAttribute("out"));
			java.util.List seqs = null;
			Sequence s;
			double rate = (Double) session1.getAttribute("divrate");
			Integer k = (Integer) session1.getAttribute("knumber");
			if (div.equals("bit")) {
				Diversity diversity = new Diversity(fparser.getDb(),
						fparser.getQuery(), k, 1 - rate, rate);
				seqs = diversity.findTopKDiverseSet();
			} else if (div.equals("ent")) {
				Diversity_entropybased div1 = new Diversity_entropybased(
						fparser.getDb(), fparser.getQuery(), k, 1 - rate, rate); 
				seqs = div1.findTopKDiverseSet();
			} else {
				seqs = fparser.getDb().subList(0, k);
			}

			session1.setAttribute("fparser", fparser);
			session1.setAttribute("done", new Boolean(true));
			session1.setAttribute("list", seqs);
			session1.setAttribute("seq", fparser.getQuery());

			done = true;
		} catch (Exception e) {

			System.out.println(e.getMessage());
			if (e instanceof FileNotFoundException)
					session1.setAttribute(
							"exception",
							"An error occured while getting result from BLAST web services, it could be query related. Please check your query and submit again.");
			else
				session1.setAttribute("exception", e.getMessage());
			error = true;
		}
	}
	
}
