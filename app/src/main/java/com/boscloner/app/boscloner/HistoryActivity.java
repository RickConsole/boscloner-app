package com.boscloner.app.boscloner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ListView;
import android.widget.Toast;

import com.boscloner.app.boscloner.decoder.WiegandDecoder;

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
public class HistoryActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "HistoryActivity";

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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.history, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                //TODO update history
                return true;
            case R.id.action_clear:
                showClearHistoryDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Confirm history cleanup?")
                .setTitle("Confirm history cleanup?")
                .setPositiveButton(R.string.ok, (dialog, id) -> ClonerHistory.getInstance().clearEvents())
                .setNegativeButton(R.string.cancel, (dialog, id) -> {})
                .create()
                .show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_clone_dialog_msg)
                .setTitle(R.string.confirm_clone_dialog_msg)
                .setPositiveButton(R.string.ok, (dialog, id1) -> {
                    ClonerCommand cloneCommand = new ClonerCommand();
                    ClonerEvent evt = ClonerHistory.getInstance().getEvents().get(position);
                    cloneCommand.setType(CommandType.CLONE);
                    CardIDData idData = new CardIDData(evt.getId());
                    cloneCommand.setData(idData);
                    ConnectedClonerContainner.getInstance().getCloner().sendCommand(cloneCommand);
                })
                .setNegativeButton(R.string.cancel, (dialog, id12) -> {})
                .create()
                .show();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ClonerEvent event = ClonerHistory.getInstance().getEvents().get(position);
        showCardTypeDialog(event);
    }

    private void showCardTypeDialog(final ClonerEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_card_type, null);
        builder.setView(dialogView);

        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.card_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.card_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        builder.setTitle("Select Card Type")
                .setPositiveButton("Decode", null)
                .setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WiegandDecoder.CardFormat format;
                switch (spinner.getSelectedItemPosition()) {
                    case 0:
                        format = WiegandDecoder.CardFormat.BIT_26;
                        break;
                    case 1:
                        format = WiegandDecoder.CardFormat.BIT_33;
                        break;
                    case 2:
                        format = WiegandDecoder.CardFormat.BIT_34;
                        break;
                    case 3:
                        format = WiegandDecoder.CardFormat.BIT_35;
                        break;
                    default:
                        format = WiegandDecoder.CardFormat.BIT_26;
                }
                decodeAndShowResult(event, format);
                dialog.dismiss();
            }
        });
    }

    private void decodeAndShowResult(ClonerEvent event, WiegandDecoder.CardFormat format) {
        try {
            String hexString = event.getId().toString();
            Log.d(TAG, "Original hex string: " + hexString);

            byte[] cardData = hexStringToByteArray(hexString);
            Log.d(TAG, "Converted byte array length: " + cardData.length);
            Log.d(TAG, "Byte array content: " + bytesToHex(cardData));

            WiegandDecoder.DecodedCard decodedCard = WiegandDecoder.decode(cardData, format);

            if (decodedCard != null) {
                showDecodedDataDialog(decodedCard, hexString);
            } else {
                showErrorDialog("Unable to decode card with selected format.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error decoding card", e);
            showErrorDialog("Error decoding card: " + e.getMessage());
        }
    }

    private void showDecodedDataDialog(WiegandDecoder.DecodedCard decodedCard, String originalData) {
        String message = String.format("Format: %s\nFacility Code: %d\nCard Number: %d\n\nOriginal Data: %s",
                decodedCard.format, decodedCard.facilityCode, decodedCard.cardNumber, originalData);

        new AlertDialog.Builder(this)
                .setTitle("Decoded Card Information")
                .setMessage(message)
                .setPositiveButton("Copy to Clipboard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Card Info", message);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(HistoryActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Decoding Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private byte[] hexStringToByteArray(String s) {
        s = s.replaceAll(":", "").replaceAll("\\s", ""); // Remove colons and any whitespace
        if (s.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have an even number of characters");
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
}
