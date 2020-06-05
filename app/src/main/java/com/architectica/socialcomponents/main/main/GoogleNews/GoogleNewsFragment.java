package com.architectica.socialcomponents.main.main.GoogleNews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.architectica.socialcomponents.R;
import com.architectica.socialcomponents.adapters.NewsAdapter;
import com.architectica.socialcomponents.api.ApiClient;
import com.architectica.socialcomponents.api.ApiInterface;
import com.architectica.socialcomponents.model.Article;
import com.architectica.socialcomponents.model.News;
import com.architectica.socialcomponents.utils.NewsUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoogleNewsFragment extends Fragment {

    private static final String API_KEY = "a7e67aca85f340ed93494bdb0151cfa5";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private NewsAdapter newsAdapter;
    String source="techcrunch";
    //private String TAG = GoogleNewsActivity.class.getSimpleName();

    public static Fragment newInstance() {

        Fragment frag = new GoogleNewsFragment();

        Bundle args = new Bundle();

        frag.setArguments(args);

        return frag;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_google_news, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        LoadJson();

        return view;

    }

    public void LoadJson(){

        articles.clear();

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        String country = NewsUtils.getCountry();

        Call<News> call;
        call = apiInterface.getNews(source,API_KEY);

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {

                if (response.isSuccessful() && response.body().getArticle() != null){

                    articles = response.body().getArticle();

                    newsAdapter = new NewsAdapter(articles,getActivity());
                    recyclerView.setAdapter(newsAdapter);
                    newsAdapter.notifyDataSetChanged();

                }
                else {

                    Toast.makeText(getActivity(), "No Result", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {

            }
        });

    }

}
