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
package com.cloudbees.eclipse.core.jenkins.api;

import java.util.Arrays;

import com.google.gson.annotations.Expose;

/**
 * Main response data
 * 
 * @author ahti
 */
public class JenkinsJobsResponse extends BaseJenkinsResponse {

  public Job[] jobs;

  public View[] views;

  public View primaryView;

  public String name;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(this.jobs);
    result = prime * result + (this.name == null ? 0 : this.name.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof JenkinsJobsResponse)) {
      return false;
    }
    JenkinsJobsResponse other = (JenkinsJobsResponse) obj;
    if (!Arrays.equals(this.jobs, other.jobs)) {
      return false;
    }
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Expose(deserialize = false, serialize = false)
  public final static String QTREE = QTreeFactory.create(JenkinsJobsResponse.class);

  public static class View implements JobViewGeneric {
    public String name;
    public String url;

    public Job[] jobs;

    //public String description;

    @Override
    public String toString() {
      return "View[" + name + "]";
    }

    public String getName() {
      return name;
    }

    public String getUrl() {
      return url;
    }

    public String getState() {
      return "v";
    }

    @Override
    public int hashCode() {
      final int prime = 311;
      int result = 1;
      result = prime * result + (this.url == null ? 0 : this.url.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof View)) {
        return false;
      }
      View other = (View) obj;
      if (this.url == null) {
        if (other.url != null) {
          return false;
        }
      } else if (!this.url.equals(other.url)) {
        return false;
      }
      return true;
    }

    public boolean isFolderOrView() {
      return true;
    }

  }

  public static class Job implements JobViewGeneric {
    public HealthReport[] healthReport;

    public String name;
    public String displayName;
    public Boolean inQueue;
    public String color;

    public String url;

    public Boolean buildable;

    public JenkinsBuild lastBuild;
    public JenkinsBuild lastCompletedBuild;
    public JenkinsBuild lastFailedBuild;
    public JenkinsBuild lastStableBuild;
    public JenkinsBuild lastSuccessfulBuild;
    public JenkinsBuild lastUnstableBuild;
    public JenkinsBuild lastUnsuccessfulBuild;

    public JenkinsJobProperty[] property;


    public String result;

    @Override
    public String toString() {
      return "Job[" + name + "]";
    }

    public String getName() {
      return name;
    }

    public String getUrl() {
      return url;
    }

    public String getDisplayName() {
      if (this.displayName != null) {
        return this.displayName;
      } else {
        return this.name;
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (this.url == null ? 0 : this.url.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof Job)) {
        return false;
      }
      Job other = (Job) obj;
      if (this.url == null) {
        if (other.url != null) {
          return false;
        }
      } else if (!this.url.equals(other.url)) {
        return false;
      }
      return true;
    }

    public String getState() {
      return color;
    }

    public boolean isFolderOrView() {
      return color == null;
    }

  }

  public interface JobViewGeneric {
    public String getName();

    public String getUrl();

    public String getState();

    public boolean isFolderOrView();
  }

}
