package com.example.amitk.mobilecon;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.dialog.v1.model.Conversation;
import com.ibm.watson.developer_cloud.http.ServiceCallback;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ChatBot extends AppCompatActivity {
    String inputText="";
    String outputText="";
    EditText userInput;
    MessageRequest request;
    Button btnSend;
    ConversationService mConversationService;
    ListView botList;
    final List<CharModel> lstChat=new ArrayList<CharModel>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        mConversationService=new ConversationService("2017-05-26","615ba88f-01d1-49cf-9204-373de21b021c","KocUeu4jS8k0");
        userInput = (EditText)findViewById(R.id.user_input);
        btnSend=(Button)findViewById(R.id.btn);
        botList=(ListView)findViewById(R.id.listView);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSetResponce();
            }
        });

    }
    public void getSetResponce(){

        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
        inputText= userInput.getText().toString();
        sendDatainList(inputText,timeStamp.getTime(),false);

        userInput.setText("");

        request = new MessageRequest.Builder()
                .inputText(inputText)
                .build();

        mConversationService
                .message("b8a35230-c672-407d-8c16-2a78b48f889a", request)
                .enqueue(new ServiceCallback<MessageResponse>() {
                    @Override
                    public void onResponse(MessageResponse response) {

                        final Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
                        if(response.getText().size()==0)
                            outputText= "Sorry, I didn't get your meaning.";
                        else
                            outputText= response.getText().get(0);

                        System.out.println("--------------------------------------------" + response.getText().size());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendDatainList(outputText,timeStamp.getTime(),true);
                            }
                        });
                    }
                    @Override
                    public void onFailure(Exception e) {
//                        if(e.equals(IndexOutOfBoundsException)){
//
//                        }
                        final Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
                        outputText="Sorry, I didn't get your meaning.";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendDatainList(outputText,timeStamp.getTime(),true);
                            }
                        });
                        System.out.println("----------------------------------------Failuer : " + e.toString());
                    }
                });
    }

    private void sendDatainList(String content,long timestamp,boolean isSend) {
        lstChat.add(new CharModel(content,isSend,timestamp,""));

        CustomAdapterChat adapterChat = new CustomAdapterChat(lstChat, ChatBot.this,false,new ArrayList<String>(),new ArrayList<String>(),getContentResolver());
        botList.setAdapter(adapterChat);
        botList.smoothScrollByOffset(botList.getCount());
    }
}
