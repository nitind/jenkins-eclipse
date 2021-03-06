/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.ui.internal.preferences;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.Messages;
import com.cloudbees.eclipse.ui.PreferenceConstants;

/**
 * CloudBees account info settings
 * 
 * @author ahtik
 */

public class GeneralPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  protected IntegerFieldEditor intervRefresh;

  public GeneralPreferencePage() {
    super(GRID);
    setPreferenceStore(CloudBeesUIPlugin.getDefault().getPreferenceStore());
//    setDescription(Messages.pref_description);    
  }

  @Override
  public void createFieldEditors() {

    getFieldEditorParent().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    createHeader();
    
    createCompositeLogin();

    new Label(getFieldEditorParent(), SWT.NONE);

    createAllJenkins();
  }

  private void createHeader() {
//    Group group = new Group(getFieldEditorParent(), SWT.NONE);
//    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//
//    GridLayout gl = new GridLayout(1, false);
//    gl.marginLeft = 5;
//    gl.marginRight = 5;
//    gl.marginTop = 5;
//    gl.marginBottom = 5;
//    gl.horizontalSpacing = 5;
//
//    group.setLayout(gl);

    final Composite groupInnerComp = new Composite(getFieldEditorParent(), SWT.NONE);
    groupInnerComp.setLayout(new GridLayout(1, false));
    groupInnerComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    ImageDescriptor id = ImageDescriptor.createFromURL(CloudBeesUIPlugin.getDefault().getBundle()
        .getResource("/icons/jenkins_cloudbees_logo2.png"));    
    Label label = new Label (groupInnerComp, SWT.NONE);
    label.setImage (id.createImage());
    
    new Label(getFieldEditorParent(), SWT.NONE);
    
  }

  private void createAllJenkins() {
    Group group = new Group(getFieldEditorParent(), SWT.SHADOW_ETCHED_IN);
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    GridLayout gl = new GridLayout(1, false);
    gl.marginLeft = 5;
    gl.marginRight = 5;
    gl.marginTop = 5;
    gl.marginBottom = 5;
    gl.horizontalSpacing = 5;

    group.setLayout(gl);

    final Composite groupInnerComp = new Composite(group, SWT.NONE);
    groupInnerComp.setLayout(new GridLayout(2, false));
    groupInnerComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    group.setText(Messages.pref_group_allJenkins);

    BooleanFieldEditor intervEnable = new BooleanFieldEditor(PreferenceConstants.P_JENKINS_REFRESH_ENABLED,
        Messages.pref_jenkins_refresh_enabled, groupInnerComp) {
      @Override
      protected void valueChanged(boolean oldValue, boolean newValue) {
        super.valueChanged(oldValue, newValue);
        GeneralPreferencePage.this.intervRefresh.setEnabled(newValue, groupInnerComp);
      }
    };

    this.intervRefresh = new IntegerFieldEditor(PreferenceConstants.P_JENKINS_REFRESH_INTERVAL,
        Messages.pref_jenkins_refresh_interval, groupInnerComp);

    addField(intervEnable);
    addField(this.intervRefresh);

    this.intervRefresh.setEnabled(getPreferenceStore().getBoolean(PreferenceConstants.P_JENKINS_REFRESH_ENABLED),
        groupInnerComp);

    createAttachJenkinsLink(groupInnerComp);

  }

  private void createCompositeLogin() {
    Group group = new Group(getFieldEditorParent(), SWT.SHADOW_ETCHED_IN);
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    GridLayout gl = new GridLayout(1, false);
    gl.marginLeft = 5;
    gl.marginRight = 5;
    gl.marginTop = 5;
    gl.marginBottom = 5;
    gl.horizontalSpacing = 5;

    group.setLayout(gl);

    Composite groupInnerComp = new Composite(group, SWT.NONE);

    groupInnerComp.setLayout(new GridLayout(3, false));
    groupInnerComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    group.setText(Messages.pref_group_login);

    final StringFieldEditor fieldEmail = new StringFieldEditor(PreferenceConstants.P_EMAIL, Messages.pref_email, 30,
        groupInnerComp);

    GridData emailLayoutData = new GridData(GridData.FILL_HORIZONTAL);
    emailLayoutData.horizontalSpan = 3;
    fieldEmail.getTextControl(groupInnerComp).setLayoutData(emailLayoutData);
    addField(fieldEmail);

    final StringFieldEditor fieldPassword = new StringFieldEditor(PreferenceConstants.P_PASSWORD,
        Messages.pref_password, 30, groupInnerComp) {

      @Override
      protected void doLoad() {
        try {
          if (getTextControl() != null) {
            String value = CloudBeesUIPlugin.getDefault().readP();
            getTextControl().setText(value);
            this.oldValue = value;
          }
        } catch (StorageException e) {
          // Ignore StorageException, very likely just
          // "No password provided."
        }
      }

      @Override
      protected void doStore() {
        try {

          CloudBeesUIPlugin.getDefault().storeP(getTextControl().getText());

        } catch (Exception e) {
          CloudBeesUIPlugin
              .showError("Saving password failed!\nPossible cause: Eclipse security master password is not set.", e);
        }
      }

      @Override
      protected void doFillIntoGrid(Composite parent, int numColumns) {
        super.doFillIntoGrid(parent, numColumns);
        getTextControl().setEchoChar('*');
      }
    };

    fieldPassword.getTextControl(groupInnerComp).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    addField(fieldPassword);

    Button b = new Button(groupInnerComp, SWT.PUSH);
    GridData validateButtonLayoutData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    validateButtonLayoutData.widthHint = 75;
    b.setLayoutData(validateButtonLayoutData);

    b.setText(Messages.pref_validate_login);
    b.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {

        final String email = fieldEmail.getStringValue();
        final String password = fieldPassword.getStringValue();

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
        try {
          dialog.run(true, true, new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) {
              try {
                monitor.beginTask("Validating CloudBees account...", 100); //TODO i18n

                monitor.subTask("Connecting...");//TODO i18n
                monitor.worked(10);
                GrandCentralService gcs = new GrandCentralService();
                gcs.setAuthInfo(email, password);
                monitor.worked(20);

                monitor.subTask("Validating...");//TODO i18n

                final boolean loginValid = gcs.validateUser(monitor);
                monitor.worked(50);

                PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                  public void run() {
                    if (loginValid) {
                      MessageDialog.openInformation(
                          CloudBeesUIPlugin.getDefault().getWorkbench().getDisplay().getActiveShell(),
                          "Validation result", "Validation successful!");//TODO i18n
                    } else {
                      MessageDialog.openError(
                          CloudBeesUIPlugin.getDefault().getWorkbench().getDisplay().getActiveShell(),
                          "Validation result", "Validation was not successful!\nWrong email or password?");//TODO i18n
                    }
                  }

                });

                monitor.worked(20);

              } catch (CloudBeesException e1) {
                throw new RuntimeException(e1);
              } finally {
                monitor.done();
              }
            }
          });
        } catch (InvocationTargetException e1) {
          Throwable t1 = e1.getTargetException().getCause() != null ? e1.getTargetException().getCause()
              : e1.getTargetException();
          Throwable t2 = t1.getCause() != null ? t1.getCause() : null;

          CloudBeesUIPlugin.showError("Failed to validate your account.", t1.getMessage(), t2);
        } catch (InterruptedException e1) {
          CloudBeesUIPlugin.showError("Failed to validate your account.", e1);
        }

      }
    });
    Composite signupAndValidateRow = new Composite(groupInnerComp, SWT.NONE);
    GridData signupRowData = new GridData(GridData.FILL_HORIZONTAL);
    signupRowData.horizontalSpan = 2;

    signupAndValidateRow.setLayoutData(signupRowData);
    GridLayout gl2 = new GridLayout(2, false);
    gl2.marginWidth = 0;
    gl2.marginHeight = 0;
    gl2.marginTop = 5;
    signupAndValidateRow.setLayout(gl2);

    createSignUpLink(signupAndValidateRow);

  }

  private void createAttachJenkinsLink(Composite parent) {
    final Link link = new Link(parent, SWT.NONE);
    link.setText(Messages.pref_attach_jenkins);
    link.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        PreferencesUtil.createPreferenceDialogOn(link.getShell(),
            "com.cloudbees.eclipse.ui.preferences.JenkinsInstancesPreferencePage", null, null);
      }
    });
    String linktooltip = Messages.pref_attach_jenkins_tooltip;
    link.setToolTipText(linktooltip);
  }

  private void createSignUpLink(Composite parent) {
    final Link link = new Link(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
    
    GridData layoutData = new GridData(GridData.FILL_BOTH);
    layoutData.widthHint = 130;
    
    link.setLayoutData(layoutData);
    link.setText(Messages.pref_signup);
    link.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        CloudBeesUIPlugin.getDefault()
        .openWithBrowser("https://grandcentral." + GrandCentralService.HOST + "/account/"+e.text);        
      }
    });
    String linktooltip = Messages.pref_signup_tooltip;
    link.setToolTipText(linktooltip);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench) {
  }

  @Override
  public boolean performOk() {
    boolean res = super.performOk();

    // Call programmatically as SecurePreferences does not provide change listeners    
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Validating user credentials") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {
        try {
          monitor.beginTask("Validating user credentials", 1000);
          CloudBeesUIPlugin.getDefault().fireSecureStorageChanged();
        } catch (CloudBeesException e) {
          CloudBeesUIPlugin.getDefault().getLogger().error(e);
        } finally {
          monitor.done();
        }
        return Status.OK_STATUS;
      }
    };

    job.setUser(false);
    job.schedule();

    return res;
  }
}
