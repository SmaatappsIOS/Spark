package com.smaat.spark.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.smaat.spark.R;
import com.smaat.spark.entity.inputEntity.ChatConnDisInputEntity;
import com.smaat.spark.entity.outputEntity.TrendsEntity;
import com.smaat.spark.main.BaseFragment;
import com.smaat.spark.model.CommonResponse;
import com.smaat.spark.model.TrendsResponse;
import com.smaat.spark.services.APIRequestHandler;
import com.smaat.spark.ui.HomeScreen;
import com.smaat.spark.utils.AppConstants;
import com.smaat.spark.utils.DialogManager;
import com.smaat.spark.utils.GlobalMethods;
import com.smaat.spark.utils.InterfaceBtnCallback;
import com.smaat.spark.utils.InterfaceTwoBtnCallback;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeFragment extends BaseFragment implements InterfaceBtnCallback {

    @BindView(R.id.topic_edt)
    EditText mTopicEdt;

    @BindView(R.id.topic_lay)
    LinearLayout mTopicLay;

    @BindView(R.id.scroll_view)
    HorizontalScrollView mScrollView;

    @BindView(R.id.search_dis_img)
    ImageView mSearchDisImg;

    @BindView(R.id.anonymous_mode_img)
    ImageView mAnonymousModeImg;

    @BindView(R.id.seek_bar_txt)
    TextView mSeekBarTxt;

    @BindView(R.id.seek_bar)
    SeekBar mSeekBar;

    @BindView(R.id.trending_flow_lay)
    FlowLayout mTrendingFlowLay;


    @BindView(R.id.seek_lay)
    LinearLayout mSeekLay;


    private ArrayList<String> mTopicStrArr;
    private ArrayList<TrendsEntity> mTrendsArrRes;

    private int mTopicSelectionInt = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_home_screen, container, false);
        ButterKnife.bind(this, rootView);
        setupUI(rootView);
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
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomeScreen) getActivity()).setHeaderLeftClick(true);
        ((HomeScreen) getActivity()).setHeadRigImgVisible(false, R.mipmap.app_icon);
        ((HomeScreen) getActivity()).setHeaderText(getString(R.string.connect));
        ((HomeScreen) getActivity()).setHeaderLay(1);


    }

    private void initView() {
        mTopicStrArr = new ArrayList<>();
        mTrendsArrRes = new ArrayList<>();

        mSearchDisImg.setTag(1);
        mAnonymousModeImg.setTag(1);
        AppConstants.CONNECT_USER_ANONYMOUS = AppConstants.FAILURE_CODE;


        //Set Default value to seek bar
        mSeekBar.setProgress(10);
        mSeekBarTxt.setText(String.valueOf(10) + " " + getString(R.string.miles));
        mSeekBar.setEnabled(false);
//        mSeekBar.setMax(500);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSeekBarTxt.setText(String.valueOf(progress) + " " + getString(R.string.miles));
                AppConstants.CHAT_DISTANCE = String.valueOf(progress);

            }
        });

        mTopicEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    edtFindText();
                }
                return true;
            }
        });

        mScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_BUTTON_PRESS:
                        mTopicEdt.setVisibility(View.VISIBLE);
                        mScrollView.setVisibility(View.GONE);
                        setTrendsAdapter();
                        break;
                }
                return true;
            }
        });


        mTopicEdt.addTextChangedListener(mInterestTextWatcher);
        trendsApiCall();

    }

    private void edtFindText() {
        String edtStr = mTopicEdt.getText().toString().trim();
        if (!edtStr.isEmpty()) {
            hideSoftKeyboard();
            String ede[] = edtStr.split("\\s+");

            List<String> trEdtStrList = new ArrayList<>(Arrays.asList(ede));
            LinkedHashSet<String> hs = new LinkedHashSet<>();
            hs.addAll(trEdtStrList);
            trEdtStrList.clear();
            trEdtStrList.addAll(hs);

            //Reset All Values
            mTopicStrArr = new ArrayList<>();
            for (int i = 0; i < mTrendsArrRes.size(); i++) {
                mTrendsArrRes.get(i).setTrend_selected(getString(R.string.no));
            }

            //Add
            for (int k = 0; k < trEdtStrList.size(); k++) {
                boolean isStrInBool = false;
                for (int j = 0; j < mTopicStrArr.size(); j++) {
                    if (trEdtStrList.get(k).trim().equalsIgnoreCase(mTopicStrArr.get(j).trim())) {
                        isStrInBool = true;
                        break;
                    }
                }
                if (!isStrInBool && !trEdtStrList.get(k).trim().isEmpty()) {
                    mTopicStrArr.add(trEdtStrList.get(k).trim());
                    for (int i = 0; i < mTrendsArrRes.size(); i++) {
                        for (int j = 0; j < mTopicStrArr.size(); j++) {
                            if (mTrendsArrRes.get(i).getName().trim().equalsIgnoreCase(mTopicStrArr.get(j).trim())) {
                                mTrendsArrRes.get(i).setTrend_selected(getString(R.string.yes));
                                break;
                            }
                        }
                    }
                }

            }

            String topicStr="";
            for (int n=0;n<mTopicStrArr.size();n++){
                topicStr=n==0?mTopicStrArr.get(n):topicStr+" "+mTopicStrArr.get(n);
            }

            mTopicEdt.setText(topicStr);

            mTopicEdt.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
        } else {
            for (int i = 0; i < mTrendsArrRes.size(); i++) {
                mTrendsArrRes.get(i).setTrend_selected(getString(R.string.no));
            }

            mTopicStrArr.clear();
        }
        setTrendsAdapter();
        setSelectedTrendsAdapter();
    }

    @OnClick({R.id.search_dis_img, R.id.anonymous_mode_img, R.id.scroll_view, R.id.topic_lay, R.id.connect_btn})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_dis_img:
                if (mSearchDisImg.getTag().equals(1)) {
                    mSearchDisImg.setTag(2);
                    mSeekBar.setEnabled(true);
                    mSeekLay.setVisibility(View.VISIBLE);
                    mSearchDisImg.setImageResource(R.drawable.distance_enable_img);
                } else {
                    mSearchDisImg.setTag(1);
                    mSeekBar.setProgress(0);
                    mSeekBar.setEnabled(false);
                    mSeekLay.setVisibility(View.INVISIBLE);
                    mSearchDisImg.setImageResource(R.drawable.distance_disable_img);
                }
                break;
            case R.id.anonymous_mode_img:
                if (mAnonymousModeImg.getTag().equals(1)) {
                    mAnonymousModeImg.setTag(2);
                    mAnonymousModeImg.setImageResource(R.drawable.distance_enable_img);
                    AppConstants.CONNECT_USER_ANONYMOUS = AppConstants.SUCCESS_CODE;
                } else {
                    mAnonymousModeImg.setTag(1);
                    mAnonymousModeImg.setImageResource(R.drawable.distance_disable_img);
                    mAnonymousModeImg.setImageResource(R.drawable.distance_disable_img);
                    AppConstants.CONNECT_USER_ANONYMOUS = AppConstants.FAILURE_CODE;
                }
                break;
            case R.id.topic_lay:
            case R.id.scroll_view:
                if (mTopicEdt.getVisibility() == View.VISIBLE) {
                    mTopicEdt.setVisibility(View.GONE);
                    mScrollView.setVisibility(View.VISIBLE);
                    setSelectedTrendsAdapter();
                } else {
                    mTopicEdt.setVisibility(View.VISIBLE);
                    mTopicEdt.setSelection(mTopicEdt.getText().toString().trim().length());
                    mScrollView.setVisibility(View.GONE);
                    setTrendsAdapter();
                }
                break;
            case R.id.connect_btn:
                String topicStr = mTopicEdt.getText().toString().trim();
                if (topicStr.isEmpty()) {
                    shakeAnimEdt(mTopicEdt);
                    DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.select_topic));
                } else {
                    checkChatConnectScreen();
                }
                break;
        }

    }

    private void checkChatConnectScreen() {
        if (!GlobalMethods.getStringValue(getActivity(), AppConstants.CHAT_CONNECTED_STATUS).equals(AppConstants.FAILURE_CODE) && !GlobalMethods.getStringValue(getActivity(), AppConstants.CHAT_CONNECTED_STATUS).isEmpty()) {
            DialogManager.getInstance().showOptionPopup(getActivity(), getResources().getString(R.string.already_connected), getResources().getString(R.string.end_chat), getResources().getString(R.string.cancel), new InterfaceTwoBtnCallback() {
                @Override
                public void onYesClick() {
                    callChatDisConnectAPI();
                }

                @Override
                public void onNoClick() {

                }
            });

        } else {

            for (int i = 0; i < mTrendsArrRes.size(); i++) {
                mTrendsArrRes.get(i).setTrend_selected(getString(R.string.no));
            }


            GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECT_SUB, GlobalMethods.setMsgEncoder(mTopicEdt.getText().toString()));
            GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECT_ORIGINAL_SUB, GlobalMethods.setMsgEncoder(mTopicEdt.getText().toString()));
            GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECT_FRIEND_ID, AppConstants.FAILURE_CODE);
            GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECT_FRIEND_NAME, AppConstants.FAILURE_CODE);
            mTopicEdt.setText("");
            mTopicStrArr.clear();
            mTopicLay.removeAllViews();
            setTrendsAdapter();
            mScrollView.setVisibility(View.GONE);
            AppConstants.CHAT_DISCOVER = AppConstants.FAILURE_CODE;
            AppConstants.CONNECT_FRIEND_ANONYMOUS = AppConstants.FAILURE_CODE;

            GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECTED_STATUS, AppConstants.SUCCESS_CODE);
            hideSoftKeyboard();
            ((HomeScreen) getActivity()).replaceFragment(new ChatConnectFragment(), 1);
        }
    }

    @Override
    public void onYesClick() {

    }

    private void trendsApiCall() {
        //Trends API Call
        ChatConnDisInputEntity trendsInputEntityRes = new ChatConnDisInputEntity(AppConstants.API_TRENDS, AppConstants.PARAMS_USER_ID, GlobalMethods.getUserID(getActivity()));
        APIRequestHandler.getInstance().callTrendsAPI(trendsInputEntityRes, this);
    }

    @Override
    public void onRequestSuccess(Object resObj) {
        if (resObj instanceof TrendsResponse) {
            TrendsResponse trendsRes = (TrendsResponse) resObj;
            if (trendsRes.getResponse_code().equalsIgnoreCase(AppConstants.SUCCESS_CODE)) {
                mTrendsArrRes = new ArrayList<>();
                mTrendsArrRes = trendsRes.getResult();
                setTrendsAdapter();
            } else {
                DialogManager.getInstance().showAlertPopup(getActivity(), trendsRes.getMessage());
            }

        } else if (resObj instanceof CommonResponse) {
            CommonResponse chatDisConnectRes = (CommonResponse) resObj;
            if (chatDisConnectRes.getResponse_code().equalsIgnoreCase(AppConstants.SUCCESS_CODE)) {
                GlobalMethods.storeStringValue(getActivity(), AppConstants.CHAT_CONNECTED_STATUS, AppConstants.FAILURE_CODE);
                AppConstants.CHAT_BACK_PRESSED = AppConstants.FAILURE_CODE;
                ((HomeScreen) getActivity()).resetFragmentStack(1);
                checkChatConnectScreen();
            } else {
                DialogManager.getInstance().showAlertPopup(getActivity(), chatDisConnectRes.getMessage());
            }
        }
    }


    private void setTrendsAdapter() {
        mTrendingFlowLay.removeAllViews();
        if (mTrendsArrRes.size() > 0) {
            for (int i = 0; i < mTrendsArrRes.size(); i++) {
                View trendsView = LayoutInflater.from(getActivity()).inflate(R.layout.adap_trends_view, null, false);
                final TextView trendsTxt = (TextView) trendsView.findViewById(R.id.trends_txt);
                trendsTxt.setText(mTrendsArrRes.get(i).getName());
                String tagStr = mTrendsArrRes.get(i).getTrend_selected();
                if (tagStr.equals(getString(R.string.yes))) {
                    trendsTxt.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.trending_select_bg));
                } else {
                    tagStr = getString(R.string.no);
                    mTrendsArrRes.get(i).setTrend_selected(tagStr);
                    trendsTxt.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.trending_un_select_bg));
                }
                trendsTxt.setTag(i);
                trendsTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int posInt = (Integer) view.getTag();
                        hideSoftKeyboard();
