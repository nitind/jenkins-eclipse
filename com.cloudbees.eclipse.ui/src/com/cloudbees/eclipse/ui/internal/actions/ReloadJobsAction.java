package com.cloudbees.eclipse.ui.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.ui.CBImages;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class ReloadJobsAction extends Action {

  public String serviceUrl;
  public String viewUrl;

  public ReloadJobsAction() {
    super();

    setText("Reload Nectar jobs...");
    setToolTipText("Reload Nectar jobs");
    setImageDescriptor(CloudBeesUIPlugin.getImageDescription(CBImages.IMG_REFRESH));

    setAccelerator(SWT.F5);

  }

  @Override
  public void runWithEvent(Event event) {

    try {
      CloudBeesUIPlugin.getDefault().showJobs(serviceUrl, viewUrl);
    } catch (CloudBeesException e) {
      //TODO I18n!
      CloudBeesUIPlugin.showError("Failed to reload Nectar jobs!", e);
    }

  }

}
