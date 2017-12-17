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

public class ContributorsActivity extends AppCompatActivity {
  private ContributorAdapter contributorAdapter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.layout_contributors_activity);

    getSupportActionBar().setTitle("Before: Contributors");

    RecyclerView contributorsView = findViewById(R.id.contributors);
    contributorsView.setLayoutManager(new LinearLayoutManager(this));

    contributorAdapter = new ContributorAdapter();
    contributorsView.addItemDecoration(new DividerItemDecoration(this, HORIZONTAL));
    contributorsView.setAdapter(contributorAdapter);

    Intent intent = getIntent();
    String userName = intent.getStringExtra("USER_NAME");
    String repoName = intent.getStringExtra("REPO_NAME");

    new GetContributors().execute(new InputParams(userName, repoName));
  }

  private class ContributorAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<Contributor> data;

    ContributorAdapter() {
      this.data = Collections.emptyList();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new ViewHolder(LayoutInflater.from(parent.getContext())
          .inflate(R.layout.contributor_view, parent, false));
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
      Contributor contributor = data.get(position);
      holder.nameView.setText(contributor.login);
      holder.commitsView.setText(String.valueOf(contributor.contributions));
      Picasso.with(ContributorsActivity.this)
          .load(contributor.avatarUrl)
          .placeholder(R.mipmap.ic_launcher)
          .resizeDimen(R.dimen.list_detail_image_size, R.dimen.list_detail_image_size)
          .centerInside()
          .into(holder.imageView);
    }

    @Override public int getItemCount() {
      return data.size();
    }

    public void setContributors(List<Contributor> data) {
      this.data = data;
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

  private class GetContributors extends AsyncTask<InputParams, Void, GetContributors.Result> {
    @Override protected Result doInBackground(InputParams... params) {
      InputParams param = params[0];
      Result result;
      try {
        String apiUrl = "https://api.github.com/repos/"
            + param.userName
            + "/"
            + param.repoName
            + "/contributors";

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
      List<Contributor> contributors = new ArrayList<>();

      JSONArray jsonContributors = (JSONArray) new JSONTokener(json).nextValue();
      for (int i = 0; i < jsonContributors.length(); i++) {
        JSONObject jsonContributor = jsonContributors.getJSONObject(i);
        String login = jsonContributor.getString("login");
        String avatarUrl = jsonContributor.getString("avatar_url");
        int contributions = jsonContributor.getInt("contributions");

        contributors.add(new Contributor(login, avatarUrl, contributions));
      }

      return new Result(contributors);
    }

    @Override protected void onPostExecute(Result result) {
      if (result != null) {
        if (result.exception != null) {
          Toast.makeText(ContributorsActivity.this, "Failed to download contributors",
              Toast.LENGTH_SHORT).show();
        } else if (result.result != null) {
          // populate recycler view
          contributorAdapter.setContributors(result.result);
        }
      }
    }

    class Result {
      List<Contributor> result;
      Exception exception;

      Result(List<Contributor> result) {
        this.result = result;
      }

      Result(Exception exception) {
        this.exception = exception;
      }
    }
  }

  private class InputParams {
    public final String userName;
    public final String repoName;

    public InputParams(String userName, String repoName) {
      this.userName = userName;
      this.repoName = repoName;
    }
  }
}
