/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.github.freddyboucher.gwt.oauth2.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.storage.client.Storage;
import java.util.Map;

/**
 * Real implementation of {@link Auth}, used in real GWT applications.
 *
 * @author jasonhall@google.com (Jason Hall)
 */
class AuthImpl extends Auth {

  static final AuthImpl INSTANCE = new AuthImpl();

  private Window window;

  AuthImpl() {
    super(getTokenStore(), new RealClock(), Scheduler.get());
    register();
  }

  /**
   * Returns the correct {@link TokenStore} implementation to use based on browser support for
   * localStorage.
   */
  // TODO(jasonhall): This will not result in CookieStoreImpl being compiled out
  // for browsers that support localStorage, and vice versa? If not, this should
  // be a deferred binding rule.
  private static TokenStore getTokenStore() {
    return Storage.isLocalStorageSupported() ? new TokenStoreImpl() : new CookieStoreImpl();
  }

  /**
   * Register a global function to receive auth responses from the popup window.
   */
  private native void register() /*-{
    var self = this;
    if (!$wnd.oauth2) {
      $wnd.oauth2 = {};
    }
    $wnd.oauth2.__doLogin = $entry(function (response) {
      self.@io.github.freddyboucher.gwt.oauth2.client.Auth::finish(Ljava/lang/String;)(response);
    });
  }-*/;

  /**
   * Get the OAuth 2.0 token for which this application may not have already been granted access, by
   * displaying a popup to the user.
   */
  @Override
  void doLogin(String authUrl, Callback<Map<String, String>, Throwable> callback) {
    if (null != window && window.isOpen()) {
      callback.onFailure(new IllegalStateException("Authentication in progress"));
    } else {
      window = openWindow(authUrl, height, width);
      if (null == window) {
        callback.onFailure(
            new RuntimeException("The authentication popup window appears to have been blocked"));
      }
    }
  }

  @Override
  void finish(String response) {
    // Clean up the popup
    if (null != window && window.isOpen()) {
      window.close();
    }
    super.finish(response);
  }

  // Because GWT's Window.open() method does not return a reference to the
  // newly-opened window, we have to manage this all ourselves manually...
  private static native Window openWindow(String url, int height, int width) /*-{
    return $wnd.open(url, 'popupWindow', 'width=' + width + ',height=' + height);
  }-*/;

  private static final class Window extends JavaScriptObject {

    @SuppressWarnings("unused")
    protected Window() {
    }

    native boolean isOpen() /*-{
      return !this.closed;
    }-*/;

    native void close() /*-{
      this.close();
    }-*/;
  }

  /**
   * Real GWT implementation of Clock.
   */
  private static class RealClock implements Clock {

    @Override
    public double now() {
      return Duration.currentTimeMillis();
    }
  }
}
