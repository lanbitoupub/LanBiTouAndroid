package com.lanbitou.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lanbitou.R;
import com.lanbitou.entities.NoteBookEntity;
import com.lanbitou.entities.NoteEntity;

import java.util.List;


/**
 * Created by joyce on 16-5-12.
 */
public class NoteBookAdapter extends BaseAdapter{

    private List<NoteBookEntity> listItems;
    private LayoutInflater inflater;

    public NoteBookAdapter(Activity activity, List<NoteBookEntity> listItems) {
        this.listItems = listItems;
        inflater = activity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null)
        {
            convertView = inflater.inflate(R.layout.notebookitem, null);
            TextView name = (TextView) convertView.findViewById(R.id.name);

            holder = new ViewHolder();
            holder.name = name;

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        NoteBookEntity pbe = listItems.get(position);
        holder.name.setText(pbe.getName());
        return convertView;
    }

    private class ViewHolder {
        TextView name;
    }
}
