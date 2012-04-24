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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.bewsia.basic.Controller;
import com.bewsia.basic.UITool;
import com.bewsia.resource.Images;
import com.bewsia.resource.Labels;
import com.bewsia.schema.Spider;
import com.bewsia.script.safe.lucene.SEntity;

public class ListSpiderDialog extends Dialog {

    private Logger logger = Logger.getLogger(ListSpiderDialog.class);
    
    private Controller controller;
    private Shell shell;
    
    private Table tableSpider;
    private List<Spider> spiders;
	
    public ListSpiderDialog(Shell parent, Controller controller) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.controller = controller;
        setText(Labels.get("ListSpiderDialog.WindowTitle"));
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
    }
    
    private void createContents(final Shell shell) {
    	FormData fd;
    	Label label;
    	Button button;
    	Table table;
    	TableColumn column;
    	
        shell.setImage(Images.get("Icon.Bewsia"));
        shell.setLayout(new FormLayout());
        
        table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
        tableSpider = table;
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 10);
        fd.width = 395;
        fd.height = 430;
        table.setLayoutData(fd);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        column = new TableColumn(table, SWT.NONE);
        column.setWidth(30);
        column = new TableColumn(table, SWT.NONE);
        column.setWidth(340);
        column.setText(Labels.get("ListSpiderDialog.LabelTitle"));
        
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ListSpiderDialog.ButtonClose"));
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

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ListSpiderDialog.ButtonAdd"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -95);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
	    		AddSpiderDialog dlg = new AddSpiderDialog(shell, controller);
	    		dlg.open();
	    		if (!dlg.getCanceled()) {
	    			fillData();
	    		}
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ListSpiderDialog.ButtonDelete"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -180);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				List<Integer> idices = new ArrayList<Integer>();
				for (int i = 0; i < tableSpider.getItemCount(); i++) {
					TableItem ti = tableSpider.getItem(i);
					if (ti.getChecked()) {
						idices.add(i);
					}
				}
				if (idices.size() == 0) return;
				for (int i = 0; i < idices.size(); i++) {
					int idx = idices.get(i);
					Spider sp = spiders.get(idx);
					sp.delete();
				}
				fillData();
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ListSpiderDialog.ButtonEdit"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -265);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				List<Integer> idices = new ArrayList<Integer>();
				for (int i = 0; i < tableSpider.getItemCount(); i++) {
					TableItem ti = tableSpider.getItem(i);
					if (ti.getChecked()) {
						idices.add(i);
					}
				}
				if (idices.size() == 0) return;
				Spider sp = spiders.get(idices.get(0));
				EditSpiderDialog dlg = new EditSpiderDialog(shell, controller, sp);
				dlg.open();
				if (!dlg.getCanceled()) {
					fillData();
				}
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ListSpiderDialog.ButtonRun"));
        fd = new FormData();
        fd.bottom = new FormAttachment(100, -10);
        fd.right = new FormAttachment(100, -350);
        fd.width = 75;
        button.setLayoutData(fd);
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				List<Integer> idices = new ArrayList<Integer>();
				for (int i = 0; i < tableSpider.getItemCount(); i++) {
					TableItem ti = tableSpider.getItem(i);
					if (ti.getChecked()) {
						idices.add(i);
					}
				}
				if (idices.size() == 0) return;
				Spider sp = spiders.get(idices.get(0));
				RunSpiderDialog dlg = new RunSpiderDialog(MainWindow.getInstance().getShell(), controller, sp);
				dlg.open();
			}
        });
        
        label = new Label(shell, SWT.LEFT);
        fd = new FormData();
        fd.top = new FormAttachment(0, 500);
        fd.left = new FormAttachment(0, 435);
        label.setLayoutData(fd);
    
        fillData();
    }
    
    private void fillData() {
    	spiders = listSpider();
    	tableSpider.removeAll();
    	for (int i = 0; i < spiders.size(); i++) {
    		Spider sp = spiders.get(i);
    		TableItem item = new TableItem(tableSpider, SWT.NONE);
    		item.setText(1, sp.getTitle());
    	}
    }
    
    private List<Spider> listSpider() {
    	List<Spider> tag = new ArrayList<Spider>();
    	Spider pat = controller.newSpider();
    	List<SEntity> results = pat.search(pat.getKind(), pat.newMatchAllDocsQuery(), pat.newSort(pat.newSortField(pat.TITLE, pat.sortFieldString(), false)), Integer.MAX_VALUE);
    	for (int i = 0; i < results.size(); i++) {
    		Spider sp = controller.newSpider();
    		sp.fromString(results.get(i).toString());
    		tag.add(sp);
    	}
    	return tag;
    }
    
}
