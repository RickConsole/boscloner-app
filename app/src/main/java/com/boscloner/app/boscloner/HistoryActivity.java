package com.boscloner.app.boscloner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.boscloner.app.boscloner.bluetooth.IBluetoothManager;
import com.boscloner.app.boscloner.cloner.ClonerEvent;
import com.boscloner.app.boscloner.cloner.ClonerHistory;
import com.boscloner.app.boscloner.cloner.ConnectedClonerContainner;
import com.boscloner.app.boscloner.cloner.cmd.CardIDData;
import com.boscloner.app.boscloner.cloner.cmd.ClonerCommand;
import com.boscloner.app.boscloner.cloner.cmd.CommandType;
import com.boscloner.app.boscloner.listeners.AvailableBluetoothDeviceList;
import com.example.jpiat.boscloner.R;

/**
 * Created by jpiat on 10/13/15.
 */
public class HistoryActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener{

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        IBluetoothManager dev = AvailableBluetoothDeviceList.getInstance().getSelectedItem();
        ListView historyList = (ListView) findViewById(R.id.history_list);
        ClonerHistory hist = ClonerHistory.getInstance();
        hist.setContext(this);
        historyList.setAdapter(hist);
        historyList.setOnItemLongClickListener(this);
        historyList.setOnItemClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.history, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                //TODO update history
                return true;
            case R.id.action_clear:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ClonerHistory.getInstance().clearEvents();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
                builder.setMessage("Confirm history cleanup ?");
                builder.setTitle("Confirm history cleanup ?");
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_clone_dialog_msg);
        builder.setTitle(R.string.confirm_clone_dialog_msg);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ClonerCommand cloneCommand = new ClonerCommand();
                ClonerEvent evt = ClonerHistory.getInstance().getEvents().get(position);
                cloneCommand.setType(CommandType.CLONE);
                CardIDData idData = new CardIDData(evt.getId());
                cloneCommand.setData(idData);
                ConnectedClonerContainner.getInstance().getCloner().sendCommand(cloneCommand);// need to compose command
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return true ;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ClonerEvent event = ClonerHistory.getInstance().getEvents().get(position);
        int[] parsedData = event.parseH10301();
        int facilityCode = parsedData[0];
        int cardNumber = parsedData[1];

        String message = String.format("Facility Code: %d\nCard Number: %d\n\nOriginal Data: %s",
                facilityCode, cardNumber, event.getId().toString());

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setTitle("Card Information")
                .setPositiveButton("Copy to Clipboard", (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Card Info", message);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(HistoryActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {})
                .create()
                .show();
    }
}
