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

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.bewsia.basic.Controller;
import com.bewsia.basic.UITool;
import com.bewsia.resource.Images;
import com.bewsia.resource.Labels;

public class ListEngineDialog extends Dialog {

    private Logger logger = Logger.getLogger(ListEngineDialog.class);
    
    private Controller controller;
    private Shell shell;
    
    private Table tableEngine;
	
    public ListEngineDialog(Shell parent, Controller controller) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.controller = controller;
        setText(Labels.get("ListEngineDialog.WindowTitle"));
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
        tableEngine = table;
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
        column.setText(Labels.get("ListEngineDialog.LabelTitle"));
        
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ListEngineDialog.ButtonClose"));
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
        button.setText(Labels.get("ListEngineDialog.ButtonAdd"));
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
	    		AddEngineDialog dlg = new AddEngineDialog(shell, controller);
	    		dlg.open();
	    		if (!dlg.getCanceled()) {
	    			fillData();
	    		}
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ListEngineDialog.ButtonDelete"));
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
				for (int i = 0; i < tableEngine.getItemCount(); i++) {
					TableItem ti = tableEngine.getItem(i);
					if (ti.getChecked()) {
						idices.add(i);
					}
				}
				if (idices.size() == 0) return;
				int oldsize = controller.getConfig().getInt("engine.size");
				int newsize = 0;
				for (int i = 1; i <= tableEngine.getItemCount(); i++) {
					if (idices.indexOf(i - 1) >= 0) continue;
					newsize++;
					controller.getConfig().setValue("engine." + newsize + ".title", controller.getConfig().getString("engine." + i + ".title"));
					controller.getConfig().setValue("engine." + newsize + ".path", controller.getConfig().getString("engine." + i + ".path"));
					controller.getConfig().setValue("engine." + newsize + ".quota", controller.getConfig().getString("engine." + i + ".quota"));
				}
				for (int i = newsize + 1; i <= oldsize; i++) {
					controller.getConfig().setValue("engine." + i + ".title", "");
					controller.getConfig().setValue("engine." + i + ".path", "");
					controller.getConfig().setValue("engine." + i + ".quota", "");
				}
				controller.getConfig().setValue("engine.size", newsize);
				controller.saveConfig();
				fillData();
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ListEngineDialog.ButtonEdit"));
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
				for (int i = 0; i < tableEngine.getItemCount(); i++) {
					TableItem ti = tableEngine.getItem(i);
					if (ti.getChecked()) {
						idices.add(i);
					}
				}
				if (idices.size() == 0) return;
				EditEngineDialog dlg = new EditEngineDialog(shell, controller, idices.get(0) + 1);
				dlg.open();
				if (!dlg.getCanceled()) {
					fillData();
				}
			}
        });

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("ListEngineDialog.ButtonSelect"));
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
				for (int i = 0; i < tableEngine.getItemCount(); i++) {
					TableItem ti = tableEngine.getItem(i);
					if (ti.getChecked()) {
						idices.add(i);
					}
				}
				if (idices.size() == 0) return;
				int no = idices.get(0) + 1;
	    		controller.getConfig().setValue("engine.selected.path", controller.getConfig().getString("engine." + no + ".path"));
	    		controller.getConfig().setValue("engine.selected.title", controller.getConfig().getString("engine." + no + ".title"));
	    		controller.getConfig().setValue("engine.selected.quota", controller.getConfig().getDouble("engine." + no + ".quota"));
	    		controller.saveConfig();
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
    	controller.checkEngineList();
    	tableEngine.removeAll();
    	int size = controller.getConfig().getInt("engine.size");
    	for (int i = 1; i <= size; i++) {
    		TableItem item = new TableItem(tableEngine, SWT.NONE);
    		item.setText(1, controller.getConfig().getString("engine." + i + ".title"));
    	}
    }
    
}
