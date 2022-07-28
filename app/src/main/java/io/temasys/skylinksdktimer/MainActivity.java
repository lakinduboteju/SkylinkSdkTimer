package io.temasys.skylinksdktimer;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sg.com.temasys.skylink.sdk.listener.LifeCycleListener;
import sg.com.temasys.skylink.sdk.listener.MessagesListener;
import sg.com.temasys.skylink.sdk.listener.RemotePeerListener;
import sg.com.temasys.skylink.sdk.rtc.SkylinkCallback;
import sg.com.temasys.skylink.sdk.rtc.SkylinkConfig;
import sg.com.temasys.skylink.sdk.rtc.SkylinkConnection;
import sg.com.temasys.skylink.sdk.rtc.SkylinkError;
import sg.com.temasys.skylink.sdk.rtc.SkylinkEvent;
import sg.com.temasys.skylink.sdk.rtc.SkylinkInfo;
import sg.com.temasys.skylink.sdk.rtc.UserInfo;

public class MainActivity extends AppCompatActivity implements LifeCycleListener, RemotePeerListener, MessagesListener {
    private ViewModel mViewModel;
    private SkylinkConnection mSkylinkConnection;
    private long mLastRecordedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewModel = new ViewModelProvider(this).get(ViewModel.class);

        RecyclerView dataRv = findViewById(R.id.dataRv);
        dataRv.setLayoutManager(new LinearLayoutManager(this));
        mViewModel.data.observe(this, newData -> dataRv.setAdapter(new RecyclerViewAdapter(this, newData)));

        Button connectBtn = findViewById(R.id.connectBtn);
        Button disconnectBtn = findViewById(R.id.disconnectBtn);
        Button getStoredMessagesBtn = findViewById(R.id.getStoredMessagesBtn);
        mViewModel.isConnectedToRoom.observe(this, isConnected -> {
            if (isConnected) {
                connectBtn.setVisibility(View.GONE); disconnectBtn.setVisibility(View.VISIBLE); getStoredMessagesBtn.setVisibility(View.VISIBLE);
            } else {
                connectBtn.setVisibility(View.VISIBLE); disconnectBtn.setVisibility(View.GONE); getStoredMessagesBtn.setVisibility(View.GONE);
            }
        });

        // Create an Skylink connection instance
        mSkylinkConnection = SkylinkConnection.getInstance();

        // Prepare Skylink Config for messaging
        SkylinkConfig skylinkConfig = new SkylinkConfig();
        skylinkConfig.setAudioVideoSendConfig(SkylinkConfig.AudioVideoConfig.NO_AUDIO_NO_VIDEO);
        skylinkConfig.setAudioVideoReceiveConfig(SkylinkConfig.AudioVideoConfig.NO_AUDIO_NO_VIDEO);
        skylinkConfig.setSkylinkRoomSize(SkylinkConfig.SkylinkRoomSize.EXTRA_SMALL);
        skylinkConfig.setMaxRemotePeersConnected(3, SkylinkConfig.AudioVideoConfig.NO_AUDIO_NO_VIDEO);
        skylinkConfig.setP2PMessaging(true);
        skylinkConfig.setTimeout(SkylinkConfig.SkylinkAction.GET_MESSAGE_STORED, 5000);

        // Init Skylink connection
        mSkylinkConnection.init(skylinkConfig, getApplicationContext(), new SkylinkCallback() {
            @Override
            public void onError(SkylinkError skylinkError, HashMap<String, Object> hashMap) {
                // TODO: Handle error
            }
        });

        // Set Skylink connection listeners
        mSkylinkConnection.setLifeCycleListener(this);
        mSkylinkConnection.setRemotePeerListener(this);
        mSkylinkConnection.setMessagesListener(this);

