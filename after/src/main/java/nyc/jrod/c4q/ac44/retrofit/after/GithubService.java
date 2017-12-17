package nyc.jrod.c4q.ac44.retrofit.after;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

interface GithubService {
  @GET("users/{user}/repos?sort=updated") //
  Call<List<Repository>> getRepositories(@Path("user") String user);

  @GET("repos/{user}/{repo}/contributors") //
  Call<List<Contributor>> getContributors( //
      @Path("user") String user, @Path("repo") String repo);
}

class Repository {
  String name;
  int watchers;
  Owner owner;
}

class Owner {
  String login;
  String avatar_url;
}

class Contributor {
  String login;
  String avatar_url;
  int contributions;
}
