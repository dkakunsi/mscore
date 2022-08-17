package com.devit.mscore;

/**
 * Root class for manager hierarchy.
 * 
 * @author dkakunsi
 */
public class Manager {

  protected Configuration configuration;

  protected Manager(Configuration configuration) {
    this.configuration = configuration;
  }
}
