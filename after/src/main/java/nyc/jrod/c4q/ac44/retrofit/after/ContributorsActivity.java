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

public class ContributorsActivity extends AppCompatActivity {

  private ContributorAdapter contributorAdapter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_contributors_activity);

    getSupportActionBar().setTitle("After: Contributors");

    RecyclerView contributorsView = findViewById(R.id.contributors);
    contributorsView.setLayoutManager(new LinearLayoutManager(this));

    contributorAdapter = new ContributorAdapter();
    contributorsView.addItemDecoration(new DividerItemDecoration(this, HORIZONTAL));
    contributorsView.setAdapter(contributorAdapter);

    Retrofit retrofit = new Retrofit.Builder() //
        .baseUrl("https://api.github.com/") //
        .addConverterFactory(GsonConverterFactory.create())
        .build();

    GithubService githubService = retrofit.create(GithubService.class);

    Intent intent = getIntent();
    String userName = intent.getStringExtra("USER_NAME");
    String repoName = intent.getStringExtra("REPO_NAME");

    Call<List<Contributor>> call = githubService.getContributors(userName, repoName);
    call.enqueue(new Callback<List<Contributor>>() {
      @Override
      public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
        List<Contributor> contributors = response.body();
        contributorAdapter.setContributors(contributors);
      }

      @Override public void onFailure(Call<List<Contributor>> call, Throwable t) {
        Toast.makeText(ContributorsActivity.this, "Failed to get contributors", Toast.LENGTH_SHORT)
            .show();
      }
    });
  }

  private class ContributorAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<Contributor> contributors;

    ContributorAdapter() {
      this.contributors = Collections.emptyList();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new ViewHolder(LayoutInflater.from(parent.getContext())
          .inflate(R.layout.contributor_view, parent, false));
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
      Contributor contributor = contributors.get(position);
      holder.nameView.setText(contributor.login);
      holder.commitsView.setText(String.valueOf(contributor.contributions));
      Picasso.with(ContributorsActivity.this)
          .load(contributor.avatar_url) // TODO fix this
          .placeholder(R.mipmap.ic_launcher)
          .resizeDimen(R.dimen.list_detail_image_size, R.dimen.list_detail_image_size)
          .centerInside()
          .into(holder.imageView);
    }

    @Override public int getItemCount() {
      return contributors.size();
    }

    public void setContributors(List<Contributor> data) {
      this.contributors = data;
      notifyDataSetChanged();
    }
  }

  private class ViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView nameView;
    TextView commitsView;

    ViewHolder(View itemView) {
      super(itemView);
      imageView = itemView.findViewById(R.id.contributor_photo);
      nameView = itemView.findViewById(R.id.contributor_name);
      commitsView = itemView.findViewById(R.id.commits);
    }
  }
}
