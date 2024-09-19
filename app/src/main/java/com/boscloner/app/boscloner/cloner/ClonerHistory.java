package com.boscloner.app.boscloner.cloner;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.jpiat.boscloner.R;
import com.boscloner.app.boscloner.cloner.cards.CardId;
import com.boscloner.app.boscloner.cloner.cmd.CardIDData;
import com.boscloner.app.boscloner.cloner.cmd.ClonerCommand;
import com.boscloner.app.boscloner.cloner.cmd.CommandType;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by jpiat on 12/17/15.
 */
public class ClonerHistory extends BaseAdapter implements IClonerObserver{
    private static final int MAX_HISTORY_LENGTH = 256 ;

    private static final String history_file_name = "card_history.json" ;
    private Context context ;
    private static ClonerHistory single = null;
    private ArrayList<ClonerEvent> events ;
    private File historyFile ;


    private ClonerHistory(){
        this.events = new ArrayList<ClonerEvent>();
    }

    public static ClonerHistory getInstance(){
        if(single == null){
            single = new ClonerHistory();
        }
        return single ;
    }

    public ArrayList<ClonerEvent> getEvents(){
        return events ;
    }

    public void clearEvents(){
        this.events.clear();
        this.notifyDataSetChanged();
    }

    public void addEvent(ClonerEvent e){
        if(this.events.size() >= MAX_HISTORY_LENGTH){
            this.events.remove(0);
        }
        this.events.add(e);
    }

    public void setContext(Context context){
        if(this.context != null) try {
            exportDataTofile();
            this.events.clear();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.context = context ;
        File dir = context.getFilesDir() ;
        historyFile = new File(dir, history_file_name);
        try {
            importFileData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return this.events.size();
    }

    @Override
    public Object getItem(int position) {
        return this.events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        RowHolder holder;
        if(convertView == null) {
            view = LayoutInflater.from(this.context).inflate(R.layout.event_row_layout, parent, false);
            holder = new RowHolder();
            holder.row_text = (TextView)view.findViewById(R.id.row_text);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (RowHolder)view.getTag();
        }
        holder.row_text.setText(this.events.get(position).getShortDescription());
        /*view.setLongClickable(true);
        view.setOnLongClickListener((View.OnLongClickListener) this.context);*/
        return view;
    }


    private void importFileData() throws IOException, JSONException {
        InputStream inStream = new FileInputStream(historyFile) ;
        JsonReader reader = new JsonReader(new InputStreamReader(inStream, "UTF-8"));
        try {
            reader.beginArray();
            while (reader.hasNext()) {
                reader.beginObject();
                ClonerEvent evt = new ClonerEvent();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("type")) {
                        evt.setType(CommandType.valueOf(reader.nextString()));
                    } else if (name.equals("time")) {
                        evt.setTimeStamp(reader.nextString());
                    } else if (name.equals("data")) {
                        evt.setId(new CardId(reader.nextString()));
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
                this.events.add(evt); //todo make sure the event was parsed in a correct way
            }
            reader.endArray();
            inStream.close();
        }catch(Exception e){
            Log.e(this.getClass().getName(), e.getMessage());
        }
    }

    private void exportDataTofile() throws IOException, JSONException {
        OutputStream outStream = new FileOutputStream(historyFile);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(outStream, "UTF-8"));
        writer.setIndent("  ");
        writer.beginArray();
        for(int i = this.events.size()  - 1; i >= 0 ; i --){
        //for (ClonerEvent e : Collections.reverse(this.events)){
            ClonerEvent e = this.events.get(i);
            if(e != null) {
                writer.beginObject();
                writer.name("type").value(e.getType().toString());
                writer.name("time").value(e.getTimeStampString());
                writer.name("data").value(e.getId().toString());
                writer.endObject();
            }
        }
        writer.endArray();
        writer.close();
        outStream.close();
    }

    public void onClonerCommand(final ClonerCommand cmd) {
        if(cmd.getType() != CommandType.STATUS ) {
            ClonerEvent evt = new ClonerEvent(((CardIDData) cmd.getData()).getId());
            evt.setType(cmd.getType());
            this.addEvent(evt);
        }
    }

    @Override
    public void onClonerDisconnect() {
       try {
           exportDataTofile();
       }catch(Exception e){

       }
    }

    private class RowHolder{
        public TextView row_text ;
    }

}
