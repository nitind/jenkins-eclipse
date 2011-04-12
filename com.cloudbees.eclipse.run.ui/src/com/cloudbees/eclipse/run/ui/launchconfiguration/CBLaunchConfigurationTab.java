package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;

public class CBLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

  private final class ProjectSelectionCompositeForLauncher extends ProjectSelectionComposite {
    private ProjectSelectionCompositeForLauncher(Composite parent, int style) {
      super(parent, style);
    }

    @Override
    public void handleUpdate() {
      IStatus status = validate();
      if (status.getSeverity() == IStatus.OK) {
        setErrorMessage(null);
        setMessage("Run CloudBees application");
      } else {
        setErrorMessage(status.getMessage());
      }
    }
  }

  private static final String TAB_NAME = "CloudBees Application";

  protected ProjectSelectionComposite content;
  protected Button launchButton;
  protected Composite main;

  @Override
  public void createControl(Composite parent) {
    this.main = new Composite(parent, SWT.NONE);
    this.main.setLayout(new GridLayout(2, false));
    this.content = new ProjectSelectionCompositeForLauncher(this.main, SWT.None);
    this.content.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    setControl(this.main);
    this.content.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        CBLaunchConfigurationTab.this.content.handleUpdate();
        updateLaunchConfigurationDialog();
      }

    });
    this.launchButton = new Button(this.main, SWT.CHECK);
    this.launchButton.setLayoutData(new GridData());
    this.launchButton.setSelection(true);
    Label label = new Label(this.main, SWT.NONE);
    label.setText("Open browser after launch.");

  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
  }

  @Override
  public void initializeFrom(ILaunchConfiguration configuration) {
    try {
      this.content.setText(configuration
          .getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, new String()));
    } catch (CoreException e) {
      CBRunUiActivator.logError(e);
    }
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    String projectName = this.content.getText();
    try {
      CBRunUtil.addDefaultAttributes(configuration, projectName);

      configuration.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_BROWSER,
          this.launchButton.getSelection());
    } catch (CoreException e) {
      CBRunUiActivator.logError(e);
    }
  }

  @Override
  public String getName() {
    return TAB_NAME;
  }

  @Override
  public Image getImage() {
    return CBRunUiActivator.getDefault().getImageRegistry().get(Images.CLOUDBEES_ICON_16x16);
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig) {
    return this.content.validate().getSeverity() == IStatus.OK;
  }

}
