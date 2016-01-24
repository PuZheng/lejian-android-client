package com.puzheng.lejian;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;

import com.puzheng.deferred.AlwaysHandler;
import com.puzheng.deferred.Deferrable;
import com.puzheng.deferred.DoneHandler;
import com.puzheng.deferred.FailHandler;
import com.puzheng.lejian.adapter.RecommendationListAdapter;
import com.puzheng.lejian.model.Recommendation;
import com.puzheng.lejian.model.SPU;
import com.puzheng.lejian.netutils.WebService;

import java.util.List;

/**
 * Created by xc on 13-11-21.
 */
public abstract class RecommendationFragment extends ListFragment implements RefreshInterface {

    private MaskableManager maskableManager;
    private com.puzheng.lejian.model.SPU spu;

    public RecommendationFragment() {

    }

    @Override

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Recommendation recommendation = (Recommendation) getListAdapter().getItem(position);
        Intent intent = new Intent(getActivity(), SPUActivity.class);
        intent.putExtra(Const.TAG_SPU_ID, recommendation.getSPUId());
        intent.putExtra(Const.TAG_SPU_NAME, recommendation.getProductName());
        startActivity(intent);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        maskableManager = new MaskableManager(getListView(), this);
        SPU spu = getArguments().getParcelable(Const.TAG_SPU);
        if (spu != null) {
            this.spu = spu;
            fetchRecommendations().done(new DoneHandler<List<SPU>>() {
                @Override
                public void done(List<SPU> spus) {
                    if (spus.size() != 0) {
                        setListAdapter(new RecommendationListAdapter(spus));
                    } else {
                        setEmptyText(getActivity().getString(R.string.no_recommendations));
                    }

                }
            }).fail(new FailHandler<Pair<String, String>>() {
                @Override
                public void fail(Pair<String, String> stringStringPair) {

                }
            }).always(new AlwaysHandler() {
                @Override
                public void always() {

                }
            });
        }
    }

    abstract public Deferrable<List<SPU>, Pair<String, String>> fetchRecommendations();

    @Override
    public void refresh() {

//        new GetRecommendationsTask(this, queryType, spuId).execute();
    }


    class GetRecommendationsTask extends AsyncTask<Void, Void, List<Recommendation>> {
        private Exception exception;
        private final ListFragment listFragment;
        private final String queryType;
        private final int spuId;

        GetRecommendationsTask(ListFragment listFragment, String queryType, int spuId) {
            this.listFragment = listFragment;
            this.queryType = queryType;
            this.spuId = spuId;
        }

        @Override
        protected List<Recommendation> doInBackground(Void... params) {
            try {
                return WebService.getInstance(getActivity()).getRecommendations(queryType, spuId);
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Recommendation> recommendations) {
//            if (maskableManager.unmask(exception)) {
//                if (recommendations != null && !recommendations.isEmpty()) {
//                    listFragment.setListAdapter(new RecommendationListAdapter(recommendations, getActivity(), (ImageFetcherInteface) getActivity()));
//                    return;
//                }else {
//                    listFragment.setEmptyText(listFragment.getString(R.string.search_no_result_found));
//                }
//            }
//            listFragment.setListAdapter(null);
        }

        @Override
        protected void onPreExecute() {
            maskableManager.mask();
        }
    }

}
