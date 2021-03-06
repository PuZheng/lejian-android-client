package com.puzheng.lejian;

import android.content.Intent;
import android.support.v4.app.ListFragment;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.puzheng.deferred.Deferrable;
import com.puzheng.deferred.DoneHandler;
import com.puzheng.lejian.model.SPU;

import java.util.List;

public class SPUListFragment extends ListFragment {
    private Deferrable<List<SPU>, Pair<String, String>> deferrable;
    private boolean inited;

    public SPUListFragment() {

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        SPU spu = (SPU) getListAdapter().getItem(position);
        Logger.json(new Gson().toJson(spu));
        Intent intent = new Intent(getActivity(), SPUActivity.class);
        intent.putExtra(Const.TAG_SPU, spu);
        getActivity().startActivity(intent);
    }

    public SPUListFragment setDeferrable(Deferrable<List<SPU>, Pair<String, String>> deferrable) {
        this.deferrable = deferrable;

        return this;
    }

    public void init() {
        if (!inited) {
            deferrable.done(new DoneHandler<List<SPU>>() {
                @Override
                public void done(List<SPU> spus) {
                    Logger.i("spus loaded");
                    Logger.json(new Gson().toJson(spus));
                    if (spus != null && spus.size() == 0) {
                        setEmptyText(getString(R.string.no_result_found));
                    }
                    setListAdapter(new SPUListActivity.SPUListAdapter(getActivity(), spus));
                }
            });
            inited = true;
        }
    }

    public static class Builder {
        private Deferrable<List<SPU>, Pair<String, String>> deferrable;

        public Builder() {

        }

        public Builder deferred(Deferrable<List<SPU>, Pair<String, String>> deferrable) {
            this.deferrable = deferrable;
            return this;
        }

        public SPUListFragment build() {
            return new SPUListFragment().setDeferrable(deferrable);
        }
    }
}