package nyc.jrod.c4q.ac44.retrofit.before;

public class Contributor {
  public final String login;
  public final String avatarUrl;
  public final int contributions;

  public Contributor(String login, String avatarUrl, int contributions) {
    this.login = login;
    this.avatarUrl = avatarUrl;
    this.contributions = contributions;
  }
}
