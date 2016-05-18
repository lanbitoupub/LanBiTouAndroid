package com.lanbitou.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lanbitou.R;
import com.lanbitou.entities.NoteEntity;

import java.util.List;


/**
 * Created by joyce on 16-5-12.
 */
public class NoteAdapter extends BaseAdapter{

    private List<NoteEntity> listItems;
    private LayoutInflater inflater;

    public NoteAdapter(Activity activity, List<NoteEntity> listItems) {
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
            convertView = inflater.inflate(R.layout.noteitem, null);
            TextView title = (TextView) convertView.findViewById(R.id.title);

            holder = new ViewHolder();
            holder.title = title;

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        NoteEntity pe = listItems.get(position);
        holder.title.setText(pe.getTitle());
        return convertView;
    }

    private class ViewHolder {
        TextView title;
    }
}
