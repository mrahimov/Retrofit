package nyc.jrod.c4q.ac44.retrofit.before;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;

public class SearchActivity extends AppCompatActivity {

  private EditText userView;
  private RepositoryAdapter repositoryAdapter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_search_activity);

    getSupportActionBar().setTitle("Before: Repository Search");

    userView = findViewById(R.id.user);
    Button searchView = findViewById(R.id.search);

    RecyclerView repositoriesView = findViewById(R.id.repos);
    repositoriesView.setLayoutManager(new LinearLayoutManager(this));

    repositoryAdapter = new RepositoryAdapter();
    repositoriesView.addItemDecoration(new DividerItemDecoration(this, HORIZONTAL));
    repositoriesView.setAdapter(repositoryAdapter);

    searchView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        String user = userView.getText().toString();
        if (user.isEmpty()) return;

        new GetRepositories().execute(user);
      }
    });
  }

  private class RepositoryAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<Repository> repositories;

    RepositoryAdapter() {
      this.repositories = Collections.emptyList();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new ViewHolder(
          LayoutInflater.from(parent.getContext()).inflate(R.layout.repo_view, parent, false));
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
      final Repository repository = repositories.get(position);
      holder.repoView.setText(repository.repoName);
      holder.watchersView.setText(String.valueOf(repository.watchers));
      Picasso.with(SearchActivity.this)
          .load(repository.avatarUrl)
          .placeholder(R.mipmap.ic_launcher)
          .resizeDimen(R.dimen.list_detail_image_size, R.dimen.list_detail_image_size)
          .centerInside()
          .into(holder.ownerView);
      holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          Intent launchIntent = new Intent(SearchActivity.this, ContributorsActivity.class);
          launchIntent.putExtra("USER_NAME", repository.owner);
          launchIntent.putExtra("REPO_NAME", repository.repoName);
          SearchActivity.this.startActivity(launchIntent);
        }
      });
    }

    @Override public int getItemCount() {
      return repositories.size();
    }

    void setRepositories(List<Repository> repositories) {
      this.repositories = repositories;
      notifyDataSetChanged();
    }
  }

  private class ViewHolder extends RecyclerView.ViewHolder {
    ImageView ownerView;
    TextView repoView;
    TextView watchersView;

    ViewHolder(View itemView) {
      super(itemView);
      ownerView = itemView.findViewById(R.id.owner_photo);
      repoView = itemView.findViewById(R.id.repo_name);
      watchersView = itemView.findViewById(R.id.watchers_count);
    }
  }

  class GetRepositories extends AsyncTask<String, Void, GetRepositories.Result> {

    @Override protected Result doInBackground(String... users) {
      String user = users[0];
      Result result;
      try {
        String apiUrl = "https://api.github.com/users/" + user + "/repos?sort=updated";
        URL url = new URL(apiUrl);

        // query API for repositories
        String jsonString = Utils.downloadUrl(url);
        if (jsonString != null) {

          // parse response
          return parseJson(jsonString);
        } else {
          throw new IOException("No response received.");
        }
      } catch (Exception e) {
        result = new Result(e);
      }
      return result;
    }

    private Result parseJson(String json) throws JSONException {
      List<Repository> repos = new ArrayList<>();

      JSONArray jsonRepos = (JSONArray) new JSONTokener(json).nextValue();
      for (int i = 0; i < jsonRepos.length(); i++) {
        JSONObject jsonRepo = jsonRepos.getJSONObject(i);
        String repoName = jsonRepo.getString("name");
        int watchers = jsonRepo.getInt("watchers");

        JSONObject jsonOwner = jsonRepo.getJSONObject("owner");
        String owner = jsonOwner.getString("login");
        String avatarUrl = jsonOwner.getString("avatar_url");

        repos.add(new Repository(repoName, owner, avatarUrl, watchers));
      }

      return new Result(repos);
    }

    @Override protected void onPostExecute(Result result) {
      if (result != null) {
        if (result.exception != null) {
          Toast.makeText(SearchActivity.this, "Failed to download repositories", Toast.LENGTH_SHORT)
              .show();
        } else if (result.result != null) {
          // populate recycler view
          repositoryAdapter.setRepositories(result.result);
        }
      }
    }

    class Result {
      List<Repository> result;
      Exception exception;

      Result(List<Repository> result) {
        this.result = result;
      }

      Result(Exception exception) {
        this.exception = exception;
      }
    }
  }
}
