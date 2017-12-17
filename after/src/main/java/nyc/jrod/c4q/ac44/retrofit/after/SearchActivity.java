package nyc.jrod.c4q.ac44.retrofit.after;

import android.content.Intent;
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
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;

public class SearchActivity extends AppCompatActivity {

  private EditText userView;
  private Button searchView;
  private GithubService githubService;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_search_activity);

    getSupportActionBar().setTitle("After: Repository Search");

    userView = findViewById(R.id.user);
    searchView = findViewById(R.id.search);

    RecyclerView repositoriesView = findViewById(R.id.repos);
    repositoriesView.setLayoutManager(new LinearLayoutManager(this));

    final RepositoryAdapter repositoryAdapter = new RepositoryAdapter();
    repositoriesView.addItemDecoration(new DividerItemDecoration(this, HORIZONTAL));
    repositoriesView.setAdapter(repositoryAdapter);

    Retrofit retrofit = new Retrofit.Builder() //
        .baseUrl("https://api.github.com/") //
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    githubService = retrofit.create(GithubService.class);

    searchView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        String user = userView.getText().toString();
        if (user.isEmpty()) return;

        // query API for repositories
        Call<List<Repository>> call = githubService.getRepositories(user);

        call.enqueue(new Callback<List<Repository>>() {
          @Override
          public void onResponse(Call<List<Repository>> call, Response<List<Repository>> response) {
            // parse response
            List<Repository> repositories = response.body();

            // populate recycler view
            repositoryAdapter.setRepositories(repositories);
          }

          @Override public void onFailure(Call<List<Repository>> call, Throwable t) {
            Toast.makeText(SearchActivity.this, "Failed to get repositories", Toast.LENGTH_SHORT)
                .show();
          }
        });
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

      holder.repoView.setText(repository.name);
      holder.watchersView.setText(String.valueOf(repository.watchers));

      Picasso.with(SearchActivity.this)
          .load(repository.owner.avatar_url)
          .placeholder(R.mipmap.ic_launcher)
          .resizeDimen(R.dimen.list_detail_image_size, R.dimen.list_detail_image_size)
          .centerInside()
          .into(holder.ownerView);

      holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          Intent launchIntent = new Intent(SearchActivity.this, ContributorsActivity.class);
          launchIntent.putExtra("USER_NAME", repository.owner.login);
          launchIntent.putExtra("REPO_NAME", repository.name);
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
}
