package JavaFiles;

import java.util.concurrent.Executor;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;

//composer used in wait zul, controlling running program and interface together.
public class ResultComposer extends GenericForwardComposer {
	
	public String[] arr;
	public static Executor exe;
	@Wire
	Label request;

	public SampleWorkingThread sthread;

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent,
			ComponentInfo compInfo) {
		return super.doBeforeCompose(page, parent, compInfo);

	}

	@Override
	public void doAfterCompose(Component comp) throws Exception {

		super.doAfterCompose(comp);
		String outstr=(String) session.getAttribute("out");
		request.setValue(outstr.substring(outstr.lastIndexOf('/')+1));

		Object arg = session.getAttribute("ncbi");
		session.setAttribute("done", new Boolean("false"));
		session.setAttribute("divstatus", new Boolean("false"));
		sthread = (new JavaFiles.SampleWorkingThread(arg, session));

		exe.execute(sthread);

	}

}
