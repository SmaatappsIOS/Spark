package com.smaat.spark.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.smaat.spark.R;
import com.smaat.spark.adapter.ChatAdapter;
import com.smaat.spark.database.ChatTable;
import com.smaat.spark.entity.inputEntity.ChatSendReceiveInputEntity;
import com.smaat.spark.entity.outputEntity.ChatReceiveEntity;
import com.smaat.spark.entity.outputEntity.UserDetailsEntity;
import com.smaat.spark.main.BaseFragment;
import com.smaat.spark.model.ChatReceiveResponse;
import com.smaat.spark.services.APIRequestHandler;
import com.smaat.spark.ui.ChatImageScreen;
import com.smaat.spark.ui.HomeScreen;
import com.smaat.spark.utils.AppConstants;
import com.smaat.spark.utils.DialogManager;
import com.smaat.spark.utils.GlobalMethods;
import com.smaat.spark.utils.InterfaceBtnCallback;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ChatFriendFragment extends BaseFragment implements InterfaceBtnCallback {


    @BindView(R.id.chat_list)
    RecyclerView mChatRecyclerView;

    @BindView(R.id.chat_edt)
    EditText mChatEdt;

    @BindView(R.id.first_msg_hint_lay)
    LinearLayout mFirstMsgHintLay;

    @BindView(R.id.hint_msg_txt)
    TextView mHintMsgTxt;

    @BindView(R.id.say_hi_txt)
    TextView mSayHiTxt;

    //    private Handler mChatHandler;
    private boolean isFragFocusBool = false, isSendPressedBool = false;
    private UserDetailsEntity mUserDetailsRes, mFriendDetailsRes;
    private ChatAdapter mChatListAdapter;
    private ArrayList<ChatReceiveEntity> mChatReceiveList;
    private Timer mReceiveAPITimer;
    private int chatOldSizeInt = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_chat_screen, container, false);
        ButterKnife.bind(this, rootView);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        isFragFocusBool = false;
        isSendPressedBool = false;
        mChatListAdapter = null;
        AppConstants.CHAT_ID = AppConstants.FAILURE_CODE;
        mChatReceiveList = new ArrayList<>();
        AppConstants.CHAT_SCREEN_TITLE = AppConstants.CHAT_FRIEND_NAME;
    }


    private void initView() {
        mUserDetailsRes = GlobalMethods.getUserDetailsRes(getActivity());
        mFriendDetailsRes = new Gson().fromJson(AppConstants.OTHER_USER_DETAILS, UserDetailsEntity.class);

        ((HomeScreen) getActivity()).mHeaderTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppConstants.OTHER_USER_DETAILS = new Gson().toJson(mFriendDetailsRes, UserDetailsEntity.class);

                AppConstants.CHAT_USER_BACK = AppConstants.SUCCESS_CODE;
                ((HomeScreen) getActivity()).replaceFragment(new UserDetailsFragment(), 1);


            }
        });

    }

    @OnClick({R.id.send_btn, R.id.camera_img})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                sendMsg();
                break;
            case R.id.camera_img:

                AppConstants.CHAT_PIC_FRIEND_ID = AppConstants.CHAT_FRIEND_ID;
                AppConstants.CHAT_PIC_SUB = AppConstants.CHAT_SUB_FRIEND;
                AppConstants.CHAT_PIC_MSG_FLOW = AppConstants.FAILURE_CODE;
                AppConstants.CHAT_PIC_BACK = AppConstants.SUCCESS_CODE;
                Intent intent;
                intent = new Intent(getActivity(), ChatImageScreen.class);
                getActivity().startActivity(intent);

                break;

        }
    }

    private void sendMsg() {
        if (GlobalMethods.isNetworkAvailable(getActivity())) {
            String chatStr = mChatEdt.getText().toString().trim();
            if (!chatStr.isEmpty()) {
                isSendPressedBool = true;
                ChatSendReceiveInputEntity chatInputEntity = new ChatSendReceiveInputEntity(AppConstants.API_CHAT_SEND, AppConstants.PARAMS_CHAT_SEND, mUserDetailsRes.getUser_id(), AppConstants.CHAT_FRIEND_ID,
                        GlobalMethods.setMsgEncoder(chatStr), AppConstants.CHAT_SUB_FRIEND, AppConstants.FAILURE_CODE, AppConstants.FAILURE_CODE, "");
                APIRequestHandler.getInstance().callSendAPI(chatInputEntity, this);

                ArrayList<ChatReceiveEntity> chatSendRes = new ArrayList<>();
                ChatReceiveEntity chatEntitySendRes = new ChatReceiveEntity();

                chatEntitySendRes.setFriend_id(AppConstants.CHAT_FRIEND_ID);
                chatEntitySendRes.setUser_id(mUserDetailsRes.getUser_id());
                chatEntitySendRes.setSubject(AppConstants.CHAT_SUB_FRIEND);
                chatEntitySendRes.setChat_id(getString(R.string.nag_val));
                chatEntitySendRes.setMessage(chatStr);
                chatEntitySendRes.setFriendname(AppConstants.CHAT_FRIEND_NAME);
                chatEntitySendRes.setUsername(mUserDetailsRes.getUsername());
                chatEntitySendRes.setUserimage(mUserDetailsRes.getMain_picture());
                chatEntitySendRes.setFriendimage(mFriendDetailsRes.getMain_picture());
                chatEntitySendRes.setDatetime(GlobalMethods.getCurrentDate());
                chatEntitySendRes.setMsg_sent_user(AppConstants.SUCCESS_CODE);
                chatEntitySendRes.setIs_attachement(AppConstants.FAILURE_CODE);
                chatEntitySendRes.setAttachement_url(AppConstants.FAILURE_CODE);

                chatSendRes.add(chatEntitySendRes);
                setDataToDB(chatSendRes);
                mChatEdt.setText("");

            }
        } else {
            DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.no_internet));
        }
    }

    private void setPicChatChat() {

        AppConstants.CHAT_PIC_BACK = AppConstants.FAILURE_CODE;
        ArrayList<ChatReceiveEntity> chatSendRes = new ArrayList<>();
        ChatReceiveEntity chatEntitySendRes = new ChatReceiveEntity();

        chatEntitySendRes.setFriend_id(AppConstants.CHAT_FRIEND_ID);
        chatEntitySendRes.setUser_id(mUserDetailsRes.getUser_id());
        chatEntitySendRes.setSubject(AppConstants.CHAT_SUB_FRIEND);
        chatEntitySendRes.setChat_id(getString(R.string.nag_val));
        chatEntitySendRes.setMessage("");
        chatEntitySendRes.setFriendname(AppConstants.CHAT_FRIEND_NAME);
        chatEntitySendRes.setUsername(mUserDetailsRes.getUsername());
        chatEntitySendRes.setUserimage(mUserDetailsRes.getMain_picture());
        chatEntitySendRes.setFriendimage(mFriendDetailsRes.getMain_picture());
        chatEntitySendRes.setDatetime(GlobalMethods.getCurrentDate());
        chatEntitySendRes.setMsg_sent_user(AppConstants.SUCCESS_CODE);
        chatEntitySendRes.setIs_attachement(getResources().getString(R.string.two));
        chatEntitySendRes.setAttachement_title(AppConstants.CHAT_PIC_TXT);
        chatEntitySendRes.setAttachement_url(AppConstants.CHAT_PIC_LOCAL_PATH);

        chatSendRes.add(chatEntitySendRes);
        setDataToDB(chatSendRes);
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (mChatHandler != null) {
//            mChatHandler.removeCallbacks(chatReceiveAPICall);
//        }
        isFragFocusBool = false;
        stopChatReceiveAPICall();


    }

    @Override
    public void onResume() {
        super.onResume();
        isFragFocusBool = true;
        initView();
        ((HomeScreen) getActivity()).setHeaderLeftClick(false);
        ((HomeScreen) getActivity()).setHeadRigImgVisible(false, R.mipmap.app_icon);

        stopChatReceiveAPICall();
        getDataFromDB(mUserDetailsRes.getUser_id(), AppConstants.CHAT_FRIEND_ID);

        ((HomeScreen) getActivity()).setHeaderLay(1);
        setHeaderTxt();
        if (AppConstants.CHAT_PIC_BACK.equals(AppConstants.SUCCESS_CODE)) {
            setPicChatChat();
        }
        setHeaderHint();
        if (mChatListAdapter != null && mChatReceiveList != null && mChatReceiveList.size() > 0) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    mChatRecyclerView.smoothScrollToPosition(mChatReceiveList.size() - 1);
                    mChatRecyclerView.getLayoutManager().smoothScrollToPosition(mChatRecyclerView, null, mChatListAdapter.getItemCount() - 1);
                }
            });


        }
    }

    private void setHeaderHint() {

        getActivity().runOnUiThread(new TimerTask() {
            @Override
            public void run() {
                mSayHiTxt.setVisibility(View.GONE);

                if (AppConstants.CHAT_FRIEND_FROM_CONNECT.equals(AppConstants.SUCCESS_CODE) && mChatReceiveList != null && chatOldSizeInt > 0) {

                    mFirstMsgHintLay.setVisibility(View.VISIBLE);
                    if (chatOldSizeInt == mChatReceiveList.size()) {
                        mHintMsgTxt.setText(getResources().getString(R.string.conversion_moved));
                    } else {
                        AppConstants.CHAT_FRIEND_FROM_CONNECT = AppConstants.FAILURE_CODE;
                        mFirstMsgHintLay.setVisibility(View.GONE);
                    }
                } else {
                    mFirstMsgHintLay.setVisibility(View.GONE);
                }

            }
        });


    }

    private void setHeaderTxt() {
        try {
            ((HomeScreen) getActivity()).setHeaderText(AppConstants.CHAT_SCREEN_TITLE);
        } catch (Exception e) {
            Log.d(AppConstants.TAG, e.toString());
        }
    }


    private void startChatReceiveAPICall() {
        mReceiveAPITimer = new Timer();
        mReceiveAPITimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isFragFocusBool) {
                    ChatSendReceiveInputEntity chatReceiveInputEntity = new ChatSendReceiveInputEntity(AppConstants.API_CHAT_RECEIVE, AppConstants.PARAMS_CHAT_RECEIVE, mUserDetailsRes.getUser_id(), AppConstants.CHAT_FRIEND_ID, AppConstants.CHAT_SUB_FRIEND, AppConstants.CHAT_ID, "");
                    APIRequestHandler.getInstance().callReceiveAPI(chatReceiveInputEntity, ChatFriendFragment.this);
                } else {
                    stopChatReceiveAPICall();
                }
            }
        }, 0, 1000);
    }

    private void setChatListAdapter(ArrayList<ChatReceiveEntity> chatList) {
        if (chatList.size() > 0) {
            if (mChatListAdapter == null) {
                mChatReceiveList.addAll(chatList);
                mChatListAdapter = new ChatAdapter(getActivity(), mChatReceiveList);
                mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                mChatRecyclerView.setAdapter(mChatListAdapter);
                mChatRecyclerView.smoothScrollToPosition(mChatReceiveList.size() - 1);
                LinearLayoutManager layoutManager = ((LinearLayoutManager) mChatRecyclerView.getLayoutManager());
                View v = layoutManager.getChildAt(0);
                if (v != null) {
                    int offsetBottom = v.getBottom();
                    layoutManager.scrollToPositionWithOffset(mChatReceiveList.size() - 1, offsetBottom);
                }
            } else {
                mChatReceiveList.addAll(chatList);
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mChatListAdapter.notifyDataSetChanged();

                    }
                });
            }
            LinearLayoutManager layoutManager = ((LinearLayoutManager) mChatRecyclerView.getLayoutManager());

            if (layoutManager != null) {
                int firstVisiblePosition = layoutManager.findLastVisibleItemPosition();

                View v = layoutManager.getChildAt(0);
                if (firstVisiblePosition > 0 && v != null) {
                    int offsetBottom = v.getBottom();
                    if (firstVisiblePosition + 1 >= 0 && mChatListAdapter.getItemCount() > 0) {
                        layoutManager.scrollToPositionWithOffset(firstVisiblePosition + 1, offsetBottom);
                    } else {
                        mChatRecyclerView.smoothScrollToPosition(offsetBottom);
                    }
                }
            }

        } else {
            mChatListAdapter = new ChatAdapter(getActivity(), mChatReceiveList);
            mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mChatRecyclerView.setAdapter(mChatListAdapter);
        }
    }

