package com.lanbitou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.lanbitou.R;
import com.lanbitou.adapters.NoteAdapter;
import com.lanbitou.adapters.NoteBookAdapter;
import com.lanbitou.entities.NoteBookEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Henvealf on 16-5-13.
 */
public class AllNotesFragment extends Fragment{

    private TextView textView;
    private ListView listView;
    private List<NoteBookEntity> listItems = new ArrayList<NoteBookEntity>();
    private NoteBookEntity noteBookEntity;
    private NoteBookAdapter noteBookAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_all_notes, container,false);

        textView = (TextView) view.findViewById(R.id.textview);
        listView = (ListView) view.findViewById(R.id.listview);

        listItems.clear();
        noteBookAdapter = new NoteBookAdapter(this.getActivity(), listItems);
        listView.setAdapter(noteBookAdapter);






        return view;
    }
}
