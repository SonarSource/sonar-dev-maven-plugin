/*
 * SonarQube Development Maven Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.dev;

import com.github.kevinsawicki.http.HttpRequest;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Uploads the plugin artifact to server. Requires SonarQube 4.3 or greater.
 *
 * <ol>
 *   <li>Enable the development mode on server : add sonar.dev=true to conf/sonar.properties</li>
 *   <li>Restart server</li>
 *   <li>Build and upload the plugin : <code>mvn package org.codehaus.sonar:sonar-dev-maven-plugin::upload</code>.
 *   It requires the properties sonarHome (path to local server installation) and sonarUrl (default value
 *   is http://localhost:9000)
 *   </li>
 * </ol>
 *
 * @goal upload
 */
public class UploadMojo extends AbstractMojo {

  /**
   * Home directory of SonarQube local installation.
   *
   * @parameter property="sonarHome"
   * @required
   */
  private File sonarHome;

  /**
   * Server URL
   *
   * @parameter property="sonarUrl" default-value="http://localhost:9000"
   * @required
   */
  private URL sonarUrl;

  /**
   * Directory containing the build files.
   *
   * @parameter expression="${project.build.directory}/${project.build.finalName}.jar"
   */
  private File jar;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    checkSettings();
    copyJar();
    restartServer();
  }

  private void checkSettings() throws MojoExecutionException {
    if (!jar.isFile() && !jar.exists()) {
      throw new MojoExecutionException("Plugin file does not exist: " + jar.getAbsolutePath());
    }
    if (!sonarHome.isDirectory() && !sonarHome.exists()) {
      throw new MojoExecutionException("Server home directory does not exist: " + sonarHome.getAbsolutePath());
    }
    File confFile = new File(sonarHome, "conf/sonar.properties");
    if (!confFile.isFile() && !confFile.exists()) {
      throw new MojoExecutionException("Not a valid server home directory: " + sonarHome.getAbsolutePath());
    }
  }

  private void copyJar() throws MojoExecutionException {
    getLog().info("Copying JAR");
    File downloadsDir = new File(sonarHome, "extensions/downloads");
    downloadsDir.mkdir();
    try {
      FileUtils.copyFileToDirectory(jar, downloadsDir);
    } catch (IOException e) {
      throw new MojoExecutionException(String.format("Fail to copy %s to %s", jar.getAbsolutePath(), downloadsDir), e);
    }
  }

  private void restartServer() throws MojoExecutionException {
    getLog().info("Restarting server");
    try {
      HttpRequest request = HttpRequest.post(new URL(sonarUrl, "/api/system/restart"));
      if (!request.noContent()) {
        throw new MojoExecutionException("Fail to restart server " + sonarUrl + ": " + request.message());
      }
    } catch (MalformedURLException e) {
      throw new MojoExecutionException("Invalid URL", e);
    }
    getLog().info("Server restarted");
  }
}
