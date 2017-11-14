package com.smaat.spark.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.smaat.spark.R;
import com.smaat.spark.adapter.ChatAdapter;
import com.smaat.spark.database.ChatTable;
import com.smaat.spark.entity.inputEntity.AnonymousInputEntity;
import com.smaat.spark.entity.inputEntity.ChatConnDisInputEntity;
import com.smaat.spark.entity.inputEntity.ChatSendReceiveInputEntity;
import com.smaat.spark.entity.outputEntity.ChatReceiveEntity;
import com.smaat.spark.entity.outputEntity.UserDetailsEntity;
import com.smaat.spark.main.BaseFragment;
import com.smaat.spark.model.AnonymousResponse;
import com.smaat.spark.model.ChatConnectResponse;
import com.smaat.spark.model.ChatReceiveResponse;
import com.smaat.spark.model.CommonModel;
import com.smaat.spark.model.CommonResponse;
import com.smaat.spark.services.APIRequestHandler;
import com.smaat.spark.ui.ChatImageScreen;
import com.smaat.spark.ui.HomeScreen;
import com.smaat.spark.utils.AppConstants;
import com.smaat.spark.utils.DialogManager;
import com.smaat.spark.utils.GlobalMethods;
import com.smaat.spark.utils.InterfaceBtnCallback;
import com.smaat.spark.utils.InterfaceTwoBtnCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.smaat.spark.R.string.search_user;


public class ChatConnectFragment extends BaseFragment implements InterfaceBtnCallback {


    @BindView(R.id.chat_list)
    RecyclerView mChatRecyclerView;

    @BindView(R.id.chat_edt)
    EditText mChatEdt;

    @BindView(R.id.send_btn)
    Button mSendBtn;

    @BindView(R.id.first_msg_hint_lay)
    LinearLayout mFirstMsgHintLay;

    @BindView(R.id.hint_msg_txt)
    TextView mHintMsgTxt;

    @BindView(R.id.say_hi_txt)
    TextView mSayHiTxt;

