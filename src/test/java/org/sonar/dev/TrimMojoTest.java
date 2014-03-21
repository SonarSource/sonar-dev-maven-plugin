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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

public class TrimMojoTest {

  @Test
  public void trimFilesFromDirectory() throws IOException, MojoFailureException, MojoExecutionException {
    File dir = newDir("trimFilesFromDirectory");
    File file1 = copyResourceToDir(1, dir);
    File file2 = copyResourceToDir(2, dir);

    TrimMojo mojo = new TrimMojo();
    mojo.setDirectory(dir);
    mojo.execute();

    assertTrimmed(file1);
    assertTrimmed(file2);
  }

  @Test
  public void excludeSomeFiles() throws IOException, MojoFailureException, MojoExecutionException {
    File dir = newDir("excludeSomeFiles");
    File file1 = copyResourceToDir(1, dir);
    File file2 = copyResourceToDir(2, dir);

    TrimMojo mojo = new TrimMojo();
    mojo.setDirectory(dir);
    mojo.setExcludes(new String[]{"**/*-1.txt"});
    mojo.execute();

    assertNotTrimmed(file1);
    assertTrimmed(file2);
  }

  @Test
  public void trimOnlySomeFiles() throws IOException, MojoFailureException, MojoExecutionException {
    File dir = newDir("trimOnlySomeFiles");
    File file1 = copyResourceToDir(1, dir);
    File file2 = copyResourceToDir(2, dir);

    TrimMojo mojo = new TrimMojo();
    mojo.setDirectory(dir);
    mojo.setIncludes(new String[]{"**/*-1.txt"});
    mojo.execute();

    assertTrimmed(file1);
    assertNotTrimmed(file2);
  }

  private void assertNotTrimmed(File file) throws IOException {
    String content = FileUtils.readFileToString(file);
    assertThat(content).startsWith("         ");
    assertThat(content).contains("            ");
  }

  private void assertTrimmed(File file) throws IOException {
    String content = FileUtils.readFileToString(file);
    assertThat(content).startsWith("many spaces");
    assertThat(content).doesNotContain("            ");
    assertThat(content).contains("white spaces should be  kept  in   the   line");
  }


  private File copyResourceToDir(int index, File dir) throws IOException {
    File file = new File(dir, "whitespace-indented-" + index + ".txt");
    FileUtils.copyURLToFile(getClass().getResource("/org/sonar/dev/TrimMojoTest/whitespace-indented.txt"), file);
    return file;
  }

  private File newDir(String name) throws IOException {
    File dir = new File("target/tmp/org/sonar/dev/TrimMojoTest/" + name);
    FileUtils.forceMkdir(dir);
    FileUtils.cleanDirectory(dir);
    return dir;
  }
}
