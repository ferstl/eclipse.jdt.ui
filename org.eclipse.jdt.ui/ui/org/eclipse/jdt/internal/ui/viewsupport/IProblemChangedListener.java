/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.jdt.internal.ui.viewsupport;

import java.util.Set;

public interface IProblemChangedListener {
	
	void problemsChanged(Set changedElements);
}