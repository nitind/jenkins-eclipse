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

  public String name;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(jobs);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
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
    if (!Arrays.equals(jobs, other.jobs)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Expose(deserialize = false, serialize = false)
  public final static String QTREE = QTreeFactory.create(JenkinsJobsResponse.class);

  public static class Job {
    public HealthReport[] healthReport;

    public String displayName;
    public Boolean inQueue;
    public String color;

    public String url;

    public Boolean buildable;

    public Build lastBuild;
    public Build lastCompletedBuild;
    public Build lastFailedBuild;
    public Build lastStableBuild;
    public Build lastSuccessfulBuild;
    public Build lastUnstableBuild;
    public Build lastUnsuccessfulBuild;

    public JenkinsJobProperty[] property;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((url == null) ? 0 : url.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
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
      if (url == null) {
        if (other.url != null) {
          return false;
        }
      } else if (!url.equals(other.url)) {
        return false;
      }
      return true;
    }

    public static class Build {
      public String fullDisplayName;
      public String url;
      public String builtOn;

      // Merged from regular build request, json tree can fetch this nicely
      public Boolean building;
      public Long duration;
      public Long number;
      public Long timestamp;

    }

  }

}
