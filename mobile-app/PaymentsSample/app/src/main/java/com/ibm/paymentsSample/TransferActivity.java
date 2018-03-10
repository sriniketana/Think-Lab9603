package com.ibm.paymentsSample;

import java.lang.*;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import android.content.*;
import android.widget.TextView;
import android.widget.Toast;

import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLResourceRequest;
import com.worklight.wlclient.api.WLResponse;
import com.worklight.wlclient.api.WLResponseListener;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by bob on 01/03/18.
 */

public class TransferActivity extends AppCompatActivity {

    private ArrayList<JSONObject> accounts = new ArrayList<JSONObject>();
    private List<String> beneficiaryList = new ArrayList<String>();
    private String username;
    private JSONObject currentAccount;
    private TransferActivity _this;
    private String msg;
    private Spinner spinner1;
    private BroadcastReceiver logoutReceiver, loginRequiredReceiver;
    protected String selectedBeneficiary;
    private JSONArray accountsList;
    private int amountToTransfer;

    List<String> spinnerArray =  new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Entered oncreate of transfer activity");

        //Get intent and username from the login activity
        Intent transferIntent = getIntent();
        username = transferIntent.getStringExtra("username");

        //Set content view for this transfer activity
        setContentView(R.layout.activity_transfer);

        final TextView statusmsg = (TextView) findViewById(R.id.msg);
        statusmsg.setVisibility(View.INVISIBLE);

        //Add onclick listener to the transfer funds button.
        Button transferFundsButton = (Button) findViewById(R.id.transferFund);

        transferFundsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get the selected Beneficiary and transfer the entered funds amount from this account holder to beneficiary account
                URI adapterPath = null;
                try {
                    EditText amount = (EditText) findViewById(R.id.transfer_amount);
                    amountToTransfer = Integer.parseInt(amount.getText().toString());
                    adapterPath = new URI("/adapters/paymentsnetwork/TransferMoney/");
                    //adapterPath = new URI("http://9.199.44.148:3000/api/TransferMoney");
                    WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.POST);
                    JSONObject transaction = new JSONObject();
                             /*
                  {
                 "$class": "com.ibm.payments.TransferMoney",
                  "source": "resource:com.ibm.payments.Account#111111",
                  "destination": "resource:com.ibm.payments.Account#222222",
                  "amount": 10
                }*/
                    transaction.put("propertyClass", "com.ibm.payments.TransferMoney");
                    transaction.put("source", "resource:com.ibm.payments.Account#" + currentAccount.get("accountId"));
                    for(int i = 0; i<accounts.size();i++) {
                        if (accounts.get(i).get("owner").toString().split("#")[1].equals(selectedBeneficiary)){
                            transaction.put("destination", "resource:com.ibm.payments.Account#" + accounts.get(i).get("accountId"));
                        }
                    }
                    transaction.put("amount",String.valueOf(amountToTransfer));

                 //   transaction.put("$class", "com.ibm.payments.TransferMoney");

                    request.send(transaction, new WLResponseListener() {
                        @Override
                        public void onSuccess(WLResponse wlResponse) {

                            System.out.println("srukotta - Transfer succeeded - " + wlResponse.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    statusmsg.setText("Transfer successful");
                                    statusmsg.setVisibility(View.VISIBLE);
                                    EditText amnt = (EditText) findViewById(R.id.transfer_amount);
                                    amnt.setText("");
                                    TextView balance = (TextView) findViewById(R.id.balance);
                                    int balanceInt = (int) Double.parseDouble(balance.getText().toString().split(":")[1]);
                                    balance.setText("Available balance is :"+String.valueOf(balanceInt-amountToTransfer));
                                }
                            });
                        }

                        @Override
                        public void onFailure(WLFailResponse wlFailResponse) {
                            msg = wlFailResponse.toString();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    statusmsg.setText("Transfer failed - " + msg);
                                    statusmsg.setVisibility(View.VISIBLE);
                                }
                            });
                            System.out.println("Transfer failed - " + wlFailResponse.toString());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        _this = this;

        URI adapterPath = null;

        try {
            adapterPath = new URI("/adapters/paymentsnetwork/Account/");
            //adapterPath = new URI("http://9.199.44.148:3000/api/Account");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.GET);
        request.send(new WLResponseListener() {
            @Override
            public void onSuccess(WLResponse wlResponse) {
                try {
                    accountsList = new JSONArray(wlResponse.getResponseText());
                    int size = accountsList.length();
                    for(int i = 0; i<size;i++) {
                        accounts.add(accountsList.getJSONObject(i));
                        if(accountsList.getJSONObject(i).get("owner").toString().split("#")[1].equals(username)){
                            currentAccount = accountsList.getJSONObject(i);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        TextView availableBalance = (TextView) findViewById(R.id.balance);
                                        availableBalance.setText("Available balance is :"+currentAccount.get("balance").toString());
                                    }catch(Exception e)
                                    {
                                        System.out.print("Exception is : " + e.getMessage());
                                    }
                                }
                            });
                            System.out.println("srukotta - current user : " + currentAccount.get("owner"));
                        }
                    }
                    URI adapterPath = null;
                       adapterPath = new URI("/adapters/paymentsnetwork/AccountHolder/");
                 //   adapterPath = new URI("http://9.199.44.148:3000/api/AccountHolder");
                    WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.GET);
                    request.send(new WLResponseListener() {
                        @Override
                        public void onSuccess(WLResponse wlResponse) {
                            try {
                                JSONArray holdersList = new JSONArray(wlResponse.getResponseText().toString());
                                for (int i = 0; i < holdersList.length(); i++) {
                                    if (!(holdersList.getJSONObject(i).get("email").equals(username))) {
                                        beneficiaryList.add(holdersList.getJSONObject(i).get("email").toString());
                                    }
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addBeneficiaries(beneficiaryList);
                                    }
                                });
                            }catch(Exception e)
                            {
                                System.out.println("Exception : " + e.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(WLFailResponse failure) {
                            System.out.println("Failed is : " + failure.toString());
                        }
                    });

                }catch(Exception e)
                {
                    System.out.println("Exception parsing adapter response");
                }

            }
            @Override
            public void onFailure(WLFailResponse wlFailResponse) {
                Log.d("Failure", wlFailResponse.getErrorMsg());
            }
        });

        logoutReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
              System.out.println("srukotta - Finally hit!!");
            }
        };

        loginRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent login = new Intent(_this, LoginActivity.class);
                _this.startActivity(login);
            }
        };
    }

    public void addBeneficiaries(List<String> beneficiaries){
        if(beneficiaries.size()>0) {
            spinner1 = (Spinner) findViewById(R.id.spinner1);

            for (String beneficiary : beneficiaries) {
                spinnerArray.add(beneficiary);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item, spinnerArray);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner sItems = (Spinner) findViewById(R.id.spinner1);
            spinner1.setAdapter(adapter);
            spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedBeneficiary = parent.getItemAtPosition(position).toString();
                    System.out.println("SelectedItem is : " + parent.getItemAtPosition(position).toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinner1.setSelection(0);
            selectedBeneficiary = beneficiaryList.get(0);
        }
    }
}
