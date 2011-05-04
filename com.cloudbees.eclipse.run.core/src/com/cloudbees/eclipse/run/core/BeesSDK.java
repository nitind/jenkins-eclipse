package com.cloudbees.eclipse.run.core;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import org.apache.tools.ant.listener.TimestampedLogger;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.api.ApplicationStatusResponse;
import com.cloudbees.api.BeesClient;
import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.GrandCentralService.AuthInfo;
import com.cloudbees.eclipse.run.sdk.CBSdkActivator;

public class BeesSDK {

  public static final String API_URL = "https://api.cloudbees.com/api";

  public static ApplicationListResponse getList() throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    return client.applicationList();
  }

  public static ApplicationInfo getServerState(final IProject project) throws Exception {
    String id = project.getName();
    return getServerState(id);
  }

  public static ApplicationInfo getServerState(final String id) throws CloudBeesException, Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);

    String appId = grandCentralService.getCachedPrimaryUser(false) + "/" + id;//$NON-NLS-1$

    return client.applicationInfo(appId);
  }

  //  public static ApplicationStatusResponse stop(final IProject project) throws Exception {
  //    return stop(project.getName());
  //  }

  public static ApplicationStatusResponse stop(final String id) throws CloudBeesException, Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    return stop(grandCentralService.getCachedPrimaryUser(false), id);
  }

  public static ApplicationStatusResponse stop(final String accountName, final String id) throws CloudBeesException,
      Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    return client.applicationStop(accountName + "/" + id);
  }

  //  public static ApplicationStatusResponse start(final IProject project) throws Exception {
  //    return start(project.getName());
  //  }

  public static ApplicationStatusResponse start(final String id) throws CloudBeesException, Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    return start(grandCentralService.getCachedPrimaryUser(false), id);
  }

  public static ApplicationStatusResponse start(final String accountName, final String id) throws CloudBeesException,
      Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    return client.applicationStart(accountName + "/" + id);
  }

  public static ApplicationDeployArchiveResponse deploy(final IProject project, final boolean build) throws Exception {
    return deploy(project, project.getName(), build);
  }

  public static ApplicationDeployArchiveResponse deploy(final IProject project, final String id, final boolean build)
      throws CloudBeesException, CoreException, FileNotFoundException, Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);

    String appId = grandCentralService.getCachedPrimaryUser(false) + "/" + id;//$NON-NLS-1$

    IPath workspacePath = project.getLocation().removeLastSegments(1);
    IPath buildPath = getWarFile(project, build).getFullPath();

    String warFile = workspacePath.toOSString() + buildPath.toOSString();
    return client.applicationDeployWar(appId, null, null, warFile, null, null);
  }

  public static ApplicationDeployArchiveResponse deploy(final String appId, final String warUrl)
      throws CloudBeesException, CoreException, FileNotFoundException, Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    return client.applicationDeployWar(appId, null, null, warUrl, null, null);
  }

  /**
   * Establishes a persistent connection to an application log so that you can see new messages as they are written to
   * the logs. This is provides a "cloud-friendly" replacement for the ubiquitous "tail" command many developers use to
   * monitor/debug application log files.
   * 
   * @param appId
   * @param logName
   *          valid options are "server", "access" or "error"
   * @param outputStream
   * @throws Exception
   */
  public static void tail(final String appId, final String logName, final OutputStream outputStream) throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    getBeesClient(grandCentralService).tailLog(appId, logName, outputStream);
  }

  /**
   * Delete an application
   * 
   * @param appId
   * @throws Exception
   */
  public static void delete(final String appId) throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    getBeesClient(grandCentralService).applicationDelete(appId);
  }

  private static IFile getWarFile(final IProject project, final boolean build) throws CloudBeesException,
      CoreException, FileNotFoundException {
    if (build) {
      runTargets(project, new String[] { "dist" });
    }
    IFile file = getBuildFolder(project).getFile("webapp.war");

    if (!file.exists()) {
      runTargets(project, new String[] { "dist" });
      file.refreshLocal(IFile.DEPTH_INFINITE, null);

      if (!file.exists()) {
        throw new FileNotFoundException("Could not find webapp.war file in build folder .");
      }
    }

    return file;
  }

  private static IFolder getBuildFolder(final IProject project) throws CloudBeesException, CoreException,
      FileNotFoundException {

    IFolder folder = project.getFolder("build");
    if (!folder.exists()) {
      runTargets(project, new String[] { "dist" });
      folder.refreshLocal(IFile.DEPTH_INFINITE, null);

      if (!folder.exists()) {
        throw new FileNotFoundException(
            "Unexpected project structure. Could not find folder \"build\" in project root.");
      }
    }

    return folder;
  }

  private static BeesClient getBeesClient(final GrandCentralService grandCentralService) throws CloudBeesException {
    AuthInfo cachedAuthInfo = grandCentralService.getCachedAuthInfo(false);

    String api_key = cachedAuthInfo.getAuth().api_key;
    String secret_key = cachedAuthInfo.getAuth().secret_key;

    BeesClientConfiguration conf = new BeesClientConfiguration(API_URL, api_key, secret_key, "xml", "1.0");
    BeesClient client = new BeesClient(conf);
    return client;
  }

  private static void runTargets(final IProject project, final String[] targets) throws CloudBeesException,
      CoreException {
    AntRunner runner = new AntRunner();

    runner.setBuildFileLocation(getBuildXmlPath(project));
    runner.setExecutionTargets(targets);

    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    AuthInfo cachedAuthInfo = grandCentralService.getCachedAuthInfo(false);

    String secretKey = "-Dbees.apiSecret=" + cachedAuthInfo.getAuth().secret_key;//$NON-NLS-1$
    String authKey = " -Dbees.apiKey=" + cachedAuthInfo.getAuth().api_key;//$NON-NLS-1$
    String appId = " -Dbees.appid=" + grandCentralService.getCachedPrimaryUser(false) + "/" + project.getName();//$NON-NLS-1$
    String beesHome = " -Dbees.home=" + CBSdkActivator.getDefault().getBeesHome();
    runner.setArguments(secretKey + authKey + appId + beesHome);

    runner.addBuildLogger(TimestampedLogger.class.getName());
    runner.run();
  }

  /**
   * Construct full path for the build.xml
   * 
   * @param project
   * @return
   */
  private static String getBuildXmlPath(final IProject project) {
    IPath workspacePath = project.getLocation().removeLastSegments(1);
    IPath buildPath = project.getFile("build.xml").getFullPath();

    return workspacePath.toOSString() + buildPath.toOSString();
  }

}