    private int mConnectAPICountInt = 1;
    private boolean isConnectedBool = false, isBackPressedBool = false, isSendPressedBool = false,
            isAddAPIPressedBool = false, isNextPressedBool = false, isCheckDBDataBool = false, isFragFocusBool = false;
    private UserDetailsEntity mUserDetailsRes;
    private ChatAdapter mChatListAdapter;
    private ArrayList<ChatReceiveEntity> mChatReceiveList;
    private UserDetailsEntity mFriendDetailsRes = new UserDetailsEntity();
    private Timer mConnectAPITimer, mReceiveAPITimer;
    private String mCFriendIDStr = "", mCFriendNameStr = "", mCFriendSubStr;
    private SimpleDateFormat mChatTargetDateTime = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
    private boolean isAnonymousBool = false;


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
//
        mChatReceiveList = new ArrayList<>();
        AppConstants.CHAT_ID = AppConstants.FAILURE_CODE;
        AppConstants.CHAT_SCREEN_TITLE = GlobalMethods.getStringValue(getActivity(), AppConstants.CHAT_CONNECTED_STATUS).equals(getResources().getString(R.string.two)) ? GlobalMethods.getStringValue(getActivity(), AppConstants.CHAT_CONNECT_FRIEND_NAME) : getResources().getString(search_user);

    }


    private void initView() {
        mUserDetailsRes = GlobalMethods.getUserDetailsRes(getActivity());

        ((HomeScreen) getActivity()).mChatHeaderTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectedBool && !isNextPressedBool && !((HomeScreen) getActivity()).mChatHeaderTxt.getText().toString().equals(getResources().getString(R.string.anonymous))) {
                    AppConstants.OTHER_USER_DETAILS = new Gson().toJson(mFriendDetailsRes, UserDetailsEntity.class);

                    AppConstants.CHAT_USER_BACK = AppConstants.SUCCESS_CODE;
                    ((HomeScreen) getActivity()).replaceFragment(new UserDetailsFragment(), 1);
                }

            }
        });
        ((HomeScreen) getActivity()).mHeaderTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectedBool && !isNextPressedBool) {

                    if (((HomeScreen) getActivity()).mHeaderTxt.getText().toString().equals(getResources().getString(R.string.anonymous))) {
                        DialogManager.getInstance().showOptionPopup(getActivity(), getResources().getString(R.string.ignore_friend), getString(R.string.ignore), getString(R.string.cancel), new InterfaceTwoBtnCallback() {
                            @Override
                            public void onYesClick() {
                                ChatSendReceiveInputEntity friendListInputEntityRes = new ChatSendReceiveInputEntity(AppConstants.API_IGNORE_FRIEND, AppConstants.PARAMS_IGNORE_FRIEND, GlobalMethods.getUserID(getActivity()), mCFriendIDStr, AppConstants.FAILURE_CODE, "");
                                APIRequestHandler.getInstance().callAddFriendAPI(friendListInputEntityRes, ChatConnectFragment.this);
                            }

                            @Override
                            public void onNoClick() {

                            }
                        });
                    } else {
                        AppConstants.OTHER_USER_DETAILS = new Gson().toJson(mFriendDetailsRes, UserDetailsEntity.class);

                        AppConstants.CHAT_USER_BACK = AppConstants.SUCCESS_CODE;
                        ((HomeScreen) getActivity()).replaceFragment(new UserDetailsFragment(), 1);


                    }
                }
            }
        });
        ((HomeScreen) getActivity()).mHeaderLeftBtnLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isNextPressedBool = false;
                backPressed();
            }
        });
        ((HomeScreen) getActivity()).mHeaderEndLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isNextPressedBool = false;
                backPressed();
            }
        });

        ((HomeScreen) getActivity()).mHeaderNextLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectedBool) {
                    isNextPressedBool = true;
                    backPressed();
                }
            }
        });
        ((HomeScreen) getActivity()).mHeaderInviteLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectedBool && !isNextPressedBool && ((HomeScreen) getActivity()).mFriendAddImg.getTag().equals(1)) {
                    isAddAPIPressedBool = true;
                    ChatSendReceiveInputEntity friendListInputEntityRes = new ChatSendReceiveInputEntity(AppConstants.API_ADD_FRIEND, AppConstants.PARAMS_ADD_FRIEND, GlobalMethods.getUserID(getActivity()), mCFriendIDStr);
                    APIRequestHandler.getInstance().callAddFriendAPI(friendListInputEntityRes, ChatConnectFragment.this);
                }
            }
        });

        ((HomeScreen) getActivity()).mHeaderVisibleLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnectedBool && !isNextPressedBool) {
                    AnonymousInputEntity anonymousInputEntityRes = new AnonymousInputEntity(AppConstants.API_ANONYMOUS, AppConstants.PARAMS_ANONYMOUS, GlobalMethods.getUserID(getActivity()), AppConstants.FAILURE_CODE);
                    APIRequestHandler.getInstance().callAnonymousAPI(anonymousInputEntityRes, ChatConnectFragment.this);
                }
            }
        });


        ((HomeScreen) getActivity()).mHeaderVisibleLay.setVisibility(View.GONE);
        ((HomeScreen) getActivity()).mHeaderInviteLay.setVisibility(View.GONE);
    }

    @OnClick({R.id.send_btn, R.id.camera_img})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                sendMsg();
                break;
            case R.id.camera_img:

                if (isConnectedBool && !isNextPressedBool) {
                    AppConstants.CHAT_PIC_FRIEND_ID = mCFriendIDStr;
                    AppConstants.CHAT_PIC_SUB = mCFriendSubStr;
                    AppConstants.CHAT_PIC_MSG_FLOW = AppConstants.SUCCESS_CODE;
                    AppConstants.CHAT_PIC_BACK = AppConstants.SUCCESS_CODE;
                    Intent intent;
                    intent = new Intent(getActivity(), ChatImageScreen.class);
//                    intent = new Intent(getActivity(), ChatImageScreenn.class);
                    getActivity().startActivity(intent);
                }
                break;

        }
    }

    private void sendMsg() {
        if (GlobalMethods.isNetworkAvailable(getActivity())) {
            String chatStr = mChatEdt.getText().toString().trim();
            if (!chatStr.isEmpty()) {
                isSendPressedBool = true;
                ChatSendReceiveInputEntity chatInputEntity = new ChatSendReceiveInputEntity(AppConstants.API_CHAT_SEND, AppConstants.PARAMS_CHAT_SEND, mUserDetailsRes.getUser_id(), mCFriendIDStr,
                        GlobalMethods.setMsgEncoder(chatStr), mCFriendSubStr, AppConstants.SUCCESS_CODE, AppConstants.FAILURE_CODE, "");
                APIRequestHandler.getInstance().callSendAPI(chatInputEntity, this);

                ArrayList<ChatReceiveEntity> chatSendRes = new ArrayList<>();
                ChatReceiveEntity chatEntitySendRes = new ChatReceiveEntity();

                chatEntitySendRes.setFriend_id(mCFriendIDStr);
                chatEntitySendRes.setUser_id(mUserDetailsRes.getUser_id());
                chatEntitySendRes.setSubject(mCFriendSubStr);
                chatEntitySendRes.setChat_id(getString(R.string.nag_val));
                chatEntitySendRes.setMessage(GlobalMethods.setMsgEncoder(chatStr));
                chatEntitySendRes.setFriendname(mCFriendNameStr);
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

    private void setDiscoverChat() {

        ArrayList<ChatReceiveEntity> chatSendRes = new ArrayList<>();
        ChatReceiveEntity chatEntitySendRes = new ChatReceiveEntity();

        chatEntitySendRes.setFriend_id(mCFriendIDStr);
        chatEntitySendRes.setUser_id(mUserDetailsRes.getUser_id());
        chatEntitySendRes.setSubject(mCFriendSubStr);
        chatEntitySendRes.setChat_id(getString(R.string.nag_val));
        chatEntitySendRes.setMessage(AppConstants.YOUTUBE_VIDEO_URL);
        chatEntitySendRes.setFriendname(mCFriendNameStr);
        chatEntitySendRes.setUsername(mUserDetailsRes.getUsername());
        chatEntitySendRes.setUserimage(mUserDetailsRes.getMain_picture());
        chatEntitySendRes.setFriendimage(mFriendDetailsRes.getMain_picture());
        chatEntitySendRes.setDatetime(GlobalMethods.getCurrentDate());
        chatEntitySendRes.setMsg_sent_user(AppConstants.SUCCESS_CODE);
        chatEntitySendRes.setIs_attachement(AppConstants.FAILURE_CODE);
        chatEntitySendRes.setAttachement_url("");

        chatSendRes.add(chatEntitySendRes);
        setDataToDB(chatSendRes);
    }

    private void setPicChatChat() {

        AppConstants.CHAT_PIC_BACK = AppConstants.FAILURE_CODE;
        ArrayList<ChatReceiveEntity> chatSendRes = new ArrayList<>();
        ChatReceiveEntity chatEntitySendRes = new ChatReceiveEntity();

        chatEntitySendRes.setFriend_id(mCFriendIDStr);
        chatEntitySendRes.setUser_id(mUserDetailsRes.getUser_id());
        chatEntitySendRes.setSubject(mCFriendSubStr);
        chatEntitySendRes.setChat_id(getString(R.string.nag_val));
        chatEntitySendRes.setMessage("");
        chatEntitySendRes.setFriendname(mCFriendNameStr);
        chatEntitySendRes.setUsername(mUserDetailsRes.getUsername());
        chatEntitySendRes.setUserimage(mUserDetailsRes.getMain_picture());
        chatEntitySendRes.setFriendimage(mFriendDetailsRes.getMain_picture());
        chatEntitySendRes.setDatetime(GlobalMethods.getCurrentDate());
        chatEntitySendRes.setMsg_sent_user(AppConstants.SUCCESS_CODE);
        chatEntitySendRes.setIs_attachement(getResources().getString(R.string.two));
        chatEntitySendRes.setAttachement_title(AppConstants.CHAT_PIC_TXT);
        chatEntitySendRes.setAttachement_url(AppConstants.CHAT_PIC_LOCAL_PATH);
//        chatEntitySendRes.setTe(AppConstants.CHAT_PIC_LOCAL_PATH);

        chatSendRes.add(chatEntitySendRes);
        setDataToDB(chatSendRes);
    }

    private void setBtnBackground() {
        if (!isBackPressedBool) {
            if (!isConnectedBool) {
                mChatEdt.setFocusable(false);
                mChatEdt.setFocusableInTouchMode(false);
                mChatEdt.setClickable(false);
                mSendBtn.setClickable(false);
                mSendBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rounded_grey_bg));

            } else {
                mChatEdt.setFocusable(true);
                mChatEdt.setFocusableInTouchMode(true);
                mChatEdt.setClickable(true);
                mSendBtn.setClickable(true);
                mSendBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.sky_blue_btn));
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        isFragFocusBool = true;

        AppConstants.CHAT_BACK_PRESSED = AppConstants.SUCCESS_CODE;
        ((HomeScreen) getActivity()).setHeaderLeftClick(true);
        ((HomeScreen) getActivity()).setHeadRigImgVisible(false, R.mipmap.app_icon);


        isConnectedBool = GlobalMethods.getStringValue(getActivity(), AppConstants.CHAT_CONNECTED_STATUS).equals(getResources().getString(R.string.two));
        mCFriendIDStr = GlobalMethods.getStringValue(getActivity(), AppConstants.CHAT_CONNECT_FRIEND_ID);
        mCFriendNameStr = GlobalMethods.getStringValue(getActivity(), AppConstants.CHAT_CONNECT_FRIEND_NAME);
        mCFriendSubStr = GlobalMethods.getStringValue(getActivity(), AppConstants.CHAT_CONNECT_SUB);


        initView();

        if (isConnectedBool && !isBackPressedBool) {
            mChatListAdapter = null;
            stopChatReceiveAPICall();
            getDataFromDB(GlobalMethods.getUserID(getActivity()), mCFriendIDStr);
        } else if (!isConnectedBool) {
            setBtnBackground();
            if (mConnectAPICountInt == 6) {
                stopChatConnectAPICall();
                mBaseFragDialog = DialogManager.getInstance().showOptionPopup(getActivity(), getString(R.string.no_users_avail), getString(R.string.try_again), getString(R.string.go_back), new InterfaceTwoBtnCallback() {
                    @Override
                    public void onYesClick() {
                        mConnectAPICountInt = 1;
                        startChatConnectAPICall();
                    }

                    @Override
                    public void onNoClick() {
                        backPressed();
                    }
                });
            } else {
                startChatConnectAPICall();
            }
        }

        ((HomeScreen) getActivity()).setHeaderLay(2);
        ((HomeScreen) getActivity()).mHeaderEndLay.setVisibility(View.VISIBLE);
        setHeaderTxt();
        ((HomeScreen) getActivity()).mNextTxt.setText(getString(R.string.next));
        ((HomeScreen) getActivity()).mNextTxt.setTextColor(ContextCompat.getColor(getActivity(), R.color.screen_bg));


        if (AppConstants.CHAT_PIC_BACK.equals(AppConstants.SUCCESS_CODE) && isConnectedBool && !isNextPressedBool) {
            setPicChatChat();
        }
//        if (mChatListAdapter != null && mChatReceiveList != null && mChatReceiveList.size() > 0) {
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
////                    mChatRecyclerView.smoothScrollToPosition(mChatReceiveList.size() - 1);
//                    mChatRecyclerView.getLayoutManager().smoothScrollToPosition(mChatRecyclerView, null, mChatReceiveList.size() - 1);
////                    mChatRecyclerView.getLayoutManager().smoothScrollToPosition(mChatRecyclerView, null, mChatListAdapter.getItemCount() - 1);
//                }
//            });
//
//
//        }
    }

    private void startChatConnectAPICall() {

        mConnectAPITimer = new Timer();
        mConnectAPITimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if (mConnectAPICountInt == 6 || !isFragFocusBool) {
                    stopChatConnectAPICall();
                } else {
                    if (!isConnectedBool && !isBackPressedBool) {
                        String randomStr = "";
                        if (mConnectAPICountInt < 6) {
                            mConnectAPICountInt++;
                            AppConstants.CHAT_SCREEN_TITLE = getString(search_user);
                            randomStr = AppConstants.FAILURE_CODE;
                        }
                        if (mConnectAPICountInt == 6) {
                            AppConstants.CHAT_SCREEN_TITLE = getString(R.string.search_random_user);
                            randomStr = AppConstants.SUCCESS_CODE;
                        }
//                        if (isNextPressedBool) {
//                            mConnectAPICountInt = 1;
//                            randomStr = AppConstants.SUCCESS_CODE;
//                        }
                        if (mConnectAPICountInt <= 6) {
                            ChatConnDisInputEntity chatConnDisInputEntityRes = new ChatConnDisInputEntity(AppConstants.API_CHAT_CONNECT, AppConstants.PARAMS_CHAT_CONNECT, mUserDetailsRes.getUser_id(),
                                    AppConstants.FAILURE_CODE, mCFriendSubStr, AppConstants.CHAT_DISTANCE, mUserDetailsRes.getLat(), mUserDetailsRes.getLon(), randomStr, AppConstants.CONNECT_USER_ANONYMOUS);
                            APIRequestHandler.getInstance().callConnectAPI(chatConnDisInputEntityRes, ChatConnectFragment.this);
                        }
                    }
                    setHeaderTxt();
                }
            }
        }, 0, 2000);
    }


    private void startChatReceiveAPICall() {
        mReceiveAPITimer = new Timer();
        mReceiveAPITimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isConnectedBool && !isBackPressedBool && isFragFocusBool) {

                    ChatSendReceiveInputEntity chatReceiveInputEntity = new ChatSendReceiveInputEntity(AppConstants.API_CHAT_RECEIVE, AppConstants.PARAMS_CHAT_RECEIVE, mUserDetailsRes.getUser_id(), mCFriendIDStr, mCFriendSubStr, AppConstants.CHAT_ID, "");
                    APIRequestHandler.getInstance().callReceiveAPI(chatReceiveInputEntity, ChatConnectFragment.this);
                } else {
                    stopChatReceiveAPICall();
                }
            }
        }, 0, 1500);
    }


    private void setHeaderTxt() {
        if (isFragFocusBool) {

            getActivity().runOnUiThread(new TimerTask() {
                @Override
                public void run() {
                    try {

                        String headerStr = AppConstants.CHAT_SCREEN_TITLE;
                        if (!AppConstants.CHAT_SCREEN_TITLE.equals(getResources().getString(R.string.user_disconnect)) && !AppConstants.CHAT_SCREEN_TITLE.equals(getResources().getString(R.string.search_user)) && AppConstants.CONNECT_FRIEND_ANONYMOUS.equals(AppConstants.SUCCESS_CODE)) {
                            headerStr = getResources().getString(R.string.anonymous);
                        }
                        ((HomeScreen) getActivity()).mHeaderNextLay.setVisibility(AppConstants.CHAT_SCREEN_TITLE.equals(getResources().getString(R.string.search_user)) ? View.INVISIBLE : View.VISIBLE);
                        ((HomeScreen) getActivity()).mChatHeaderTxt.setText(headerStr);
                        ((HomeScreen) getActivity()).setHeaderText(headerStr);

                    } catch (Exception e) {
                        Log.d(AppConstants.TAG, e.toString());
                    }
                }
            });

        }
    }

    private void setHintHeaderTxt(final boolean isFriendBool, final boolean isRandomBool, final boolean isFirstLayVisibleBool) {

        getActivity().runOnUiThread(new TimerTask() {
            @Override
            public void run() {
                mFirstMsgHintLay.setVisibility(isFirstLayVisibleBool ? View.VISIBLE : View.GONE);
                String connectHintStr, interestHintStr, firstMsgHintStr;
                connectHintStr = String.format(getResources().getString(R.string.connect_hint_msg), ((HomeScreen) getActivity()).mChatHeaderTxt.getText().toString().trim().equals(getResources().getString(R.string.anonymous)) ? getResources().getString(R.string.anonymous) : mCFriendNameStr);
                interestHintStr = String.format(getResources().getString(R.string.interest_hint_msg), mCFriendSubStr);
                firstMsgHintStr = connectHintStr + " " + interestHintStr;
                if (isFriendBool) {
                    firstMsgHintStr = getResources().getString(R.string.conversion_moved);
                } else if (isRandomBool) {
                    firstMsgHintStr = connectHintStr + " " + getResources().getString(R.string.random_hint_msg);
                }
                mHintMsgTxt.setText(firstMsgHintStr);
                mSayHiTxt.setVisibility(isFriendBool ? View.GONE : View.VISIBLE);
            }
        });


    }

    private void setChatListAdapter(ArrayList<ChatReceiveEntity> chatList) {
        if (chatList.size() > 0) {
            mChatReceiveList.addAll(chatList);
            checkAnonymousAdapterData();

            if (mChatListAdapter == null) {
                mChatListAdapter = new ChatAdapter(getActivity(), mChatReceiveList);
                mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                mChatRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mChatRecyclerView.setAdapter(mChatListAdapter);

//                mChatRecyclerView.getLayoutManager().smoothScrollToPosition(mChatRecyclerView, null, mChatReceiveList.size() - 1);
//                LinearLayoutManager layoutManager = ((LinearLayoutManager) mChatRecyclerView.getLayoutManager());
//                View v = layoutManager.getChildAt(0);
//                if (v != null) {
//                    int offsetBottom = v.getBottom();
//                    layoutManager.scrollToPositionWithOffset(mChatReceiveList.size() - 1, offsetBottom);
//                }

            } else {
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
//                        mChatRecyclerView.smoothScrollToPosition(offsetBottom);

                        mChatRecyclerView.getLayoutManager().smoothScrollToPosition(mChatRecyclerView, null, mChatListAdapter.getItemCount() - 1);
//                        layoutManager.scrollToPositionWithOffset(mChatReceiveList.size() - 1, offsetBottom);
                    }
                }
            }

        } else {
            mChatListAdapter = new ChatAdapter(getActivity(), mChatReceiveList);
            mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mChatRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mChatRecyclerView.setAdapter(mChatListAdapter);

        }
    }

