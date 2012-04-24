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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bewsia.basic.Controller;
import com.bewsia.basic.UITool;
import com.bewsia.resource.Images;
import com.bewsia.resource.Labels;

public class EditEngineDialog extends Dialog {

    private Logger logger = Logger.getLogger(EditEngineDialog.class);
    
    private Controller controller;
    private Shell shell;
    private Text textTitle;
    private Text textPath;
    private Text textQuota;
    private int no;
	
    private boolean canceled = true;
	
    public EditEngineDialog(Shell parent, Controller controller, int no) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        this.controller = controller;
        this.no = no;
        setText(Labels.get("EditEngineDialog.WindowTitle"));
    }
	
    public boolean getCanceled() {
    	return canceled;
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
    	Text text;
    	Combo combo;
    	
        shell.setImage(Images.get("Icon.Bewsia"));
        shell.setLayout(new FormLayout());
    	
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("EditEngineDialog.LabelTitle"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 10);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textTitle = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 30);
        fd.left = new FormAttachment(0, 10);
        fd.width = 400;
        text.setLayoutData(fd);
        text.setText(controller.getConfig().getString("engine." + no + ".title"));

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("EditEngineDialog.LabelPath"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 60);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textPath = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 80);
        fd.left = new FormAttachment(0, 10);
        fd.width = 335;
        text.setLayoutData(fd);
        text.setText(controller.getConfig().getString("engine." + no + ".path"));
        
        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("EditEngineDialog.ButtonBrowse"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 78);
        fd.right = new FormAttachment(100, -10);
        fd.width = 60;
        button.setLayoutData(fd);
        
        button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				DirectoryDialog dlg = new DirectoryDialog(shell);
				dlg.setText("Browse engine path");
				dlg.setMessage("Select path of engine");
				String folder = dlg.open();
				if (folder != null) {
					textPath.setText(folder);
				}
			}
        });
        
        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("EditEngineDialog.LabelQuota"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 110);
        fd.left = new FormAttachment(0, 10);
        label.setLayoutData(fd);
        
        text = new Text(shell, SWT.BORDER);
        textQuota = text;
        fd = new FormData();
        fd.top = new FormAttachment(0, 130);
        fd.left = new FormAttachment(0, 10);
        fd.width = 100;
        text.setLayoutData(fd);
        text.setText(controller.getConfig().getString("engine." + no + ".quota"));

        label = new Label(shell, SWT.NONE);
        label.setText(Labels.get("EditEngineDialog.LabelQuotaUnit"));
        fd = new FormData();
        fd.top = new FormAttachment(0, 132);
        fd.left = new FormAttachment(0, 130);
        label.setLayoutData(fd);
        
        
        label = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
    	fd = new FormData();
    	fd.bottom = new FormAttachment(100, -40);
    	fd.left = new FormAttachment(0, 0);
    	fd.right = new FormAttachment(100, 0);
    	label.setLayoutData(fd);

        button = new Button(shell, SWT.PUSH);
        button.setText(Labels.get("EditEngineDialog.ButtonClose"));
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
        button.setText(Labels.get("EditEngineDialog.ButtonSave"));
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
				if (textTitle.getText().trim().length() == 0) {
					UITool.warningBox(shell, Labels.get("EditEngineDialog.MessageTitleRequired"));
					return;
				}
				File file = new File(textPath.getText());
				if (!file.exists() || !file.isDirectory()) {
					UITool.warningBox(shell, Labels.get("EditEngineDialog.MessagePathNotExists"));
					return;
				}
				double quota = 0;
				try {
					quota = Double.parseDouble(textQuota.getText());
				} catch (Exception e) {
					quota = 0;
				}
				if (quota <= 0) {
					UITool.warningBox(shell, Labels.get("EditEngineDialog.MessageInvalidQuota"));
					return;
				}
				controller.getConfig().setValue("engine." + no + ".title", textTitle.getText());
				controller.getConfig().setValue("engine." + no + ".path", textPath.getText());
				controller.getConfig().setValue("engine." + no + ".quota", quota);
				controller.saveConfig();
				canceled = false;
				shell.close();
			}
        });
        
        label = new Label(shell, SWT.LEFT);
        fd = new FormData();
        fd.top = new FormAttachment(0, 200);
        fd.left = new FormAttachment(0, 435);
        label.setLayoutData(fd);
        
    }
	
}