//                    if (mScrollView.getVisibility() == View.VISIBLE) {
//                        mTopicEdt.setVisibility(View.VISIBLE);
//                        mScrollView.setVisibility(View.GONE);
//                    }
                        if (mTopicStrArr.size() > 0 && mTrendsArrRes.get(posInt).getTrend_selected().equals(getString(R.string.yes))) {
                            for (int i = 0; i < mTopicStrArr.size(); i++) {

                                if (mTopicStrArr.get(i).equals(mTrendsArrRes.get(posInt).getName())) {
                                    mTopicStrArr.remove(i);
                                    mTrendsArrRes.get(posInt).setTrend_selected(getString(R.string.no));
                                    mTopicSelectionInt -= 1;
                                    trendsTxt.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.trending_un_select_bg));
                                }
                            }
                        } else if (mTopicSelectionInt < 5) {
                            mTopicStrArr.add(mTrendsArrRes.get(posInt).getName());
                            mTrendsArrRes.get(posInt).setTrend_selected(getString(R.string.yes));
                            mTopicSelectionInt += 1;
                            trendsTxt.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.trending_select_bg));
                        }

                        mTopicEdt.setVisibility(mTopicStrArr.size() > 0 ? View.GONE : View.VISIBLE);
                        mScrollView.setVisibility(mTopicStrArr.size() > 0 ? View.VISIBLE : View.GONE);
