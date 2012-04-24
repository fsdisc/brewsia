/*
 *  Bewsia - Micro Search Engine for Desktop
 * 
 *  Copyright (c) 2011 Tran Dinh Thoai <dthoai@yahoo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.bewsia.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Query;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bewsia.basic.CodeTool;
import com.bewsia.basic.Controller;
import com.bewsia.basic.UITool;
import com.bewsia.resource.Files;
import com.bewsia.resource.Images;
import com.bewsia.resource.Labels;
import com.bewsia.script.safe.lucene.SEntity;

public class MainWindow {

    private static Logger logger = Logger.getLogger(MainWindow.class);
	
	private Controller controller;
	private Shell shell;
	private Browser browser;
	private String page = "Home";
	private Timer menuTimer;
	
	private static MainWindow instance;
	
    public MainWindow() {
    	instance = this;
    	this.controller = new Controller();
    	Display display = Display.getDefault();
    	shell = new Shell(display, SWT.CLOSE | SWT.MAX | SWT.MIN | SWT.RESIZE);
    	decorate();
    }
    
    public static MainWindow getInstance() {
    	return instance;
    }
    
    public Shell getShell() {
    	return shell;
    }
    
    public void open() {
    	menuTimer = new Timer();
    	menuTimer.schedule(new MenuTask(), 10, 10);
    	shell.open();
    	while (!shell.isDisposed()) {
    		if (!shell.getDisplay().readAndDispatch())
    			shell.getDisplay().sleep();
    	}
    	menuTimer.cancel();
    	menuTimer.purge();
    }
    
    private void decorate() {
    	FormData fd;
    	Label label;
    	Text text;
    	
		shell.setText(Labels.get("MainWindow.WindowTitle") + " - " + controller.getSelectedEngineTitle());
    	shell.setSize(800, 600);
    	shell.setImage(Images.get("Icon.Bewsia"));
    	
    	shell.setLayout(new FormLayout());
    	browser = new Browser(shell, SWT.NONE);
    	fd = new FormData();
    	fd.top = new FormAttachment(0, 0);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	fd.bottom = new FormAttachment(100, 0);
    	browser.setLayoutData(fd);
    	
    	browser.addLocationListener(new LocationListener() {
			@Override
			public void changed(LocationEvent arg) {
			}

			@Override
			public void changing(LocationEvent arg) {
				if (arg.location.indexOf("about:") < 0) {
					arg.doit = false;
					openUrl(arg.location);
				}
			}
    	});

    	UITool.placeCentered(shell, 800, 600);
    	
    	search("", 1);
    }
    
    private void openUrl(String url) {
    	org.eclipse.swt.program.Program.launch(url);    	
    }
    
    private void homeMenu(String location) {
    	if ("Home".equals(location)) {
    		loadPage(location);
    	} else if ("Search".equals(location)) {
			String term = "";
			try {
				term = browser.evaluate("return getTerm()") + "";
			} catch (Throwable e) {
				logger.error("", e);
			}
    		int pageno = 1;
    		try {
    			pageno = Integer.parseInt(browser.evaluate("return getPage()") + "");
    		} catch (Throwable e) {
    			logger.error("", e);
    		}
    		search(term, pageno);
    	} else if ("Spider".equals(location)) {
    		Timer timer = new Timer();
    		timer.schedule(new ListSpiderTask(timer), 1);
    	} else if ("Engine".equals(location)) {
    		Timer timer = new Timer();
    		timer.schedule(new ListEngineTask(timer), 1);
    	}
    }
    
    private void search(String query, int pageno) {
    	if (pageno <= 0) pageno = 1;
    	int pagesize = 10;
    	String content = Files.get("Home");
    	content = CodeTool.replace(content, "$query$", encode(query));
    	String results = "";
    	
    	if (query.trim().length() > 0) {
    		SEntity pat = controller.newSelectedEntity();
    		Query qry = pat.newMatchAllDocsQuery();
    		if (!query.equalsIgnoreCase("spec:all")) {
        		try {
            		qry = pat.parseQuery(query, new String[] { "title", "desc" }, new Occur[] { Occur.SHOULD, Occur.SHOULD });
        		} catch (Exception e) {
        		}
    		}
    		List<SEntity> rs = new ArrayList<SEntity>();
    		int count = 0;
    		try {
    			count = pat.count("Link", qry, Integer.MAX_VALUE);
    		} catch (Exception e) {
    		}
			int pagecount = 1;
			if (count > 0) {
				pagecount = ((count - 1 - ((count - 1) % pagesize)) / pagesize) + 1;
			}
    		if (pageno > pagecount) {
    			pageno = pagecount;
    		}
    		int max = pageno + 5;
    		if (max > pagecount) max = pagecount;
    		int min = pageno - 5;
    		if (min < 1) min = 1;
    		List<Integer> pl = new ArrayList<Integer>();
    		for (int i = 1; i <= 5; i++) {
    			if (i <= pagecount) {
    				pl.add(i);
    			}
    		}
    		for (int i = min; i <= max; i++) {
    			if (pl.indexOf(i) < 0) {
    				pl.add(i);
    			}
    		}
    		for (int i = pagecount - 4; i <= pagecount; i++) {
    			if (pl.indexOf(i) < 0 && i > 0) {
    				pl.add(i);
    			}
    		}
    		try {
        		rs = pat.search("Link", qry, pagesize, pageno);
    		} catch (Exception e) {
    			logger.error("", e);
    		}
    		results = Files.get("Result");
    		for (int i = 0; i < rs.size(); i++) {
    			SEntity rec = rs.get(i);
    			String title = rec.getString("title");
    			String desc = rec.getString("desc");
    			String url = rec.getString("url");
    			try {
        			title = pat.highlight(qry, title, "title", 100, 3, " (...) "); 
    			} catch (Exception e) {
    			}
    			try {
        			desc = pat.highlight(qry, desc, "desc", 100, 3, " (...) ");
    			} catch (Exception e) {
    			}
    			String item = Files.get("ResultItem");
    			item = CodeTool.replace(item, "$url$", url);
    			item = CodeTool.replace(item, "$title$", title);
    			item = CodeTool.replace(item, "$desc$", desc);
    			results = CodeTool.replace(results, "$item$", item + "$item$");
    		}
			results = CodeTool.replace(results, "$item$", "");
			String pages = Files.get("ResultPageList");
			for (int i = 0; i < pl.size(); i++) {
				int no = pl.get(i);
				String item = Files.get("ResultPageItem");
				String cls = "page";
				if (pageno == no) {
					cls = "curpage";
				}
    			item = CodeTool.replace(item, "$class$", cls);
    			item = CodeTool.replace(item, "$pageno$", no + "");
    			pages = CodeTool.replace(pages, "$item$", item + "$item$");
			}
			pages = CodeTool.replace(pages, "$item$", "");
			results = CodeTool.replace(results, "$pagelist$", pages);
    	} else {
    		pageno = 1;
    	}

    	content = CodeTool.replace(content, "$pageno$", "" + pageno);
    	content = CodeTool.replace(content, "$results$", results);
    	
    	browser.setText(content);
    	page = "Home";
    }

    private String encode(String src) {
    	String tag = src;
    	tag = CodeTool.replace(tag, "~", "~t");
    	tag = CodeTool.replace(tag, "\"", "~q");
    	return tag;
    }
    
    private void loadPage(String location) {
    	browser.setText(Files.get(location));
    	page = location;
    }

    private class ListEngineTask extends TimerTask {

    	private Timer timer;
    	
    	public ListEngineTask(Timer timer) {
    		this.timer = timer;
    	}
    	
		@Override
		public void run() {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
		    		ListEngineDialog dlg = new ListEngineDialog(shell, controller);
		    		dlg.open();
				}
			});
			timer.cancel();
			timer.purge();
			timer = null;
		}
    	
    }
    
    private class ListSpiderTask extends TimerTask {

    	private Timer timer;
    	
    	public ListSpiderTask(Timer timer) {
    		this.timer = timer;
    	}
    	
		@Override
		public void run() {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
		    		ListSpiderDialog dlg = new ListSpiderDialog(shell, controller);
		    		dlg.open();
				}
			});
			timer.cancel();
			timer.purge();
			timer = null;
		}
    	
    }
    
    private class MenuTask extends TimerTask {
    	
		@Override
		public void run() {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						shell.setText(Labels.get("MainWindow.WindowTitle") + " - " + controller.getSelectedEngineTitle());
					} catch (Exception e) {
						logger.error("", e);
					}
					String location = "";
					try {
						location = browser.evaluate("if (changed) { return ask(); } else { return '' }") + "";
					} catch (Throwable e) {
						logger.error("", e);
					}
					if (location.length() > 0) {
						if ("Home".equals(page)) {
							homeMenu(location);
						}
					}
				}
			});
		}
    	
    }
    
}
