package com.rcpapp.providers;

import java.io.File;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;

public class FileNameColumnLabelProvide extends LabelProvider implements IStyledLabelProvider {

	@Override
	public StyledString getStyledText(Object arg0) {
		if (arg0 instanceof File) {
			File file = (File) arg0;
			StyledString styledString = new StyledString(getFileName(file));
			String[] files = file.list();
			if (files != null) {
				styledString.append(" ( " + files.length + " ) ", StyledString.COUNTER_STYLER);
			}
			return styledString;
		}

		return null;
	}

	private String getFileName(File file) {
		String name = file.getName();
		return name.isEmpty() ? file.getPath() : name;
	}
}