//    private void setChatListAdapter(ArrayList<ChatReceiveEntity> chatList) {
//        if (chatList.size() > 0) {
//
//            ArrayList<ChatReceiveEntity> mChatResponseList1 = new ArrayList<>();
//            ArrayList<ChatReceiveEntity> tempChatList = new ArrayList<>();
//
//            for (ChatReceiveEntity mChatResponseEntity :
//                    chatList) {
//                tempChatList.add(mChatResponseEntity);
//            }
//
//            for (int i = 0; i < tempChatList.size(); i++) {
//                ChatReceiveEntity obj = tempChatList.get(i);
//                String str = GlobalMethods.checkBetweentime(getActivity(), obj.getDatetime());
//                obj.setHeader_key(str);
//                mChatResponseList1.add(obj);
//
//            }
//
//
////            for (int i = 0; i < chatList.size(); i++) {
////                ChatReceiveEntity obj = chatList.get(i);
////                String str = GlobalMethods.checkBetweentime(getActivity(), obj.getDatetime());
////                obj.setHeader_key(str);
////                mChatResponseList1.add(obj);
////
////            }
//
//
//            mChatReceiveList.addAll(chatList);
//            checkAnonymousAdapterData();
//
//            if (mChatListAdapter == null) {
//                mChatListAdapter = new ChatAdapter(getActivity(), mChatReceiveList);
//                mChatStickyList.setLayoutManager(new LinearLayoutManager(getActivity()));
//                mChatStickyList.setAdapter(mChatListAdapter);
//                mChatStickyList.smoothScrollToPosition(mChatReceiveList.size() - 1);
//            } else {
//                getActivity().runOnUiThread(new Runnable() {
//                    public void run() {
//                        mChatListAdapter.notifyDataSetChanged();
//
//                    }
//                });
//            }
//            LinearLayoutManager layoutManager = ((LinearLayoutManager) mChatStickyList.getLayoutManager());
//
//            if (layoutManager != null) {
//                int firstVisiblePosition = layoutManager.findLastVisibleItemPosition();
//
//                View v = layoutManager.getChildAt(0);
//                if (firstVisiblePosition > 0 && v != null) {
//                    int offsetBottom = v.getBottom();
//                    if (firstVisiblePosition + 1 >= 0 && mChatListAdapter.getItemCount() > 0) {
//                        layoutManager.scrollToPositionWithOffset(firstVisiblePosition + 1, offsetBottom);
//                    } else {
//                        mChatRecyclerView.smoothScrollToPosition(offsetBottom);
//                    }
//                }
//            }
//
//        } else {
//            mChatListAdapter = new ChatAdapter(getActivity(), mChatReceiveList);
//            mChatStickyList.setAdapter(mChatListAdapter);
//        }
//    }


