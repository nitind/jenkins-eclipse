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
package com.cloudbees.eclipse.dev.ui.views.build;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;

import com.cloudbees.eclipse.core.jenkins.api.ChangeSetPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;


public class RecentChangesClickListener implements IDoubleClickListener {

  public void doubleClick(final DoubleClickEvent event) {
    JenkinsBuildDetailsResponse buildDetails = ((RecentChangesContentProvider) ((TreeViewer) event.getSource())
        .getContentProvider()).getBuildDetails();
    Object selection = ((TreeSelection) event.getSelection()).getFirstElement();
    if (!(selection instanceof ChangeSetPathItem)) {
      return;
    }
    final ChangeSetPathItem item = (ChangeSetPathItem) selection;

    String jobUrl = buildDetails.url;

    { // strip build number
      while (jobUrl.endsWith("/")) {
        jobUrl = jobUrl.substring(0, jobUrl.length() - 1);
      }

      int pos = jobUrl.lastIndexOf("/");
      jobUrl = jobUrl.substring(0, pos);
    }

    //System.out.println("Clicked: " + event + " - " + jobUrl + " -> " + " - " + item.path);

    CloudBeesDevUiPlugin.getDefault().openRemoteFile(jobUrl, item);
  }


}
