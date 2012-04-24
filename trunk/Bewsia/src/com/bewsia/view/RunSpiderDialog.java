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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bewsia.basic.Controller;
import com.bewsia.basic.UITool;
import com.bewsia.resource.Images;
import com.bewsia.resource.Labels;
import com.bewsia.schema.Spider;
import com.bewsia.script.LuceneHandler;
import com.bewsia.script.Machine;
import com.bewsia.script.safe.lucene.SEntity;

public class RunSpiderDialog extends Dialog {

    private Logger logger = Logger.getLogger(RunSpiderDialog.class);
    
    private Controller controller;
    private Shell shell;
	
    private Spider spider;
    private Text textLog;
    private Timer timer;
    
    public RunSpiderDialog(Shell parent, Controller controller, Spider spider) {
        super(parent, SWT.CLOSE | SWT.MIN);
        this.controller = controller;
        this.spider = spider;
        setText(Labels.get("RunSpiderDialog.WindowTitle") + " : " + spider.getTitle());
    }
    
    public void open() {
        shell = new Shell(getParent(), getStyle());
        shell.setText(getText());
        createContents(shell);
        shell.pack();
        shell.open();
        UITool.placeCentered(shell);
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        if (timer != null) {
        	timer.cancel();
        	timer.purge();
        	timer = null;
        }
    }
 
    private void createContents(final Shell shell) {
    	FormData fd;
    	Label label;
    	Button button;
    	Text text;
    	Combo combo;
    	
        shell.setImage(Images.get("Icon.Bewsia"));
        shell.setLayout(new FormLayout());

        text = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        textLog = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 10);
        fd.right = new FormAttachment(100, -10);
        fd.bottom = new FormAttachment(100, -50);
        text.setLayoutData(fd);
        
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("EditSpiderDialog.ButtonClose"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -10);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shell.close();
			}
        });
        
        label = new Label(shell, SWT.LEFT);
        fd = new FormData();
        fd.top = new FormAttachment(0, 500);
        fd.left = new FormAttachment(0, 435);
        label.setLayoutData(fd);
        
        timer = new Timer();
        timer.schedule(new RunTask(), 1);
    }
 
    private class RunTask extends TimerTask {

		@Override
		public void run() {
        	String dirEng = controller.getSelectedEnginePath();
        	double quota = controller.getSelectedEngineQuota();
        	File file = new File(dirEng, "index");
        	file.mkdirs();
        	String dirIdx = file.getAbsolutePath();
        	file = new File(dirEng, "backup");
        	file.mkdirs();
        	String dirBak = file.getAbsolutePath();
			
			DataHandler dh = new DataHandler(dirIdx, dirBak, quota);
			dh.save("Starting ...", "Info");
			Machine m = new Machine(dh);
			try {
				Map args = new HashMap();
				List links = new ArrayList();
				args.put("links", links);
				Machine.run(m, spider.getScript(), args);
				
				for (int i = 0; i < links.size(); i++) {
				    Map item = (Map)links.get(i);
				    String line = "";
				    for (Object key : item.keySet()) {
				        line += "\r\n" + key + " : " + item.get(key);
				    }
				    dh.save("\r\n" + (i + 1) + " --------------------------------\r\n" + line + "\r\n", "Info");
				}			
			} catch (Throwable e) {
				logger.error("", e);
				dh.save(e.getMessage(), "Error");
			}
			dh.save("Ending ...", "Info");
		}
    	
    }

    private class DataHandler extends Machine.Handler {
    	
    	private String idxDir;
    	private String bakDir;
    	private double quota;
    	
    	public DataHandler(String idxDir, String bakDir, double quota) {
    		this.idxDir = idxDir;
    		this.bakDir = bakDir;
    		this.quota = quota;
    	}
    	
        public SEntity.Handler getEntityHandler() { 
        	return new LuceneHandler(idxDir, bakDir, quota);
        }
    	
        public void debug(String message) {
        	save(message, "Debug");
        }
      
        public void error(String message) { 
        	save(message, "Error");
        }
      
        public void fatal(String message) { 
        	save(message, "Fatal");
        }
      
        public void info(String message) { 
        	save(message, "Info");
        }
    	
        public void save(String message, String stage) {
        	final String t_message = message;
        	final String t_stage = stage;
        	final Map t_map = new HashMap();
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					try {
						if (textLog == null || textLog.isDisposed()) {
							t_map.put("exit", true);
							return;
						}
			        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
			        	int size = 1024 * 100;
			        	if (textLog.getText().length() > size) {
			        		textLog.setText(textLog.getText().substring(size));
			        	}
			        	textLog.setText(textLog.getText() + "\r\n[" + sdf.format(new Date()) + "] " + t_stage + ":\r\n" + t_message + "\r\n");
			        	textLog.setSelection(textLog.getText().length());
					} catch (Throwable e) {
						logger.error("", e);
					}
				}
			});
			if (t_map.containsKey("exit")) {
				int i = 0/0;
			}
        }
    	
    }
    
}
