package com.yofish.apollo.listener;

import com.google.common.base.Preconditions;
import com.yofish.apollo.domain.App;
import org.springframework.context.ApplicationEvent;

public class AppCreationEvent extends ApplicationEvent {

  public AppCreationEvent(Object source) {
    super(source);
  }

  public App getApp() {
    Preconditions.checkState(source != null);
    return (App) this.source;
  }
}
