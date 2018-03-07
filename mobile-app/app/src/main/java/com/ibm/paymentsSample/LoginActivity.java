package com.ibm.paymentsSample;

import android.app.AlertDialog;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.api.WLResourceRequest;
import com.worklight.wlclient.api.WLResponse;
import com.worklight.wlclient.api.WLResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput, firstNameInput, lastNameInput, emailInput;
    private TextView errorMsgDisplay, remainingAttemptsDisplay;
    private String email;
    private boolean createAccount = true;
    private boolean createAccountHolder = true;
    private BroadcastReceiver loginErrorReceiver, loginRequiredReceiver, loginSuccessReceiver;
    private int accountIdMax;
    private LoginActivity _this;
    private String msg;
    private final String DEBUG_NAME = "LoginActivity";
    private boolean continueLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final TextView statusmsg = (TextView) findViewById(R.id.msg);
        statusmsg.setVisibility(View.INVISIBLE);

        _this = this;

        //Initialize the UI elements
        usernameInput = (EditText)findViewById(R.id.usernameInput);
        passwordInput = (EditText)findViewById(R.id.passwordInput);
        errorMsgDisplay = (TextView)findViewById(R.id.errorMsg);
        remainingAttemptsDisplay = (TextView)findViewById(R.id.remainingAttempts);
        firstNameInput = (EditText) findViewById(R.id.firstname);
        lastNameInput = (EditText) findViewById(R.id.lastname);
        emailInput = (EditText) findViewById(R.id.email);
        final Button createAccountButton = (Button) findViewById(R.id.createaccount);
        Button loginButton = (Button) findViewById(R.id.login);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusmsg.setVisibility(View.INVISIBLE);
                URI adapterPath = null;
                try {
                    adapterPath = new URI("/adapters/paymentsnetwork/AccountHolder/");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.GET);
                final EditText usernameField = (EditText) findViewById(R.id.usernameInput);
                email = emailInput.getText().toString();
                request.send(new WLResponseListener() {
                    @Override
                    public void onSuccess(WLResponse wlResponse) {
                        try {
                            JSONArray accountHoldersList = new JSONArray(wlResponse.getResponseText().toString());
                            JSONObject accountholder;
                            for (int i = 0; i < accountHoldersList.length(); i++) {
                                accountholder = accountHoldersList.getJSONObject(i);
                                if (email.equals(accountholder.get("email"))) {
                                    createAccountHolder = false;
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        URI adapterPathNew = null;
                        try {
                            adapterPathNew = new URI("/adapters/paymentsnetwork/Account/");
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }

                        WLResourceRequest request = new WLResourceRequest(adapterPathNew, WLResourceRequest.GET);
                        request.send(new WLResponseListener() {
                            @Override
                            public void onSuccess(WLResponse wlResponse) {
                                try {
                                    JSONArray accountsList = new JSONArray(wlResponse.getResponseText());
                                    for (int i = 0; i < accountsList.length(); i++) {
                                        if (accountsList.getJSONObject(i).get("owner").toString().split("#")[1].equals(email)) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    statusmsg.setText("Error. This user already has an account.");
                                                    statusmsg.setVisibility(View.VISIBLE);
                                                }
                                            });
                                            createAccount = false;
                                        }
                                        if (Integer.parseInt(accountsList.getJSONObject(i).get("accountId").toString()) > accountIdMax) {
                                            accountIdMax = Integer.parseInt(accountsList.getJSONObject(i).get("accountId").toString());
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(WLFailResponse failResponse) {
                                System.out.print("Accounts fetch failed with msg : " + failResponse.toString());
                                msg = failResponse.toString();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        statusmsg.setText("Accounts fetch failed with msg : " + msg);
                                        statusmsg.setVisibility(View.VISIBLE);
                                    }
                                });
                            }

                        });
                        if (createAccountHolder) {
                            URI adapterPath = null;
                            JSONObject accountHolderDetails = new JSONObject();
                            try {
                                adapterPath = new URI("/adapters/paymentsnetwork/AccountHolder/");

                            accountHolderDetails.put("propertyClass","com.ibm.payments.AccountHolder");
                            accountHolderDetails.put("email",emailInput.getText());
                            accountHolderDetails.put("firstName",firstNameInput.getText());
                            accountHolderDetails.put("lastName",lastNameInput.getText());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            WLResourceRequest requestNew = new WLResourceRequest(adapterPath, WLResourceRequest.POST);
                            requestNew.send(accountHolderDetails, new WLResponseListener() {
                                @Override
                                public void onSuccess(WLResponse wlResponse) {
                                    try {
                                        System.out.println("Account Holder created successfully");
                                        URI adapterPath = null;
                                        JSONObject accountDetails = new JSONObject();
                                        try {
                                            adapterPath = new URI("/adapters/paymentsnetwork/Account/");
                                            accountDetails.put("propertyClass","com.ibm.payments.Account");
                                            accountDetails.put("balance","100");
                                            accountDetails.put("owner",email);
                                            accountDetails.put("accountId",String.valueOf(accountIdMax + 1));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        WLResourceRequest requestNew = new WLResourceRequest(adapterPath, WLResourceRequest.POST);
                                        requestNew.send(accountDetails,new WLResponseListener() {
                                            @Override
                                            public void onSuccess(WLResponse wlResponse) {
                                                System.out.println("Account created successfully");
                                                Intent openTransfer = new Intent(_this, TransferActivity.class);
                                                openTransfer.putExtra("username",emailInput.getText().toString());
                                                runOnUiThread(new Runnable() {
                                                                  @Override
                                                                  public void run() {
                                                                      usernameInput.setText("");
                                                                      passwordInput.setText("");
                                                                      firstNameInput.setText("");
                                                                      lastNameInput.setText("");
                                                                      emailInput.setText("");
                                                                  }
                                                              }
                                                );
                                                _this.startActivity(openTransfer);
                                            }

                                            @Override
                                            public void onFailure(WLFailResponse failResponse) {
                                                System.out.print("Account creation failed with msg : " + failResponse.toString());
                                                msg = failResponse.toString();
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        statusmsg.setText("Account creation failed with msg : " + msg);
                                                        statusmsg.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                            }
                                        });
                                    } catch (Exception e) {
                                            e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(WLFailResponse failResponse) {
                                    System.out.print("Account Holder failed with msg : " + failResponse.toString());
                                    msg = failResponse.toString();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            statusmsg.setText("Account Holder creation failed with msg : " + msg);
                                            statusmsg.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            });
                        }

                        if (createAccount && !createAccountHolder) {
                            URI adapterPath = null;
                            JSONObject accountDetails = new JSONObject();
                            try {
                                adapterPath = new URI("/adapters/paymentsnetwork/Account/");
                                accountDetails.put("propertyClass","com.ibm.payments.Account");
                                accountDetails.put("balance","100");
                                accountDetails.put("owner",email);
                                accountDetails.put("accountId",String.valueOf(accountIdMax + 1));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            WLResourceRequest requestNew = new WLResourceRequest(adapterPath, WLResourceRequest.POST);
                            requestNew.send(new WLResponseListener() {
                                @Override
                                public void onSuccess(WLResponse wlResponse) {
                                    System.out.println("Account created successfully");
                                    Intent openTransfer = new Intent(_this, TransferActivity.class);
                                    openTransfer.putExtra("username",emailInput.getText().toString());
                                    runOnUiThread(new Runnable() {
                                                      @Override
                                                      public void run() {
                                                          usernameInput.setText("");
                                                          passwordInput.setText("");
                                                          firstNameInput.setText("");
                                                          lastNameInput.setText("");
                                                          emailInput.setText("");
                                                      }
                                                  }
                                    );
                                    _this.startActivity(openTransfer);
                                }

                                @Override
                                public void onFailure(WLFailResponse failResponse) {
                                    System.out.print("Account creation failed with msg : " + failResponse.toString());
                                    msg = failResponse.toString();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            statusmsg.setText("Account creation failed with msg : " + msg);
                                            statusmsg.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            });
                        }
                    }
                    @Override
                    public void onFailure(WLFailResponse failure) {
                        System.out.println("Account holder fetch failed");
                        msg = failure.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusmsg.setText("Account holder fetch failed" + msg);
                                statusmsg.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            }
        });

        //Login Button behavior
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(usernameInput.getText().toString().isEmpty() || passwordInput.getText().toString().isEmpty()){
                    alertError("Username and password are required");
                }
                else{
                    URI adapterPath = null;

                    try {
                        adapterPath = new URI("/adapters/paymentsnetwork/Account/");
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    WLResourceRequest request = new WLResourceRequest(adapterPath, WLResourceRequest.GET);
                    request.send(new WLResponseListener() {
                        @Override
                        public void onSuccess(WLResponse wlResponse) {
                            try {
                                JSONArray accountsList = new JSONArray(wlResponse.getResponseText());
                                int size = accountsList.length();
                                for (int i = 0; i < size; i++) {
                                    if (accountsList.getJSONObject(i).get("owner").toString().split("#")[1].equals(usernameInput.getText().toString())) {
                                        continueLogin = true;
                                        break;
                                    }
                                }
                                if(continueLogin)
                                {
                                    JSONObject credentials = new JSONObject();
                                    try {
                                        credentials.put("username",usernameInput.getText().toString());
                                        credentials.put("password",passwordInput.getText().toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Intent intent = new Intent();
                                    intent.setAction(Constants.ACTION_LOGIN);
                                    intent.putExtra("credentials",credentials.toString());
                                    LocalBroadcastManager.getInstance(_this).sendBroadcast(intent);
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            statusmsg.setText("Please create account first");
                                            statusmsg.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    System.out.println("Account does not exist for this user. First create an account and then login.");
                                }
                            }catch (Exception e)
                            {
                                System.out.println("Exception occurred : " + e.getMessage());
                            }
                        }
                        @Override
                        public void onFailure(WLFailResponse failResponse)
                        {
                            statusmsg.setText("Failed getting accounts list with failure : " + failResponse.toString());
                            statusmsg.setVisibility(View.VISIBLE);
                            System.out.println("Failed getting accounts list with failure : " + failResponse.toString());
                        }
                    });

                }
            }
        });

        //Login error receiver
        loginErrorReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(DEBUG_NAME, "loginErrorReceiver");
                errorMsgDisplay.setText("");
                remainingAttemptsDisplay.setText("");
                alertError(intent.getStringExtra("errorMsg"));
            }
        };

        //Login required
        loginRequiredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                Log.d(DEBUG_NAME, "loginRequiredReceiver");
                Runnable run = new Runnable() {
                    public void run() {
                        //Set error message:
                        errorMsgDisplay.setText(intent.getStringExtra("errorMsg"));

                        //Display remaining attempts
                        if(intent.getIntExtra("remainingAttempts",-1) > -1) {
                            remainingAttemptsDisplay.setText(getString(R.string.remaining_attempts, intent.getIntExtra("remainingAttempts",-1)));
                        }
                    }
                };
                _this.runOnUiThread(run);
            }
        };

        //Login success
        loginSuccessReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(isTaskRoot()){
                    //First time, go to protected area
                    Intent openTransfer = new Intent(_this, TransferActivity.class);
                    openTransfer.putExtra("username",usernameInput.getText().toString());
                    _this.startActivity(openTransfer);

                } else{
                    //Other times, go "back" to wherever you came from
                    finish();
                }
            }
        };

    }

    @Override
    protected void onStart() {
        Log.d(DEBUG_NAME, "onStart");
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(loginRequiredReceiver, new IntentFilter(Constants.ACTION_LOGIN_REQUIRED));
        LocalBroadcastManager.getInstance(this).registerReceiver(loginErrorReceiver, new IntentFilter(Constants.ACTION_LOGIN_FAILURE));
        LocalBroadcastManager.getInstance(this).registerReceiver(loginSuccessReceiver, new IntentFilter(Constants.ACTION_LOGIN_SUCCESS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_NAME, "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginErrorReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginRequiredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loginSuccessReceiver);
    }

    public void alertError(final String msg) {

        Runnable run = new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(_this);
                builder.setMessage(msg)
                        .setTitle("Error");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };
        _this.runOnUiThread(run);
    }

}

