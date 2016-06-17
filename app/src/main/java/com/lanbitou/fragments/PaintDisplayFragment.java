package com.lanbitou.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lanbitou.R;
import com.lanbitou.adapters.PaintDisplayAdapter;

/**
 * Created by Henvealf on 16-5-13.
 */
public class PaintDisplayFragment extends Fragment {

    private ListView listView;
    private PaintDisplayAdapter adapter;
    private int uid;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_paint_display, container,false);
        getActivity().setTitle(R.string.paintTitle);

        SharedPreferences preferences = getActivity().getSharedPreferences("lanbitou", Context.MODE_PRIVATE);
        uid = preferences.getInt("uid", -1);

        listView = (ListView) view.findViewById(R.id.paint_display_lv);
        adapter = new PaintDisplayAdapter(getActivity(),uid);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("lanbitou", "requestCode" + requestCode + "");
        adapter.setImagePathList();
        adapter.notifyDataSetChanged();
    }


}
