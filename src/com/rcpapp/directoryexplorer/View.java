package com.rcpapp.directoryexplorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.part.ViewPart;

import com.rcpapp.providers.FileNameColumnLabelProvide;
import com.rcpapp.providers.FileSizeLabelProvider;
import com.rcpapp.providers.FileTreeContentProvider;

public class View extends ViewPart {
	public static final String ID = "DirectoryExplorer.view";

	@Inject
	IWorkbench workbench;

	private TreeViewer viewer;
	private String directoryPath;
	private File dirFile;
	private Text searchText;
	private String pattern;
	private volatile int count;
	static Logger logger = Logger.getLogger(View.class.getName());

	@Override
	public void createPartControl(Composite parent) {

		GridLayout gridLayout = new GridLayout(2, true);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		parent.setLayout(gridLayout);

		// Created group for Directory browsing..
		Group dirGroup = new Group(parent, SWT.NONE);
		dirGroup.setText("Directory Browser");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		dirGroup.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 3;
		dirGroup.setLayoutData(gridData);

		Label dirLabel = new Label(dirGroup, SWT.NONE);
		dirLabel.setText("Directory Path :");

		Text dirtext = new Text(dirGroup, SWT.NONE);
		dirtext.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Button dirbutton = new Button(dirGroup, SWT.PUSH);
		dirbutton.setText("Browse");
		dirbutton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dirbutton.addSelectionListener(browseSelectionListener(parent, dirtext));

		// Create the tree viewer to display the file tree
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setContentProvider(new FileTreeContentProvider());
		viewer.getTree().setHeaderVisible(true);
		viewer.setComparator(viewerComparator());

		// Free Text Box is created to show the file information
		Text fileText = new Text(parent, SWT.BORDER | SWT.WRAP | SWT.MULTI);
		fileText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		viewer.addSelectionChangedListener(onClickFileViewer(fileText));

		TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		TreeColumn column = viewerColumn.getColumn();
		column.setText("File Name");
		column.setWidth(300);
		column.setResizable(true);
		viewerColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new FileNameColumnLabelProvide()));

		TreeViewerColumn sizeColumn = new TreeViewerColumn(viewer, SWT.NONE | SWT.RIGHT);
		sizeColumn.getColumn().setText("File Size");
		sizeColumn.getColumn().setWidth(100);
		sizeColumn.getColumn().setAlignment(SWT.RIGHT);
		sizeColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new FileSizeLabelProvider()));

		// created group for searching pattern
		Group group = new Group(parent, SWT.NONE);
		group.setText("Search Text");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		group.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;
		group.setLayoutData(gridData);

		Label label = new Label(group, SWT.NONE);
		label.setText("Input String :");

		searchText = new Text(group, SWT.SINGLE | SWT.BORDER);
		searchText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

		MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_INFORMATION | SWT.OK);
		messageBox.setText("Information");

		Button button = new Button(group, SWT.PUSH);
		button.setText("Search");
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalIndent = 5;
		button.setLayoutData(gridData);
		button.addSelectionListener(searchSelectionListener(parent, messageBox));
	}

	/*
	 * This method return the selected directory with their children files
	 */
	private SelectionAdapter browseSelectionListener(Composite parent, Text dirtext) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog directoryDialog = new DirectoryDialog(parent.getShell());
				directoryPath = directoryDialog.open();
				if (directoryPath != null)
					dirtext.setText(directoryPath);
				dirFile = new File(directoryPath);
				viewer.setInput(dirFile.listFiles());
			}
		};
	}

	/*
	 * This method is used to sort the files based on the size using
	 * ViewerComparator
	 */
	private ViewerComparator viewerComparator() {
		return new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				File file1 = (File) e1;
				File file2 = (File) e2;
				return (int) (file1.length() - file2.length());
			}
		};
	}

	private ISelectionChangedListener onClickFileViewer(Text fileText) {
		return new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				TreeSelection select = (TreeSelection) event.getSelection();
				TreePath[] paths = select.getPaths();
				int charCount = 0;
				String finalString;
				if (paths.length <= 0) { // while closing the tree
					return;
				}
				File file = (File) paths[0].getLastSegment();
				String absolutePath = file.getAbsolutePath();
				long size = file.length();

				FileReader fileReader;
				try {
					fileReader = new FileReader(file);
					BufferedReader bufferedReader = new BufferedReader(fileReader);
					int read = bufferedReader.read();
					while (read != -1) {
						char c = (char) read;
						if (c >= 33 && c <= 126) {
							charCount++;
						}
						read = bufferedReader.read();
					}
					bufferedReader.close();
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				finalString = "Path: " + absolutePath + "\n" + "File Size: " + size + "\n"
						+ "Number of Characters in File: " + charCount;
				fileText.setText(finalString);
				logger.info("Logging Information :" + "\n" + finalString);
			}
		};
	}

	private SelectionListener searchSelectionListener(Composite parent, MessageBox messageBox) {
		return new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(parent.getShell());
				monitorDialog.open();
				pattern = searchText.getText();
				count = 0;

				try {
					monitorDialog.run(true, true, runProgressMonitor());
				} catch (InvocationTargetException | InterruptedException e1) {
					e1.printStackTrace();
				}

				messageBox.setMessage("The the number of occurances of " + "'" + pattern + "' " + " are " + count);
				messageBox.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		};
	}

	private IRunnableWithProgress runProgressMonitor() {
		return new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor arg0) throws InvocationTargetException, InterruptedException {
				dirFile = new File(directoryPath);
				File[] listFiles = dirFile.listFiles();
				Queue<File> queue = new LinkedList<File>(Arrays.asList(listFiles));

				while (!queue.isEmpty()) {
					File file = queue.remove();

					if (file.isDirectory()) {
						File[] listFiles2 = file.listFiles();
						if (listFiles2 != null) {
							for (int i = 0; i < listFiles2.length; i++)
								queue.add(listFiles2[i]);
						}
						continue;
					}

					FileReader fileReader;
					try {

						if (!file.getName().endsWith(".txt"))
							continue;
						fileReader = new FileReader(file);
						BufferedReader reader = new BufferedReader(fileReader);
						while (true) {
							String readLine = reader.readLine();
							if (readLine == null)
								break;
							int startIndex = readLine.indexOf(pattern);
							while (startIndex != -1) {
								count++;
								startIndex = readLine.indexOf(pattern, startIndex + pattern.length());
							}
						}
						reader.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		};
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

}
