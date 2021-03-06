gwt-oauth2
==========

* [gwt-oauth2](gwt-oauth2) : The core library
* [sample](sample) : A demo application

Live demo: [https://gwt-oauth2.appspot.com/](https://gwt-oauth2.appspot.com/)

Quick start
-------------

1\. Add the Maven denpendency
```xml
<dependency>
  <groupId>io.github.freddyboucher</groupId>
  <artifactId>gwt-oauth2</artifactId>
  <version>0.3</version>
  <scope>provided</scope>
</dependency>
```

2\. Add `<inherits name="io.github.freddyboucher.gwt.oauth2.OAuth2"/>` to your GWT module XML file.

3\. Use it as follow:
```java
public class App implements EntryPoint {
  @Override
  public void onModuleLoad() {
    Button button = new Button("Google", (ClickHandler) event -> {
      AuthRequest req =
          new AuthRequest("https", "accounts.google.com", "o/oauth2/auth", "GOOGLE_CLIENT_ID")
              .setParameter("scope", "email profile openid");
      Auth.get().login(req, new Callback<Map<String, String>, Throwable>() {
        @Override
        public void onFailure(Throwable reason) {
          GWT.log(null, reason);
        }

        @Override
        public void onSuccess(Map<String, String> result) {
          String token = result.get("access_token");
          GWT.log(token);
        }
      }, "access_token");
    });
    RootPanel.get().add(button);
  }
}
```

Contributing
-------------

Code must pass [Google Java Style Guide](https://checkstyle.sourceforge.io/styleguides/google-java-style-20180523/javaguide.html).