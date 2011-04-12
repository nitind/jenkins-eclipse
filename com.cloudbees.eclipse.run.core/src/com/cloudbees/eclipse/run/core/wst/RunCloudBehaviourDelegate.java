package com.cloudbees.eclipse.run.core.wst;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;

public class RunCloudBehaviourDelegate extends ServerBehaviourDelegate {

  public RunCloudBehaviourDelegate() {
  }

  @Override
  public void stop(boolean force) {
    try {
      String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      BeesSDK.stop(project);
    } catch (Exception e) {
      CBRunCoreActivator.logError(e);
    }
    setServerState(IServer.STATE_STOPPED);
  }

  @Override
  public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
    super.startModule(module, monitor);
  }

  @Override
  protected void initialize(IProgressMonitor monitor) {
    try {
      String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

      setState(BeesSDK.getServerState(project).getStatus());
    } catch (Exception e) {
      CBRunCoreActivator.logError(e);
      setServerState(IServer.STATE_UNKNOWN);
    }
  }

  private void setState(String status) {
    if ("stopped".equals(status)) {
      setServerState(IServer.STATE_STOPPED);
    } else if ("active".equals(status) || "hibernate".equals(status)) {
      setServerState(IServer.STATE_STARTED);
    } else {
      setServerState(IServer.STATE_UNKNOWN);
    }
  }

  @Override
  public IStatus publish(int kind, IProgressMonitor monitor) {
    try {
      if (getServer().getServerState() != IServer.STATE_STARTED || kind == IServer.PUBLISH_CLEAN
          || kind == IServer.PUBLISH_AUTO) {
        return null;
      }

      String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

      ApplicationDeployArchiveResponse deploy = BeesSDK.deploy(project, true);
      setServerPublishState(IServer.PUBLISH_STATE_NONE);
      setServerState(IServer.STATE_STARTED);
      return null;
    } catch (Exception e) {
      return new Status(IStatus.ERROR, CBRunCoreActivator.PLUGIN_ID, e.getMessage(), e);
    }
  }

  @Override
  public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor)
      throws CoreException {
    try {
      String projectName = getServer().getAttribute(CBLaunchConfigurationConstants.PROJECT, "");
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      BeesSDK.start(project);
    } catch (Exception e) {
      CBRunCoreActivator.logError(e);
    }
    setServerState(IServer.STATE_STARTED);
  }
}