//    private void setChatListAdapter(final ArrayList<ChatReceiveEntity> chatList) {
//
//
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (chatList.size() > 0) {
//                    ArrayList<ChatReceiveEntity> mChatResponseList1 = new ArrayList<>();
//                    ArrayList<ChatReceiveEntity> tempChatList = new ArrayList<>();
//
//                    for (ChatReceiveEntity mChatResponseEntity :
//                            chatList) {
//                        tempChatList.add(mChatResponseEntity);
//                    }
//
//                    for (int i = 0; i < tempChatList.size(); i++) {
//                        ChatReceiveEntity obj = tempChatList.get(i);
//                        String str = GlobalMethods.checkBetweentime(getActivity(), obj.getDatetime());
//                        obj.setHeader_key(str);
//                        mChatResponseList1.add(obj);
//
//                    }
//                    chatList.clear();
//                    chatList.addAll(mChatResponseList1);
//
//                    final int lastChatCountInt, newChatCountInt, mFirstInt;
//                    lastChatCountInt = mChatReceiveList.size();
//                    newChatCountInt = chatList.size();
//                    mFirstInt = mChatStickyList.getFirstVisiblePosition();
//
//                    mChatReceiveList.addAll(chatList);
//
//                    if (mChatListAdapter == null) {
//                        mChatListAdapter = new ChatAdapter(getActivity(), mChatReceiveList);
//                        mChatStickyList.setAdapter(mChatListAdapter);
//                    } else {
//                        if (newChatCountInt > lastChatCountInt) {
//
//                            mChatListAdapter.notifyDataSetChanged();
//                            if (lastChatCountInt > 3) {
//                                int new_pos = mFirstInt + 2;
//                                mChatStickyList.setSelection(new_pos);
//                                mChatStickyList.findFocus();
//                            }
//
//                        }
//
//                    }
//                } else {
//                    mChatListAdapter = new ChatAdapter(getActivity(), mChatReceiveList);
//                    mChatStickyList.setAdapter(mChatListAdapter);
//                }
//            }
//        });
//
//
//    }

    @Override
    public void onRequestSuccess(Object resObj) {

        if (isFragFocusBool && resObj instanceof ChatReceiveResponse) {
            //Receive Chat API Res
            ChatReceiveResponse chatChatReceiveRes = (ChatReceiveResponse) resObj;
            if (chatChatReceiveRes.getResponse_code().equalsIgnoreCase(AppConstants.SUCCESS_CODE)) {


                mFriendDetailsRes.setFriend(chatChatReceiveRes.getExtra_response().getFriend());
                mFriendDetailsRes.setMain_picture(chatChatReceiveRes.getExtra_response().getFriend_main_picture());
                mFriendDetailsRes.setMore_mages(chatChatReceiveRes.getExtra_response().getFriend_more_mages());
                mFriendDetailsRes.setAddress(chatChatReceiveRes.getExtra_response().getFriend_address());
                mFriendDetailsRes.setHide_location(chatChatReceiveRes.getExtra_response().getFriend_location());
                mFriendDetailsRes.setInterests(chatChatReceiveRes.getExtra_response().getFriend_interests());
                AppConstants.OTHER_USER_DETAILS = new Gson().toJson(mFriendDetailsRes, UserDetailsEntity.class);


                setHeaderHint();
                if (chatChatReceiveRes.getResult().size() > 0 && !(AppConstants.CHAT_ID.equals(chatChatReceiveRes.getResult().get(chatChatReceiveRes.getResult().size() - 1).getChat_id()))) {
                    isSendPressedBool = false;
                    AppConstants.CHAT_ID = chatChatReceiveRes.getResult().get(chatChatReceiveRes.getResult().size() - 1).getChat_id();
                    setDataToDB(chatChatReceiveRes.getResult());
                }
                if (!chatChatReceiveRes.getExtra_response().getFriend().equals(getActivity().getResources().getString(R.string.three))) {

                    DialogManager.getInstance().showAlertPopup(getActivity(), getActivity().getResources().getString(R.string.user_del_msg));
                    getActivity().onBackPressed();

                }
            }

        }

    }

    private void setDataToDB(ArrayList<ChatReceiveEntity> chatModel) {

        if (chatModel.size() > 0) {

            UserDetailsEntity userDetailsRes = GlobalMethods.getUserDetailsRes(getActivity());
            ArrayList<ChatReceiveEntity> chatArrList = new ArrayList<>();
            String videoIDStr = "", attachmentTypeStr = "", attachmentURLImgStr = "", attachmentTitleStr = "";

            for (int i = 0; i < chatModel.size(); i++) {
                ChatReceiveEntity model = new ChatReceiveEntity();
                boolean isUserBool = userDetailsRes.getUser_id().equals(chatModel.get(i).getUser_id());

                model.setUser_id(userDetailsRes.getUser_id());
                model.setUsername(userDetailsRes.getUsername());

                model.setFriend_id(isUserBool ? chatModel.get(i).getFriend_id() : chatModel.get(i).getUser_id());
                model.setFriendname(isUserBool ? chatModel.get(i).getFriendname() : chatModel.get(i).getUsername());


                model.setUserimage(chatModel.get(i).getUserimage());
                model.setFriendimage(chatModel.get(i).getFriendimage());
                model.setMsg_sent_user(chatModel.get(i).getUser_id());
                model.setSubject(AppConstants.CHAT_SUB_FRIEND);
                model.setChat_id(chatModel.get(i).getChat_id());
                model.setMessage(chatModel.get(i).getMessage());
                model.setDatetime(chatModel.get(i).getDatetime());


                attachmentTypeStr = chatModel.get(i).getIs_attachement();
                attachmentURLImgStr = chatModel.get(i).getAttachement_url();
                attachmentTitleStr = chatModel.get(i).getMessage();

                if (GlobalMethods.getMsgDecoder(model.getMessage()).contains(getResources().getString(R.string.youtube_link)) || GlobalMethods.getMsgDecoder(model.getMessage()).contains(getResources().getString(R.string.youtube)) || GlobalMethods.getMsgDecoder(model.getMessage()).contains(getResources().getString(R.string.ph_youtube_format))) {
                    videoIDStr = GlobalMethods.getYoutubeVideoID(GlobalMethods.getMsgDecoder(model.getMessage()));
//                    attachmentTitleStr = GlobalMethods.getTitleQuietly(GlobalMethods.getMsgDecoder(model.getMessage()));
                    attachmentURLImgStr = "http://img.youtube.com/vi/" + videoIDStr + "/0.jpg";
                    attachmentTypeStr = getResources().getString(R.string.three);
                    try {
                        attachmentTitleStr = GlobalMethods.getYoutubeTitle(videoIDStr);
                    } catch (Exception e) {
                        Log.d(AppConstants.TAG, e.getMessage());
                    }
                }

                model.setVideo_id(videoIDStr);
                model.setAttachement_url(attachmentURLImgStr);
                model.setIs_attachement(attachmentTypeStr);
                model.setAttachement_title(attachmentTitleStr);


                ChatTable.updateChatTable(model);
                ChatTable.updateMaxChatIDTable(model);
                chatArrList.add(model);
            }

            setChatListAdapter(chatArrList);
        }
    }

//    private void setDataToDB(ArrayList<ChatReceiveEntity> chatModel) {
//
//        if (chatModel.size() > 0) {
//
//            UserDetailsEntity userDetailsRes = GlobalMethods.getUserDetailsRes(getActivity());
//            ArrayList<ChatReceiveEntity> chatArrList = new ArrayList<>();
//
//            for (int i = 0; i < chatModel.size(); i++) {
//                ChatReceiveEntity model = new ChatReceiveEntity();
//                boolean isUserBool = userDetailsRes.getUser_id().equals(chatModel.get(i).getUser_id());
//
//                model.setUser_id(userDetailsRes.getUser_id());
//                model.setUsername(userDetailsRes.getUsername());
//                model.setUserimage(userDetailsRes.getMain_picture());
//
//                model.setFriend_id(isUserBool ? chatModel.get(i).getFriend_id() : chatModel.get(i).getUser_id());
//                model.setFriendname(isUserBool ? chatModel.get(i).getFriendname() : chatModel.get(i).getUsername());
//                model.setFriendimage(isUserBool ? chatModel.get(i).getFriendimage() : chatModel.get(i).getUserimage());
////                model.setMsg_sent_user(chatModel.get(i).getMsg_sent_user().equals(AppConstants.SUCCESS_CODE) ? chatModel.get(i).getUser_id() : chatModel.get(i).getFriend_id());
//                model.setMsg_sent_user(chatModel.get(i).getUser_id());
//
//                model.setSubject(chatModel.get(i).getSubject());
//                model.setChat_id(chatModel.get(i).getChat_id());
//                model.setMessage(chatModel.get(i).getMessage());
//                model.setDatetime(chatModel.get(i).getDatetime());
//
//                ChatTable.updateChatTable(model);
//                ChatTable.updateMaxChatIDTable(model);
//                chatArrList.add(model);
//            }
//
//            setChatListAdapter(chatArrList);
//        }
//
//    }

    private void getDataFromDB(String userIdStr, String friendIdStr) {

        ChatReceiveEntity model = new ChatReceiveEntity();
        model.setUser_id(userIdStr);
        model.setFriend_id(friendIdStr);
        model.setSubject(AppConstants.CHAT_SUB_FRIEND);

        ArrayList<ChatReceiveEntity> mMsgList = ChatTable.getChatMessageList(model);
        chatOldSizeInt = mMsgList.size();
        String chatIDStr = ChatTable.getChatMaxID(model);

        if (!chatIDStr.isEmpty() && Integer.valueOf(chatIDStr) > 0) {
            AppConstants.CHAT_ID = chatIDStr;
        } else {
            AppConstants.CHAT_ID = AppConstants.FAILURE_CODE;
        }
        mChatReceiveList = new ArrayList<>();
        mChatListAdapter = null;
        setChatListAdapter(mMsgList);

        startChatReceiveAPICall();

    }

    @Override
    public void onRequestFailure(Throwable t) {
        if (isSendPressedBool) {
            isSendPressedBool = false;
        }
    }

    @Override
    public void onYesClick() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isFragFocusBool = false;
        stopChatReceiveAPICall();
    }

    private void stopChatReceiveAPICall() {
        if (mReceiveAPITimer != null) {
            mReceiveAPITimer.cancel();
            mReceiveAPITimer.purge();
        }
    }


}