//                        setSelectedTrendsAdapter();
                        setEdtValues(mTopicStrArr);
                        edtFindText();

                    }
                });

                mTrendingFlowLay.addView(trendsView);
            }
        } else {
            mTopicEdt.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        }
    }


    private void setSelectedTrendsAdapter() {
        mTopicLay.removeAllViews();
        for (int pos = 0; pos < mTopicStrArr.size(); pos++) {
            final ViewGroup nullParent = null;
            View selectedView = LayoutInflater.from(getActivity()).inflate(R.layout.adap_selected_trends_view, nullParent);
            final TextView trendsTxt = (TextView) selectedView.findViewById(R.id.trends_txt);
            final TextView cancelTxt = (TextView) selectedView.findViewById(R.id.cancel_txt);
            trendsTxt.setText(mTopicStrArr.get(pos));
            cancelTxt.setTag(pos);

            cancelTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int posInt = (Integer) view.getTag();
                    boolean topIsInBool = false;

                    for (int i = 0; i < mTrendsArrRes.size(); i++) {
                        if (mTopicStrArr.get(posInt).equals(mTrendsArrRes.get(i).getName())) {
                            mTrendsArrRes.get(i).setTrend_selected(getString(R.string.no));
                            mTopicStrArr.remove(posInt);
                            topIsInBool = true;
                            if (mTopicStrArr.size() > 0) {
                                setSelectedTrendsAdapter();
                            } else {
                                mTopicLay.removeAllViews();
                                mTopicEdt.setVisibility(View.VISIBLE);
                                mScrollView.setVisibility(View.GONE);
                            }
                            setTrendsAdapter();
                            setEdtValues(mTopicStrArr);
                            break;
                        }

                    }
                    if (!topIsInBool) {
                        mTopicStrArr.remove(posInt);
                        if (mTopicStrArr.size() > 0) {
                            setSelectedTrendsAdapter();
                        } else {
                            mTopicLay.removeAllViews();
                            mTopicEdt.setVisibility(View.VISIBLE);
                            mScrollView.setVisibility(View.GONE);
                        }
                        setTrendsAdapter();
                        setEdtValues(mTopicStrArr);
                    }

                }
            });

            mTopicLay.addView(selectedView);
        }
    }

    private void setEdtValues(ArrayList<String> topicStrArr) {
        String topicStr = "";
        for (int i = 0; i < topicStrArr.size(); i++) {

            if (i == 0) {
                topicStr = topicStrArr.get(i);
            } else {
                topicStr += " " + topicStrArr.get(i);
            }
        }
        mTopicEdt.setText(topicStr);
    }

    private void callChatDisConnectAPI() {
        ChatConnDisInputEntity chatDisConInputEntityRes = new ChatConnDisInputEntity(AppConstants.API_CHAT_DISCONNECT, AppConstants.PARAMS_USER_ID, GlobalMethods.getUserID(getActivity()), AppConstants.FAILURE_CODE);
        APIRequestHandler.getInstance().callDisConnectAPI(chatDisConInputEntityRes, this);
    }

    TextWatcher mInterestTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String ede[] = charSequence.toString().split("\\s+");
            mTopicSelectionInt = ede.length;

            if (ede.length > 4) {
                final char c = charSequence.toString().charAt(charSequence.length() - 1);
                if (c == ' ') {
                    DialogManager.getInstance().showAlertPopup(getActivity(), getResources().getString(R.string.max_topic));
                    mTopicEdt.removeTextChangedListener(mInterestTextWatcher);
                    String result = charSequence.toString().trim();
                    mTopicEdt.setText(result);
                    mTopicEdt.setSelection(result.length());
                    mTopicEdt.addTextChangedListener(mInterestTextWatcher);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
//    private class FourDigitCardFormatWatcher implements TextWatcher {
//
//
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before,
//                                  int count) {
////            String ede[] = s.toString().split("\\s+");
////            if (ede.length > 3) {
////                final char c = s.charAt(s.length() - 1);
////                if (c == ' ') {
////
////                    mTopicEdt.setText(s.toString().trim());
////                    mTopicEdt.setSelection(s.length());
////
////                }
////
////            }
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count,
//                                      int after) {
////            int wordsLength = countWords(s.toString());// words.length;
////            if (count == 0 && wordsLength >= 4) {
////                setCharLimit(mTopicEdt, mTopicEdt.getText().length());
////            } else {
//////                removeFilter(mTopicEdt);
////                setCharLimit(mTopicEdt, 1000);
////            }
//
////            String ede[] = s.toString().split("\\s+");
////            if (ede.length > 3) {
////                final char c = s.charAt(s.length() - 1);
////                if (c == ' '){
//////                    s.toString().trim();
////                    s.delete(s.length() - 1, s.length());
//////                    mTopicEdt.setText(mTopicEdt.getText().toString().trim());
//////                    mTopicEdt.setSelection(mTopicEdt.getText().toString().trim().length());
////                }
////            }
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {
//
//
//            String ede[] = s.toString().split("\\s+");
//            if (ede.length > 3) {
//                mTopicEdt.removeTextChangedListener(new FourDigitCardFormatWatcher());
//                String result = s.toString().trim();
//                if (!s.toString().equals(result)) {
//                    mTopicEdt.setText(result);
//                    mTopicEdt.setSelection(result.length());
//                    // alert the user
//                }
//
//
//                final char c = s.charAt(s.length() - 1);
//                if (c == ' '){
////                    s.toString().trim();
//                    s.delete(s.length() - 1, s.length());
////                    mTopicEdt.setText(mTopicEdt.getText().toString().trim());
////                    mTopicEdt.setSelection(mTopicEdt.getText().toString().trim().length());
//                }
//                mTopicEdt.addTextChangedListener(new FourDigitCardFormatWatcher());
//            }
//        }
//    }

}