//    private void checkAnonymousAdapterData() {
//
//        for (int posInt = 0; posInt < mChatReceiveList.size(); posInt++) {
//            mChatReceiveList.get(posInt).setUser_connect_anonymous(AppConstants.CONNECT_USER_ANONYMOUS);
//            mChatReceiveList.get(posInt).setFriend_connect_anonymous(AppConstants.CONNECT_FRIEND_ANONYMOUS);
//        }
//        if (mChatListAdapter != null && mChatReceiveList.size() > 0)
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    mChatListAdapter.notifyDataSetChanged();
//                }
//            });
//    }


    private void checkAnonymousAdapterData() {

        if (isAnonymousBool) {

            for (int posInt = 0; posInt < mChatReceiveList.size(); posInt++) {

                ChatReceiveEntity chatData = mChatReceiveList.get(posInt);
                String headerDateStr = AppConstants.SUCCESS_CODE, msgDateVisibleStr = AppConstants.SUCCESS_CODE;

                chatData.setUser_connect_anonymous(AppConstants.CONNECT_USER_ANONYMOUS);
                chatData.setFriend_connect_anonymous(AppConstants.CONNECT_FRIEND_ANONYMOUS);

//            if (GlobalMethods.getMsgDecoder(chatData.getMessage()).contains(getResources().getString(R.string.youtube_link)) || GlobalMethods.getMsgDecoder(chatData.getMessage()).contains(getResources().getString(R.string.youtube)) || GlobalMethods.getMsgDecoder(chatData.getMessage()).contains(getResources().getString(R.string.ph_youtube_format))) {
//                videoIDStr = GlobalMethods.getYoutubeVideoID(GlobalMethods.getMsgDecoder(chatData.getMessage()));
//                attachmentTitleStr = GlobalMethods.getYoutubeTitle(videoIDStr);
//                attachmentURLImgStr = "http://img.youtube.com/vi/" + videoIDStr + "/0.jpg";
//            }

                if (posInt > 0 && chatData.getMsg_sent_user().equalsIgnoreCase(mChatReceiveList.get(posInt - 1).getMsg_sent_user()) && GlobalMethods.getCustomDateFormat(chatData.getDatetime(), mChatTargetDateTime).equalsIgnoreCase(GlobalMethods.getCustomDateFormat(mChatReceiveList.get(posInt - 1).getDatetime(), mChatTargetDateTime))) {
                    msgDateVisibleStr = AppConstants.FAILURE_CODE;
                }

                if (posInt > 0 && mChatReceiveList.get(posInt - 1).getHeader_datetime().equals(GlobalMethods.checkBetweentime(getActivity(), chatData.getDatetime()))) {
                    headerDateStr = AppConstants.FAILURE_CODE;
                }


//            chatData.setVideo_id(videoIDStr);
//            chatData.setAttachement_url(attachmentURLImgStr);
//            chatData.setAttachement_title(attachmentTitleStr);

                chatData.setDatetimeVisible(msgDateVisibleStr);
                chatData.setHeader_datetimeVisible(headerDateStr);
                chatData.setHeader_datetime(GlobalMethods.checkBetweentime(getActivity(), chatData.getDatetime()));

                mChatReceiveList.set(posInt, chatData);
            }


            if (mChatListAdapter != null && mChatReceiveList.size() > 0)
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChatListAdapter.notifyDataSetChanged();
                    }
                });

            if (AppConstants.CONNECT_USER_ANONYMOUS.equals(AppConstants.SUCCESS_CODE) && AppConstants.CONNECT_FRIEND_ANONYMOUS.equals(AppConstants.SUCCESS_CODE)) {
                isAnonymousBool = true;
            }
        }
    }


    @Override
    public void onYesClick() {

    }

    private void backPressed() {
        isBackPressedBool = true;
        hideSoftKeyboard();
        ChatConnDisInputEntity chatDisConInputEntityRes = new ChatConnDisInputEntity(AppConstants.API_CHAT_DISCONNECT, AppConstants.PARAMS_USER_ID, mUserDetailsRes.getUser_id(), AppConstants.FAILURE_CODE);
        APIRequestHandler.getInstance().callDisConnectAPI(chatDisConInputEntityRes, this);
    }

    @Override
    public void onRequestSuccess(Object resObj) {
        if (isFragFocusBool) {
            if (resObj instanceof ChatConnectResponse) {
                ChatConnectResponse chatConnDisRes = (ChatConnectResponse) resObj;

                if (chatConnDisRes.getResponse_code().equalsIgnoreCase(AppConstants.SUCCESS_CODE)) {
                    baseAlertDismiss();
                    if (chatConnDisRes.getResult().size() > 0) {
                        stopChatConnectAPICall();
                        isConnectedBool = true;
                        isNextPressedBool = false;
                        UserDetailsEntity chatConnectRes = chatConnDisRes.getResult().get(0);
                        mFriendDetailsRes = chatConnectRes;
                        AppConstants.OTHER_USER_DETAILS = new Gson().toJson(mFriendDetailsRes, UserDetailsEntity.class);
                        mCFriendSubStr = chatConnectRes.getSubject();
                        mCFriendIDStr = chatConnectRes.getUser_id();
                        mCFriendNameStr = chatConnectRes.getUsername();


                        //Delete old data from DB
                        ChatReceiveEntity deleteEntityRes = new ChatReceiveEntity();
                        deleteEntityRes.setUser_id(GlobalMethods.getUserID(getActivity()));
                        deleteEntityRes.setFriend_id(mCFriendIDStr);
                        deleteEntityRes.setSubject(AppConstants.CHAT_SUB_FRIEND);
                        ChatTable.deleteChatMessageList(deleteEntityRes);


                        AppConstants.CHAT_SCREEN_TITLE = chatConnectRes.getUsername();
                        AppConstants.CONNECT_FRIEND_ANONYMOUS = chatConnectRes.getConnect_anon_status();
                        setHeaderTxt();
                        setBtnBackground();
                        setHintHeaderTxt(chatConnectRes.getFriend().equals(getString(R.string.three)), chatConnectRes.getRandom().equals(AppConstants.SUCCESS_CODE), true);
                        mCFriendSubStr = chatConnectRes.getFriend().equals(getString(R.string.three)) ? AppConstants.CHAT_SUB_FRIEND : mCFriendSubStr;
//                    ((HomeScreen) getActivity()).setHeaderLay(AppConstants.CHAT_IS_FRIEND.equals(AppConstants.SUCCESS_CODE) ? 1 : 2);
                        ((HomeScreen) getActivity()).mHeaderInviteLay.setVisibility(chatConnectRes.getFriend().equalsIgnoreCase(AppConstants.FAILURE_CODE) || chatConnectRes.getFriend().equalsIgnoreCase(getResources().getString(R.string.four)) ? View.VISIBLE : View.GONE);
                        ((HomeScreen) getActivity()).mHeaderVisibleLay.setVisibility(AppConstants.CONNECT_USER_ANONYMOUS.equals(AppConstants.SUCCESS_CODE) ?
                                View.VISIBLE : View.INVISIBLE);
                        if (chatConnectRes.getFriend().equals(getString(R.string.three))) {
                            ((HomeScreen) getActivity()).mHeaderVisibleLay.setVisibility(View.GONE);
                        } else if (!chatConnectRes.getFriend().equals(AppConstants.FAILURE_CODE)) {
                            ((HomeScreen) getActivity()).mHeaderVisibleLay.setVisibility(View.GONE);

                        }
                        GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECTED_STATUS, getResources().getString(R.string.two));

                        GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECT_SUB, mCFriendSubStr);
                        GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECT_FRIEND_ID, mCFriendIDStr);
                        GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECT_FRIEND_NAME, mCFriendNameStr);

                        ((HomeScreen) getActivity()).mFriendAddImg.setImageResource(chatConnectRes.getConnect_anon_status().equalsIgnoreCase(AppConstants.SUCCESS_CODE) ? R.drawable.friend_add_close_img : R.drawable.friend_add_img);
                        ((HomeScreen) getActivity()).mFriendAddImg.setTag(chatConnectRes.getConnect_anon_status().equalsIgnoreCase(AppConstants.SUCCESS_CODE) ? 0 : 1);
                        if (mCFriendSubStr.equals(AppConstants.CHAT_SUB_FRIEND) && !isCheckDBDataBool) {
                            getDataFromDB(mUserDetailsRes.getUser_id(), mCFriendIDStr);
//                        } else if (mChatHandler != null) {
                        } else {

                            if (mConnectAPICountInt < 6 && AppConstants.CHAT_DISCOVER.equals(AppConstants.SUCCESS_CODE)) {
                                setDiscoverChat();
                            }
                            stopChatConnectAPICall();
                            startChatReceiveAPICall();
                        }

                    } else {
                        if (mConnectAPICountInt == 6) {

                            stopChatConnectAPICall();
                            mBaseFragDialog = DialogManager.getInstance().showOptionPopup(getActivity(), getString(R.string.no_users_avail), getString(R.string.try_again), getString(R.string.go_back), new InterfaceTwoBtnCallback() {
                                @Override
                                public void onYesClick() {
                                    mConnectAPICountInt = 1;
                                    startChatConnectAPICall();
                                }

                                @Override
                                public void onNoClick() {
                                    backPressed();
                                }
                            });
                        }

                    }
                } else {
                    if (mConnectAPICountInt == 6) {

                        stopChatConnectAPICall();
                        mBaseFragDialog = DialogManager.getInstance().showOptionPopup(getActivity(), getString(R.string.no_users_avail), getString(R.string.yes), getString(R.string.no), new InterfaceTwoBtnCallback() {
                            @Override
                            public void onYesClick() {
                                mConnectAPICountInt = 1;

                                startChatConnectAPICall();
                            }

                            @Override
                            public void onNoClick() {
                                backPressed();
                            }
                        });
                    }
                }
            }
            if (resObj instanceof ChatReceiveResponse) {
                //Receive Chat API Res
                ChatReceiveResponse chatChatReceiveRes = (ChatReceiveResponse) resObj;
                if (chatChatReceiveRes.getResponse_code().equalsIgnoreCase(AppConstants.SUCCESS_CODE)) {
                    if (chatChatReceiveRes.getExtra_response().getStatus().equalsIgnoreCase(AppConstants.FAILURE_CODE) || (chatChatReceiveRes.getExtra_response().getStatus().equalsIgnoreCase(AppConstants.SUCCESS_CODE) && chatChatReceiveRes.getExtra_response().getConnected_user_id().equals(mCFriendIDStr))) {
                        //Chat button disable
                        isBackPressedBool = true;
                        hideSoftKeyboard();
                        mChatEdt.setText("");
                        AppConstants.CHAT_SCREEN_TITLE = getString(R.string.user_disconnect);

                        setHintHeaderTxt(false, false, false);
                        mChatEdt.setFocusable(false);
                        mChatEdt.setFocusableInTouchMode(false);
                        mChatEdt.setClickable(false);
                        mSendBtn.setClickable(false);
                        ((HomeScreen) getActivity()).mHeaderInviteLay.setVisibility(View.GONE);
                        ((HomeScreen) getActivity()).mHeaderVisibleLay.setVisibility(View.GONE);
                        mSendBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rounded_grey_bg));


//                        ChatReceiveEntity deleteEntityRes = new ChatReceiveEntity();
//                        deleteEntityRes.setUser_id(GlobalMethods.getUserID(getActivity()));
//                        deleteEntityRes.setFriend_id(mCFriendIDStr);
//                        deleteEntityRes.setSubject(AppConstants.CHAT_SUB_FRIEND);
//                        ChatTable.deleteChatMessageList(deleteEntityRes);

                        mChatReceiveList = new ArrayList<>();
                        setChatListAdapter(mChatReceiveList);
                        GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECTED_STATUS, AppConstants.FAILURE_CODE);
                    } else {

                        ((HomeScreen) getActivity()).mHeaderInviteLay.setVisibility(chatChatReceiveRes.getExtra_response().getFriend().equalsIgnoreCase(AppConstants.FAILURE_CODE) || chatChatReceiveRes.getExtra_response().getFriend().equalsIgnoreCase(getResources().getString(R.string.four)) ? View.VISIBLE : View.GONE);
                        ((HomeScreen) getActivity()).mHeaderVisibleLay.setVisibility(chatChatReceiveRes.getExtra_response().getUser_connect_anonymous().equals(AppConstants.SUCCESS_CODE) ?
                                View.VISIBLE : View.INVISIBLE);
                        AppConstants.CONNECT_USER_ANONYMOUS = chatChatReceiveRes.getExtra_response().getUser_connect_anonymous();
                        AppConstants.CONNECT_FRIEND_ANONYMOUS = chatChatReceiveRes.getExtra_response().getFriend_connect_anonymous();

                        if (AppConstants.CONNECT_USER_ANONYMOUS.equals(AppConstants.SUCCESS_CODE) && AppConstants.CONNECT_FRIEND_ANONYMOUS.equals(AppConstants.SUCCESS_CODE)) {
                            isAnonymousBool = true;
                        }
                        checkAnonymousAdapterData();

                        setHeaderTxt();
                        mFriendDetailsRes.setFriend(chatChatReceiveRes.getExtra_response().getFriend());
                        mFriendDetailsRes.setMain_picture(chatChatReceiveRes.getExtra_response().getFriend_main_picture());
                        mFriendDetailsRes.setMore_mages(chatChatReceiveRes.getExtra_response().getFriend_more_mages());
                        mFriendDetailsRes.setAddress(chatChatReceiveRes.getExtra_response().getFriend_address());
                        mFriendDetailsRes.setHide_location(chatChatReceiveRes.getExtra_response().getFriend_location());
                        mFriendDetailsRes.setInterests(chatChatReceiveRes.getExtra_response().getFriend_interests());
                        AppConstants.OTHER_USER_DETAILS = new Gson().toJson(mFriendDetailsRes, UserDetailsEntity.class);

                        ((HomeScreen) getActivity()).mFriendAddImg.setImageResource(chatChatReceiveRes.getExtra_response().getFriend_connect_anonymous().equalsIgnoreCase(AppConstants.SUCCESS_CODE) ? R.drawable.friend_add_close_img : R.drawable.friend_add_img);
                        ((HomeScreen) getActivity()).mFriendAddImg.setTag(chatChatReceiveRes.getExtra_response().getFriend_connect_anonymous().equalsIgnoreCase(AppConstants.SUCCESS_CODE) ? 0 : 1);

                        if (chatChatReceiveRes.getResult().size() > 0 && !(AppConstants.CHAT_ID.equals(chatChatReceiveRes.getResult().get(chatChatReceiveRes.getResult().size() - 1).getChat_id()))) {
                            isSendPressedBool = false;
                            AppConstants.CHAT_ID = chatChatReceiveRes.getResult().get(chatChatReceiveRes.getResult().size() - 1).getChat_id();
                            setDataToDB(chatChatReceiveRes.getResult());
                        }
                        if (chatChatReceiveRes.getExtra_response().getFriend().equals(getString(R.string.three))) {
                            ((HomeScreen) getActivity()).mHeaderVisibleLay.setVisibility(View.GONE);
                            AppConstants.CHAT_BACK_PRESSED = getResources().getString(R.string.two);
                            getActivity().onBackPressed();
                        } else if (!chatChatReceiveRes.getExtra_response().getFriend().equals(AppConstants.FAILURE_CODE)) {
                            ((HomeScreen) getActivity()).mHeaderVisibleLay.setVisibility(View.GONE);

                        }
                    }
                    if (!chatChatReceiveRes.getExtra_response().getFriend().equals(getActivity().getResources().getString(R.string.three)) && mCFriendSubStr.equals(AppConstants.CHAT_SUB_FRIEND)) {

                        DialogManager.getInstance().showAlertPopup(getActivity(), getActivity().getResources().getString(R.string.user_del_msg));
                        getActivity().onBackPressed();

                    }

                }

            }
            if (resObj instanceof CommonResponse) {
                CommonResponse chatDisConnectRes = (CommonResponse) resObj;
                if (chatDisConnectRes.getResponse_code().equalsIgnoreCase(AppConstants.SUCCESS_CODE)) {

                    if (isAddAPIPressedBool) {
                        isAddAPIPressedBool = false;
                        DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.friend_req_sent));
                    } else {
                        AppConstants.CONNECT_FRIEND_ANONYMOUS = AppConstants.FAILURE_CODE;
                        stopChatReceiveAPICall();
//                        ChatReceiveEntity deleteEntityRes = new ChatReceiveEntity();
//                        deleteEntityRes.setUser_id(GlobalMethods.getUserID(getActivity()));
//                        deleteEntityRes.setFriend_id(mCFriendIDStr);
//                        deleteEntityRes.setSubject(AppConstants.CHAT_SUB_FRIEND);
//                        ChatTable.deleteChatMessageList(deleteEntityRes);

//ddd
                        hideSoftKeyboard();
                        mChatEdt.setText("");

                        mChatEdt.setFocusable(false);
                        mChatEdt.setFocusableInTouchMode(false);
                        mChatEdt.setClickable(false);
                        mSendBtn.setClickable(false);
                        ((HomeScreen) getActivity()).mHeaderInviteLay.setVisibility(View.GONE);
                        ((HomeScreen) getActivity()).mHeaderVisibleLay.setVisibility(View.GONE);
                        mSendBtn.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rounded_grey_bg));
                        GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECTED_STATUS, AppConstants.FAILURE_CODE);
                        mConnectAPICountInt = 1;
                        if (isNextPressedBool) {

                            GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECT_SUB, GlobalMethods.getStringValue(getActivity(), AppConstants.CHAT_CONNECT_ORIGINAL_SUB));

                            isConnectedBool = false;
                            isSendPressedBool = false;
                            isBackPressedBool = false;
                            isAddAPIPressedBool = false;

                            isCheckDBDataBool = false;
                            setHintHeaderTxt(false, false, false);
                            AppConstants.CHAT_ID = AppConstants.FAILURE_CODE;
                            AppConstants.CHAT_SCREEN_TITLE = getString(search_user);
                            mChatReceiveList = new ArrayList<>();
                            setChatListAdapter(mChatReceiveList);

                            mCFriendSubStr = GlobalMethods.getStringValue(getActivity(), AppConstants.CHAT_CONNECT_ORIGINAL_SUB);
                            GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECT_SUB, mCFriendSubStr);

                            mConnectAPICountInt = 1;
                            stopChatReceiveAPICall();
                            startChatConnectAPICall();
                        } else {
                            AppConstants.CHAT_BACK_PRESSED = AppConstants.FAILURE_CODE;
                            if (AppConstants.CHAT_DISCOVER.equals(AppConstants.SUCCESS_CODE)) {
                                ((HomeScreen) getActivity()).resetFragmentStack(0);
                                ((HomeScreen) getActivity()).setFooterImg(1);
                                AppConstants.CHAT_DISCOVER = AppConstants.FAILURE_CODE;
                                ((HomeScreen) getActivity()).replaceFragment(new DiscoverFragment(), 0);

                            } else {
                                getActivity().onBackPressed();
                            }
                        }
                    }
                } else {
                    DialogManager.getInstance().showAlertPopup(getActivity(), chatDisConnectRes.getMessage());
                }
            }
            if (resObj instanceof AnonymousResponse) {
                AnonymousResponse anonymousRes = (AnonymousResponse) resObj;
                if (anonymousRes.getResponse_code().equals(AppConstants.SUCCESS_CODE)) {
                    DialogManager.getInstance().showAlertPopup(getActivity(), getActivity().getResources().getString(R.string.you_revealed));

                }
            }
            if (resObj instanceof CommonModel) {
                CommonModel ignoreFriendRes = (CommonModel) resObj;
                    DialogManager.getInstance().showAlertPopup(getActivity(), ignoreFriendRes.getMessage());

            }
            setHeaderTxt();
        }
    }

    @Override
    public void onRequestFailure(Throwable t) {
        if (isAddAPIPressedBool) {
            isAddAPIPressedBool = false;
        }
        baseAlertDismiss();
        if (!isConnectedBool) {
            if (mConnectAPICountInt == 6) {
                stopChatConnectAPICall();
                mBaseFragDialog = DialogManager.getInstance().showOptionPopup(getActivity(), getString(R.string.no_users_avail), getString(R.string.try_again), getString(R.string.go_back), new InterfaceTwoBtnCallback() {
                    @Override
                    public void onYesClick() {
                        mConnectAPICountInt = 1;
                        startChatConnectAPICall();
                    }

                    @Override
                    public void onNoClick() {
                        backPressed();
                    }
                });
            }
        } else if (isSendPressedBool) {
            isSendPressedBool = false;
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
                model.setHeader_datetime(GlobalMethods.checkBetweentime(getActivity(), chatModel.get(i).getDatetime()));

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

            setHintHeaderTxt(false, false, false);
            setChatListAdapter(chatArrList);
        }
    }

    private void getDataFromDB(String userIdStr, String friendIdStr) {
        isCheckDBDataBool = true;
        ChatReceiveEntity model = new ChatReceiveEntity();
        model.setUser_id(userIdStr);
        model.setFriend_id(friendIdStr);
        model.setSubject(AppConstants.CHAT_SUB_FRIEND);

        ArrayList<ChatReceiveEntity> mMsgList = ChatTable.getChatMessageList(model);

        String chatIDStr = ChatTable.getChatMaxID(model);

        sysOut("chatIDStr  " + chatIDStr);
        if (!chatIDStr.isEmpty() && Integer.valueOf(chatIDStr) > 0) {
            AppConstants.CHAT_ID = chatIDStr;
        } else {
            AppConstants.CHAT_ID = AppConstants.FAILURE_CODE;
        }

        mChatReceiveList = new ArrayList<>();
        mChatListAdapter = null;
        setChatListAdapter(mMsgList);
        if (mChatListAdapter != null && mChatReceiveList != null && mChatReceiveList.size() > 0) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    mChatRecyclerView.smoothScrollToPosition(mChatReceiveList.size() - 1);
                    mChatRecyclerView.getLayoutManager().smoothScrollToPosition(mChatRecyclerView, null, mChatListAdapter.getItemCount() - 1);
                }
            });


        }
        startChatReceiveAPICall();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isFragFocusBool = false;
        stopChatConnectAPICall();
        stopChatReceiveAPICall();
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragFocusBool = false;
        stopChatConnectAPICall();
        stopChatReceiveAPICall();
    }


    private void stopChatReceiveAPICall() {
        if (mReceiveAPITimer != null) {
            mReceiveAPITimer.cancel();
            mReceiveAPITimer.purge();
        }
    }

    private void stopChatConnectAPICall() {
        if (mConnectAPITimer != null) {
            mConnectAPITimer.cancel();
            mConnectAPITimer.purge();
        }
    }


    private void setHeaders(UserDetailsEntity chatConnectRes) {

        AppConstants.CHAT_SCREEN_TITLE = chatConnectRes.getUsername();
        AppConstants.CONNECT_FRIEND_ANONYMOUS = chatConnectRes.getConnect_anon_status();
        setHeaderTxt();
        setBtnBackground();
        setHintHeaderTxt(chatConnectRes.getFriend().equals(getString(R.string.three)), chatConnectRes.getRandom().equals(AppConstants.SUCCESS_CODE), true);

        mCFriendSubStr = chatConnectRes.getFriend().equals(getString(R.string.three)) ? AppConstants.CHAT_SUB_FRIEND : mCFriendSubStr;
//                    ((HomeScreen) getActivity()).setHeaderLay(AppConstants.CHAT_IS_FRIEND.equals(AppConstants.SUCCESS_CODE) ? 1 : 2);
        ((HomeScreen) getActivity()).mHeaderInviteLay.setVisibility(chatConnectRes.getFriend().equalsIgnoreCase(AppConstants.FAILURE_CODE) || chatConnectRes.getFriend().equalsIgnoreCase(getResources().getString(R.string.four)) ? View.VISIBLE : View.GONE);
        ((HomeScreen) getActivity()).mHeaderVisibleLay.setVisibility(AppConstants.CONNECT_USER_ANONYMOUS.equals(AppConstants.SUCCESS_CODE) ?
                View.VISIBLE : View.INVISIBLE);
        if (chatConnectRes.getFriend().equals(getString(R.string.three))) {
            ((HomeScreen) getActivity()).mHeaderVisibleLay.setVisibility(View.GONE);
        } else if (!chatConnectRes.getFriend().equals(AppConstants.FAILURE_CODE)) {
            ((HomeScreen) getActivity()).mHeaderVisibleLay.setVisibility(View.GONE);

        }
        GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECTED_STATUS, getResources().getString(R.string.two));

        GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECT_SUB, mCFriendSubStr);
        GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECT_FRIEND_ID, mCFriendIDStr);
        GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECT_FRIEND_NAME, mCFriendNameStr);

        ((HomeScreen) getActivity()).mFriendAddImg.setImageResource(chatConnectRes.getConnect_anon_status().equalsIgnoreCase(AppConstants.SUCCESS_CODE) ? R.drawable.friend_add_close_img : R.drawable.friend_add_img);
        ((HomeScreen) getActivity()).mFriendAddImg.setTag(chatConnectRes.getConnect_anon_status().equalsIgnoreCase(AppConstants.SUCCESS_CODE) ? 0 : 1);

    }
}
