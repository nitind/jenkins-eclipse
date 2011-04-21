package com.cloudbees.eclipse.dev.ui.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class FavouritesUtils {

  private static final int MARGIN = 10;

  private static final String FAVOURITES_LIST = "FAVOURITES_LIST";

  public static final String FAVOURITES = "favourites://";//$NON-NLS-1$

  private static final IPreferenceStore prefs = CloudBeesDevUiPlugin.getDefault().getPreferenceStore();

  private static LinkedList<Shell> shells = new LinkedList<Shell>();

  private static final Color gradientStart = new Color(Display.getDefault(), 255, 255, 200);
  private static final Color gradientEnd = new Color(Display.getDefault(), 225, 225, 200);

  public static JenkinsJobsResponse getFavouritesResponse(final IProgressMonitor monitor) throws CloudBeesException {
    List<String> favourites = getFavourites();
    HashMap<String, JenkinsService> instances = getInstances(favourites);

    ArrayList<Job> filtered = new ArrayList<JenkinsJobsResponse.Job>();

    for (String instance : instances.keySet()) {
      JenkinsService jenkinsService = instances.get(instance);
      JenkinsJobsResponse allJobs = jenkinsService.getJobs(instance, monitor);
      for (Job j : allJobs.jobs) {
        if (favourites.contains(j.url)) {
          filtered.add(j);
        }
      }
    }

    JenkinsJobsResponse jobs = new JenkinsJobsResponse();
    jobs.name = "Favourite jobs";
    jobs.viewUrl = FAVOURITES;
    jobs.jobs = filtered.toArray(new Job[filtered.size()]);

    return jobs;
  }

  private static HashMap<String, JenkinsService> getInstances(List<String> favourites) {
    HashMap<String, JenkinsService> instances = new HashMap<String, JenkinsService>();

    for (String string : favourites) {
      String instance = string.split("job")[0];

      if (instances.get(instance) == null) {
        instances.put(instance, getService(instance));
      }
    }
    return instances;
  }

  private static JenkinsService getService(String instance) {
    List<JenkinsService> allJenkinsServices = CloudBeesUIPlugin.getDefault().getAllJenkinsServices();
    for (JenkinsService jenkinsService : allJenkinsServices) {

      if (instance.startsWith(jenkinsService.getUrl())) {
        return jenkinsService;
      }

    }
    return null;
  }

  private static ArrayList<String> getFavourites() {
    ArrayList<String> favourites = new ArrayList<String>();

    String pref = prefs.getString(FAVOURITES_LIST);

    if (!"".equals(pref)) { // we don't want an empty URL
      for (String string : pref.split(",")) {
        favourites.add(Utils.fromB64(string));
      }
    }

    return favourites;
  }

  private static void storeFavourites(ArrayList<String> favourites) {
    String pref = "";
    if (favourites != null && !favourites.isEmpty()) {
      for (String string : favourites) {
        pref += Utils.toB64(string) + ",";
      }

      pref = pref.substring(0, pref.length() - 1);
    }

    prefs.setValue(FAVOURITES_LIST, pref);
    try {
      CloudBeesDevUiPlugin.getDefault().showJobs(FAVOURITES, false);
    } catch (CloudBeesException e) {
      //TODO i18n
      CloudBeesUIPlugin.showError("Failed to reload Jenkins jobs!", e);
    }
  }

  public static void removeFavourite(String favourite) {
    ArrayList<String> fav = getFavourites();
    if (fav.contains(favourite)) {
      fav.remove(favourite);
      storeFavourites(fav);
    }
  }

  public static void addFavourite(String favourite) {
    ArrayList<String> fav = getFavourites();
    if (!fav.contains(favourite)) {
      fav.add(favourite);
      storeFavourites(fav);
    }
  }

  public static boolean isFavourite(String favourite) {
    return getFavourites().contains(favourite);
  }

  public static void showNotification(final Job job) {
    Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    Rectangle bounds = activeShell.getBounds();

    final Shell shell = new Shell(activeShell, SWT.NO_FOCUS);
    shell.setBackgroundMode(SWT.INHERIT_DEFAULT);

    shell.addListener(SWT.Resize, new Listener() {

      private Image oldImage;

      @Override
      public void handleEvent(Event e) {
        try {
          Rectangle rect = shell.getClientArea();
          Image newImage = new Image(Display.getDefault(), Math.max(1, rect.width), rect.height);
          GC gc = new GC(newImage);

          // fill background
          gc.setForeground(gradientStart);
          gc.setBackground(gradientEnd);
          gc.fillGradientRectangle(rect.x, rect.y, rect.width, rect.height, true);

          gc.dispose();

          shell.setBackgroundImage(newImage);

          if (this.oldImage != null) {
            this.oldImage.dispose();
          }
          this.oldImage = newImage;
        } catch (Exception e1) {
          CloudBeesUIPlugin.showError(e1.getMessage(), e1);
        }
      }
    });

    GridLayout layout = new GridLayout();
    layout.marginBottom = MARGIN;
    layout.marginTop = MARGIN;
    layout.marginLeft = MARGIN;
    layout.marginRight = MARGIN;
    shell.setLayout(layout);

    shell.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseUp(MouseEvent e) {
        shell.setVisible(false);
      }
    });
    Label label = new Label(shell, SWT.NO_BACKGROUND);

    String message = job.displayName + " " + job.url;
    if (job.lastBuild != null) {
      message = job.lastBuild.result + " " + message;
    }
    label.setText(message);
    label.setLayoutData(new GridData());
    label.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseUp(MouseEvent e) {
        CloudBeesDevUiPlugin.getDefault().showBuildForJob(job);
        shell.setVisible(false);
      }

    });
    final Cursor cursor = new Cursor(shell.getDisplay(), SWT.CURSOR_HAND);
    label.setCursor(cursor);
    shell.pack(true);
    int width = label.getBounds().width + 3 * MARGIN;
    int height = label.getBounds().height + 3 * MARGIN;

    shell.setBounds(bounds.x + bounds.width - width - 5, bounds.y + bounds.height - height - 5, width, height);
    for (Shell s : shells) {
      s.setLocation(s.getLocation().x, s.getLocation().y - height);
    }
    shells.addFirst(shell);
    shell.open();

    new Thread() {

      @Override
      public void run() {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          CloudBeesUIPlugin.showError(e.getMessage(), e);
        }
        shell.getDisplay().asyncExec(new Runnable() {

          @Override
          public void run() {
            shell.close();
            shells.remove(shell);
            cursor.dispose();
          }
        });
      }
    }.start();
  }

}
