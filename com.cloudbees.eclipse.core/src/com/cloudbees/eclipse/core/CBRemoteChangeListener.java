package com.cloudbees.eclipse.core;

import java.util.List;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;

public interface CBRemoteChangeListener {

  void activeJobViewChanged(JenkinsJobsResponse newView);

  void jenkinsChanged(List<JenkinsInstanceResponse> instances);

  void activeJobHistoryChanged(JenkinsJobAndBuildsResponse newView);

  void forgeChanged(List<ForgeInstance> instances);
  
  void activeAccountChanged(String email, String newAccountName);

  /**
   * Called when jenkins instance status could have been potentially changed. For example called whenever jobs list changed for this url 
   * 
   * @param viewUrl
   * @param online
   */
  void jenkinsStatusUpdate(String viewUrl, boolean online);
  
}