        mLastRecordedTime = 0;
    }

    @Override
    protected void onDestroy() {
        if (mViewModel.isConnectedToRoom.getValue()) {
            mSkylinkConnection.disconnectFromRoom(new SkylinkCallback() {
                @Override
                public void onError(SkylinkError skylinkError, HashMap<String, Object> hashMap) {
                    // TODO: Handle error
                }
            });
        }
        mSkylinkConnection.clearInstance();
        mSkylinkConnection = null;
        super.onDestroy();
    }

    public void onConnectedBtnPressed(View v) {
        mLastRecordedTime = SystemClock.elapsedRealtime();

        // Connect to Skylink room
        mSkylinkConnection.connectToRoom(getString(R.string.app_key),
            getString(R.string.app_key_secret),
            "my-test-room",
            "my-test-peer",
            new SkylinkCallback() {
                @Override
                public void onError(SkylinkError skylinkError, HashMap<String, Object> hashMap) {
                    // TODO: Handle error
                }
            }
        );
    }

    public void onDisconnectedBtnPressed(View v) {
        mLastRecordedTime = SystemClock.elapsedRealtime();

        mSkylinkConnection.disconnectFromRoom(new SkylinkCallback() {
            @Override
            public void onError(SkylinkError skylinkError, HashMap<String, Object> hashMap) {
                // TODO: Handle error
            }
        });
    }

    public void onGetStoredMessagesBtnPressed(View v) {
        mLastRecordedTime = SystemClock.elapsedRealtime();

        mSkylinkConnection.getStoredMessages(new SkylinkCallback.StoredMessages() {
            @Override
            public void onObtainStoredMessages(JSONArray jsonArray, Map<SkylinkError, JSONArray> map) {
                long timeTookToGetStoredMessages = SystemClock.elapsedRealtime() - mLastRecordedTime;

                List<Pair<String, Long>> data = mViewModel.data.getValue();
                data.add(new Pair<>("GetStoredMessages", timeTookToGetStoredMessages));
                mViewModel.data.postValue(data);
            }
        });
    }

    // LifeCycleListener callbacks

    @Override
    public void onConnectToRoomSucessful() {
        long timeTookToConnect = SystemClock.elapsedRealtime() - mLastRecordedTime;

        List<Pair<String, Long>> data = mViewModel.data.getValue();
        data.add(new Pair<>("Connect", timeTookToConnect));
        mViewModel.data.postValue(data);

        mViewModel.isConnectedToRoom.postValue(true);
    }

    @Override
    public void onConnectToRoomFailed(String s) {
    }

    @Override
    public void onDisconnectFromRoom(SkylinkEvent skylinkEvent, String s) {
        long timeTookToDisconnect = SystemClock.elapsedRealtime() - mLastRecordedTime;

        List<Pair<String, Long>> data = mViewModel.data.getValue();
        data.add(new Pair<>("Disconnect", timeTookToDisconnect));
        mViewModel.data.postValue(data);

        mViewModel.isConnectedToRoom.postValue(false);
    }

    @Override
    public void onChangeRoomLockStatus(boolean b, String s) {
    }

    @Override
    public void onReceiveInfo(SkylinkInfo skylinkInfo, HashMap<String, Object> hashMap) {
    }

    @Override
    public void onReceiveWarning(SkylinkError skylinkError, HashMap<String, Object> hashMap) {
    }

    @Override
    public void onReceiveError(SkylinkError skylinkError, HashMap<String, Object> hashMap) {
    }

    // RemotePeerListener callbacks

    @Override
    public void onReceiveRemotePeerJoinRoom(String s, UserInfo userInfo) {
    }

    @Override
    public void onConnectWithRemotePeer(String s, UserInfo userInfo, boolean b) {
    }

    @Override
    public void onRefreshRemotePeerConnection(String s, UserInfo userInfo, boolean b, boolean b1) {
    }

    @Override
    public void onReceiveRemotePeerUserData(Object o, String s) {
    }

    @Override
    public void onOpenRemotePeerDataConnection(String s) {
    }

    @Override
    public void onDisconnectWithRemotePeer(String s, UserInfo userInfo, boolean b) {
    }

    @Override
    public void onReceiveRemotePeerLeaveRoom(String s, SkylinkInfo skylinkInfo, UserInfo userInfo) {
    }

    @Override
    public void onErrorForRemotePeerConnection(SkylinkError skylinkError, HashMap<String, Object> hashMap) {
    }

    // MessagesListener callbacks

    @Override
    public void onReceiveServerMessage(Object o, boolean b, Long aLong, String s) {
    }

    @Override
    public void onReceiveP2PMessage(Object o, boolean b, Long aLong, String s) {
    }
}