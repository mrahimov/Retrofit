package nyc.jrod.c4q.ac44.retrofit.before;

public class Repository {
  public final String repoName;
  public final String owner;
  public final String avatarUrl;
  public final int watchers;

  public Repository(String repoName, String owner, String avatarUrl, int watchers) {
    this.repoName = repoName;
    this.owner = owner;
    this.avatarUrl = avatarUrl;
    this.watchers = watchers;
  }
}
