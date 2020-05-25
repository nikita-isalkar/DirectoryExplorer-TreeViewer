package com.rcpapp.providers;

import java.io.File;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;

public class FileSizeLabelProvider extends LabelProvider implements IStyledLabelProvider {

	@Override
	public StyledString getStyledText(Object arg0) {
		if (arg0 instanceof File) {
			File file = (File) arg0;
			if (file.isDirectory()) {
				// a directory is just a container and has no size
				return new StyledString("0");
			}
			return new StyledString(String.valueOf(file.length()));
		}
		return null;
	}
}