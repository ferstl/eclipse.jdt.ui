/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.ui.refactoring;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ui.help.WorkbenchHelp;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Presents the list of failed preconditions to the user
 */
class ErrorWizardPage extends RefactoringWizardPage {
		
	public static final String PAGE_NAME= "ErrorPage"; //$NON-NLS-1$
	
	private RefactoringStatus fStatus;
	private RefactoringStatusViewer fViewer;
	
	public ErrorWizardPage() {
		super(PAGE_NAME);
	}
	
	/**
	 * Sets the page's refactoring status to the given value.
	 * @param status the refactoring status.
	 */
	public void setStatus(RefactoringStatus status) {
		fStatus= status;
		if (fStatus != null) {
			setPageComplete(isRefactoringPossible());
			int severity= fStatus.getSeverity();
			if (severity >= RefactoringStatus.FATAL) {
				setDescription(RefactoringMessages.getString("ErrorWizardPage.cannot_proceed")); //$NON-NLS-1$
			} else if (severity >= RefactoringStatus.INFO) {
				setDescription(RefactoringMessages.getString("ErrorWizardPage.confirm")); //$NON-NLS-1$
			} else {
				setDescription(""); //$NON-NLS-1$
			}
		} else {
			setPageComplete(true);
			setDescription(""); //$NON-NLS-1$
		}	
	}
	
	public RefactoringStatus getStatus() {
		return fStatus;
	}
	
	//---- UI creation ----------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * Method declared in IWizardPage.
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		setControl(fViewer= new RefactoringStatusViewer(parent, SWT.NONE));
		Dialog.applyDialogFont(fViewer);
		WorkbenchHelp.setHelp(getControl(), IJavaHelpContextIds.REFACTORING_ERROR_WIZARD_PAGE);			
	}
	
	//---- Reimplementation of WizardPage methods ------------------------------------------

	/* (non-Javadoc)
	 * Method declared on IDialog.
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			fViewer.setStatus(fStatus);
		}
		super.setVisible(visible);
	}
	
	/* (non-Javadoc)
	 * Method declared in IWizardPage.
	 */
	public boolean canFlipToNextPage() {
		// We have to call super.getNextPage since computing the next
		// page is expensive. So we avoid it as long as possible.
		return fStatus != null && isRefactoringPossible() &&
			   isPageComplete() && super.getNextPage() != null;
	}
	
	/* (non-Javadoc)
	 * Method declared in IWizardPage.
	 */
	public IWizardPage getNextPage() {
		RefactoringWizard wizard= getRefactoringWizard();
		Change change= wizard.getChange();
		if (change == null) {
			change= wizard.createChange(new CreateChangeOperation(getRefactoring()), false);
			wizard.setChange(change);
		}
		if (change == null)
			return this;
			
		return super.getNextPage();
	}
	
	/* (non-JavaDoc)
	 * Method defined in RefactoringWizardPage
	 */
	protected boolean performFinish() {
		RefactoringWizard wizard= getRefactoringWizard();
		Change change= wizard.getChange();
		PerformChangeOperation op= null;
		if (change != null) {
			op= new UIPerformChangeOperation(change);
		} else {
			CreateChangeOperation ccop= new CreateChangeOperation(getRefactoring());
			op= new UIPerformChangeOperation(ccop);
		}
		return wizard.performFinish(op);
	} 
	
	//---- Helpers ----------------------------------------------------------------------------------------
	
	private boolean isRefactoringPossible() {
		return fStatus.getSeverity() < RefactoringStatus.FATAL;
	}	
}
