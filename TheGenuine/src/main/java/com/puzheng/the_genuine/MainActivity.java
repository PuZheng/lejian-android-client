package com.puzheng.the_genuine;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.widget.Toast;

import com.puzheng.the_genuine.data_structure.VerificationInfo;
import com.puzheng.the_genuine.netutils.WebService;
import com.puzheng.the_genuine.utils.BadResponseException;
import com.puzheng.the_genuine.utils.Misc;
import com.puzheng.the_genuine.utils.PoliteBackgroundTask;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class MainActivity extends Activity implements BackPressedInterface {

    public static final String TAG_VERIFICATION_INFO = "VERIFICATION_INFO";
    public static final String TAG_PRODUCT_RESPONSE = "PRODUCT_RESPONSE";
    private static final String MIME_TEXT_PLAIN = "text/plain";
    private BackPressedHandle backPressedHandle = new BackPressedHandle();
    public static boolean isNfcEnabled = true;


    @Override
    public void doBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        backPressedHandle.doBackPressed(this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        isNfcEnabled = NfcAdapter.getDefaultAdapter(this) != null;
        if (isNfcEnabled) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container, new NfcFragment());
            ft.commit();
            handleIntent(getIntent());
        } else {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container, new BarCodeFragment());
            ft.commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private String extractNFCMessage(Intent intent) {
        String action = intent.getAction();
        Tag tag = null;
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            } else {
                Toast.makeText(MainActivity.this, "Wrong mime type: " + type, Toast.LENGTH_SHORT).show();
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();
            boolean hit = false;
            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    hit = true;
                    break;
                }
            }
            if (!hit) {
                Toast.makeText(MainActivity.this, "无法识别的NFC标签", Toast.LENGTH_SHORT).show();
                tag = null;
            }
        }
        if (tag == null) {
            return null;
        }
        Ndef ndef = Ndef.get(tag);
        if (ndef == null) {
            // NDEF is not supported by this Tag.
            return null;
        }
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();
        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                try {
                    return readText(ndefRecord);
                } catch (UnsupportedEncodingException e) {
                    Toast.makeText(MainActivity.this, "Unsupported Encoding" + e, Toast.LENGTH_SHORT).show();
                }
            }
        }
        return null;
    }

    private void handleIntent(Intent intent) {
        final String code = extractNFCMessage(intent);
        if (!Misc.isEmptyString(code)) {
            PoliteBackgroundTask.Builder<VerificationInfo> builder = new PoliteBackgroundTask.Builder<VerificationInfo>(this);
            builder.msg("已读取NFC信息，正在验证真伪");
            builder.run(new PoliteBackgroundTask.XRunnable<VerificationInfo>() {
                @Override
                public VerificationInfo run() throws Exception {
                    try {
                        return WebService.getInstance(MainActivity.this).verify(code);
                    } catch (Exception e) {
                        return null;
                    }
                }
            });
            builder.after(new PoliteBackgroundTask.OnAfter<VerificationInfo>() {

                @Override
                public void onAfter(VerificationInfo verificationInfo) {
                    Intent intent;
                    if (verificationInfo != null) {
                        intent = new Intent(MainActivity.this, SPUActivity.class);
                        intent.putExtra(TAG_VERIFICATION_INFO, verificationInfo);
                    } else {
                        intent = new Intent(MainActivity.this, CounterfeitActivity.class);
                    }
                    startActivity(intent);
                }
            });
            builder.create().start();
        }
    }

    private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */
        byte[] payload = record.getPayload();
        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"
        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }
}
