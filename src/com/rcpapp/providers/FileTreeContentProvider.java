package com.rcpapp.providers;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class FileTreeContentProvider implements ITreeContentProvider {
	/**
	 * Gets the children of the specified object
	 * 
	 * @param arg0 the parent object
	 * @return Object[]
	 */
	public Object[] getChildren(Object arg0) {

		FilenameFilter filter = filterDirectory();
		File[] listFiles = ((File) arg0).listFiles(filter); // get the next child
		return listFiles;
	}

	/**
	 * Gets the parent of the specified object
	 * 
	 * @param arg0 the object
	 * @return Object
	 */
	public Object getParent(Object arg0) {
		// Return this file's parent file
		return ((File) arg0).getParentFile();
	}

	/**
	 * Returns whether the passed object has children
	 * 
	 * @param arg0 the parent object
	 * @return boolean
	 */
	public boolean hasChildren(Object arg0) {
		File file = (File) arg0; /// get all directory structure and check whether it has children
		return file.isDirectory() ? true : false;

	}

	/**
	 * Gets the root element(s) of the tree
	 * 
	 * @param arg0 the input data
	 * @return Object[]
	 */
	public Object[] getElements(Object input) {
		File[] list = (File[]) input;
		return list;
	}

	/**
	 * Disposes any created resources
	 */
	public void dispose() {
		// Nothing to dispose
	}

	/**
	 * Called when the input changes
	 * 
	 * @param arg0 the viewer
	 * @param arg1 the old input
	 * @param arg2 the new input
	 */
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// Nothing to change
	}

	private FilenameFilter filterDirectory() {
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {

				java.nio.file.Path path = Paths.get(dir.getAbsolutePath(), name);
				File file = path.toFile();
				if (file.isDirectory()) {
					return true;
				}

				return name.endsWith(".txt") ? true : false;
			}
		};
		return filter;
	}
}
