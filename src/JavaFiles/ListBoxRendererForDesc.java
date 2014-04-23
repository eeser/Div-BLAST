package JavaFiles;

import jaligner.Sequence;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;


//for rendering sequences to a list box in result page
public class ListBoxRendererForDesc implements ListitemRenderer {

	Listcell tempcell;
	int count = 1;

	/*
	 * public ListBoxRendererForDesc(org.zkoss.zul.Label detail){
	 * this.detail=detail; this.box=(Groupbox)
	 * detail.getParent().getParent().getParent().getParent(); }
	 */

	@Override
	public void render(Listitem item, Object data, int index) throws Exception {

		// TODO Auto-generated method stub

		Sequence seq = (Sequence) data;// TODO sonra bunu sequence yapmalıyım

		tempcell = new Listcell();
		tempcell.appendChild(new Checkbox());
		item.appendChild(tempcell);

		tempcell = new Listcell();
		tempcell.setLabel(seq.getCount()+"");
	
		item.appendChild(tempcell);

		item.setValue(seq);
		tempcell = new Listcell();
		tempcell.setLabel(seq.getDescription());

		// tempcell.setValue("<a href=\"#detailBox\">"+seq.getDescription()+"</a>");
		// A link=new A(seq.getDescription());
		// link.setHref("#seqDetail");
		// AnchorlayoutTag a=new AnchorlayoutTag();
		//
		// tempcell.appendChild(link);

		// tempcell.setWidgetListener("onclick",
		// "window.location.href='#seqDetail'");

		// new Button().sethr
		// tempcell.setValue(name);
		item.appendChild(tempcell);
		// TODO bir de buraya sıra eklenmeli

		tempcell = new Listcell();
		tempcell.setLabel((int) seq.getScore() + "");
		item.appendChild(tempcell);

		tempcell = new Listcell();
		tempcell.setLabel(seq.getCoveragePercent() + "%");
		item.appendChild(tempcell);

		tempcell = new Listcell();
		tempcell.setLabel(seq.getExpectString());
		item.appendChild(tempcell);

		tempcell = new Listcell();
		tempcell.setLabel(seq.getIdentities() + "%");
		item.appendChild(tempcell);
		item.setWidgetListener("onClick", "window.location.href='#seqDetail'");

		item.addEventListener("onClick", new ItemListener(seq));

	}

	class ItemListener implements org.zkoss.zk.ui.event.EventListener {

		Sequence seq;

		ItemListener(Sequence s) {
			seq = s;
		}

		@Override
		public void onEvent(Event event) throws Exception {
			/*
			 * detail.setPre(true); detail.setValue(seq.getTostr());
			 * System.out.println(seq.getTostr()); box.setVisible(true);
			 */

			Clients.evalJavaScript("setLabel('"
					+ seq.getTostr().replaceAll("\n", "#") + "')");
		}

	}

}
