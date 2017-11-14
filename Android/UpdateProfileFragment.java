package com.smaat.spark.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.smaat.spark.R;
import com.smaat.spark.entity.inputEntity.LoginRegResetInputEntity;
import com.smaat.spark.entity.outputEntity.UserDetailsEntity;
import com.smaat.spark.main.BaseFragment;
import com.smaat.spark.model.AddressResponse;
import com.smaat.spark.model.CommonResponse;
import com.smaat.spark.model.ImageResponse;
import com.smaat.spark.model.UserDetailsResponse;
import com.smaat.spark.services.APIRequestHandler;
import com.smaat.spark.ui.HomeScreen;
import com.smaat.spark.utils.AppConstants;
import com.smaat.spark.utils.DialogManager;
import com.smaat.spark.utils.GlobalMethods;
import com.smaat.spark.utils.InterfaceBtnCallback;
import com.smaat.spark.utils.ProfileImageSelectionUtil;

import org.apmem.tools.layouts.FlowLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.smaat.spark.utils.ProfileImageSelectionUtil.getCorrectOrientationImage;

public class UpdateProfileFragment extends BaseFragment implements InterfaceBtnCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    @BindView(R.id.user_name_edt)
    EditText mUserNameEdt;

    @BindView(R.id.address_edt)
    EditText mAddressEdt;

    @BindView(R.id.interests_edt)
    EditText mInterestsEdt;

    @BindView(R.id.email_edt)
    EditText mEmailEdt;

    @BindView(R.id.pwd_edt)
    EditText mPwdEdt;

    @BindView(R.id.edit_btn)
    Button mEditBtn;

    @BindView(R.id.user_img_pager)
    ViewPager mUserImgPager;

    @BindView(R.id.indicator_one_img)
    ImageView mIndicatorOneImg;

    @BindView(R.id.indicator_two_img)
    ImageView mIndicatorTwoImg;

    @BindView(R.id.indicator_three_img)
    ImageView mIndicatorThreeImg;

    @BindView(R.id.indicator_four_img)
    ImageView mIndicatorFourImg;

    @BindView(R.id.indicator_five_img)
    ImageView mIndicatorFiveImg;

    @BindView(R.id.parent_scroll_view)
    ScrollView mParentScrollView;

    @BindView(R.id.interests_scroll_lay)
    ScrollView mInterestsScrollView;

    @BindView(R.id.interests_flow_lay)
    FlowLayout mInterestsFlowLay;

    @BindView(R.id.change_profile_txt)
    TextView mChangeProfileTxt;

    private GoogleApiClient mGoogleApiClient;

    private UserDetailsEntity mUserDetailsRes;
    private int mPicRequestCameraInt = 1;
    private int mPicRequestGalleryInt = 2;
    private int mPicRequestCropInt = 3;
    private int mIndicatorPosInt = 0;
    private Dialog mPictureDialog;

    private ArrayList<String> mImageStrArrList;
    private ArrayList<String> mInterestsStrArr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_update_profile_screen, container, false);
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
    public void onResume() {
        super.onResume();
        initGoogleAPIClient();
        ((HomeScreen) getActivity()).setHeaderLeftClick(true);
        ((HomeScreen) getActivity()).setHeaderText(getString(R.string.my_profile));
        ((HomeScreen) getActivity()).setHeadRigImgVisible(true, R.drawable.settings_img);
        ((HomeScreen) getActivity()).setHeaderLay(1);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {

        mInterestsStrArr = new ArrayList<>();

        mUserDetailsRes = GlobalMethods.getUserDetailsRes(getActivity());
        setUserData(mUserDetailsRes);

        mEmailEdt.setClickable(false);
        mEmailEdt.setFocusableInTouchMode(false);

        mUserNameEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    hideSoftKeyboard();
                    updateUserName();
                }
                return true;
            }
        });
        mInterestsEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    hideSoftKeyboard();
                    edtFindText();
                }
                return true;

            }
        });
        mInterestsFlowLay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                boolean isBtnPressedBool = false;
                mParentScrollView.requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_BUTTON_PRESS:
                    case MotionEvent.ACTION_DOWN:
                        mInterestsEdt.setVisibility(View.VISIBLE);
                        mInterestsScrollView.setVisibility(View.GONE);
                        isBtnPressedBool = true;
                        break;
                }
                return isBtnPressedBool;
            }
        });

        mInterestsScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                boolean isBtnPressedBool = false;
                mInterestsScrollView.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_BUTTON_PRESS:
                        mInterestsEdt.setVisibility(View.VISIBLE);
                        mInterestsScrollView.setVisibility(View.GONE);
                        isBtnPressedBool = true;
                        break;
                }

                return isBtnPressedBool;
            }
        });
        ((HomeScreen) getActivity()).mHeaderRightBtnLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((HomeScreen) getActivity()).replaceFragment(new SettingsFragment(), 1);
            }
        });

        mInterestsEdt.addTextChangedListener(mInterestTextWatcher);
    }


    private String getCurrentLatLong() {

        String latlongStr = "";

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            DialogManager.showToast(getActivity(), getString(R.string.go_settings_per));

        } else {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                latlongStr = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
            }
        }
        return latlongStr;
    }

    @OnClick({R.id.change_profile_txt, R.id.edit_lay, R.id.edit_btn, R.id.location_lay, R.id.interests_lay, R.id.reset_lay, R.id.reset_btn, R.id.update_btn})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_profile_txt:
                showPictureUploadPopup(!mImageStrArrList.get(mIndicatorPosInt).equals(AppConstants.FAILURE_CODE));
                break;
            case R.id.edit_lay:
            case R.id.edit_btn:
                updateUserName();
                break;
            case R.id.location_lay:
                String addressURLStr = String.format(AppConstants.GET_ADDRESS_URL, getCurrentLatLong());
                APIRequestHandler.getInstance().callGetUserAddressAPI(addressURLStr, this);

                break;
            case R.id.interests_scroll_lay:
            case R.id.interests_lay:
                mInterestsEdt.setVisibility(View.VISIBLE);
                mInterestsScrollView.setVisibility(View.GONE);
                break;
            case R.id.reset_lay:
            case R.id.reset_btn:
                ((HomeScreen) getActivity()).replaceFragment(new ResetPwdFragment(), 1);
                break;
            case R.id.update_btn:
                validateFields();
                break;
        }


    }


    private void updateUserName(){
        if (mEditBtn.getText().toString().trim().equals(getString(R.string.edit))) {
            userEdtField(true);
            mUserNameEdt.setSelection(mUserNameEdt.getText().toString().trim().length());
        } else {
            String userNameStr = mUserNameEdt.getText().toString().trim();

            if (userNameStr.isEmpty()) {
                shakeAnimEdt(mUserNameEdt);
                DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.enter_name));
            } else if (userNameStr.equals(mUserDetailsRes.getUsername())) {
                userEdtField(false);
            } else {
                //Update UserName API Call
                LoginRegResetInputEntity loginRegResetInputEntity = new LoginRegResetInputEntity(AppConstants.API_CHANGE_NAME, AppConstants.PARAMS_CHANGE_NAME, GlobalMethods.getUserID(getActivity()), userNameStr, "");
                APIRequestHandler.getInstance().callUpdateUserNameAPI(loginRegResetInputEntity, this);
            }
        }
    }
    private void setUserData(UserDetailsEntity userDetailsRes) {

        mUserNameEdt.setText(userDetailsRes.getUsername());
        mAddressEdt.setText(userDetailsRes.getAddress());
        if (!userDetailsRes.getInterests().isEmpty()) {
            mInterestsEdt.setText(userDetailsRes.getInterests());
            edtFindText();
        } else {
            mInterestsEdt.setVisibility(View.VISIBLE);
            mInterestsScrollView.setVisibility(View.GONE);
        }
        mEmailEdt.setText(userDetailsRes.getEmail_id());
        mPwdEdt.setText(userDetailsRes.getPassword());

        userEdtField(false);
        mPwdEdt.setClickable(false);
        mPwdEdt.setFocusable(false);
        mPwdEdt.setFocusableInTouchMode(false);


        if (mAddressEdt.getText().toString().trim().isEmpty()) {
            mAddressEdt.setText(getString(R.string.not_ava));
        }

        mImageStrArrList = new ArrayList<>();

//        mImageStrArrList.add(userDetailsRes.getMain_picture().trim().isEmpty() ? AppConstants.FAILURE_CODE : userDetailsRes.getMain_picture());

        if (!userDetailsRes.getMain_picture().trim().isEmpty()) {
            mImageStrArrList.add(userDetailsRes.getMain_picture());
        }

        if (!userDetailsRes.getMore_mages().trim().isEmpty()) {

            String moreImagesList[] = userDetailsRes.getMore_mages().trim().split(",");

            Collections.addAll(mImageStrArrList, moreImagesList);
        }

        int imageSize = mImageStrArrList.size();
        for (int i = imageSize; i < 5; i++) {

            mImageStrArrList.add(AppConstants.FAILURE_CODE);

        }
        if (mImageStrArrList.size() > 0) {
            mUserImgPager.setAdapter(new UserImagesPager(getActivity(), mImageStrArrList));
            setImgTxt(0);
        }

        mUserImgPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mIndicatorPosInt = position;
                setImgTxt(position);
                mIndicatorOneImg.setImageResource(position == 0 ? R.drawable.circle_sky_blue_bg : R.drawable.circle_gray_bg);
                mIndicatorTwoImg.setImageResource(position == 1 ? R.drawable.circle_sky_blue_bg : R.drawable.circle_gray_bg);
                mIndicatorThreeImg.setImageResource(position == 2 ? R.drawable.circle_sky_blue_bg : R.drawable.circle_gray_bg);
                mIndicatorFourImg.setImageResource(position == 3 ? R.drawable.circle_sky_blue_bg : R.drawable.circle_gray_bg);
                mIndicatorFiveImg.setImageResource(position == 4 ? R.drawable.circle_sky_blue_bg : R.drawable.circle_gray_bg);
                mChangeProfileTxt.setText(mImageStrArrList.get(position).equals(AppConstants.FAILURE_CODE) ? getString(R.string.add_pic) : getString(R.string.change_pic));

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mUserImgPager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mUserImgPager.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        mUserImgPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                // do transformation here
                page.startAnimation(mShakeAnimation);
            }
        });
    }


    private void initGoogleAPIClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(UpdateProfileFragment.this)
                    .addOnConnectionFailedListener(UpdateProfileFragment.this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

    }

    private void userEdtField(boolean isEnable) {
        mUserNameEdt.setClickable(isEnable);
        mUserNameEdt.setFocusable(isEnable);
        mUserNameEdt.setFocusableInTouchMode(isEnable);
        mEditBtn.setText(isEnable ? getString(R.string.update) : getString(R.string.edit));
        if (isEnable)
            mUserNameEdt.requestFocus();
    }


    String ImageName = "";
    File file;


    private void validateFields() {
        String userNameStr = mUserNameEdt.getText().toString().trim();
        String addressStr = mAddressEdt.getText().toString().trim();
        String interestsStr = mInterestsEdt.getText().toString().trim();
        String emailStr = mEmailEdt.getText().toString().trim();
        String pwdStr = mPwdEdt.getText().toString().trim();

        if (userNameStr.isEmpty()) {
            shakeAnimEdt(mUserNameEdt);
            DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.enter_name));
        } else if (addressStr.isEmpty()) {
            shakeAnimEdt(mAddressEdt);
            DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.enter_loc));
//        } else if (interestsStr.isEmpty()) {
//            shakeAnimEdt(mInterestsEdt);
//            DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.enter_interest), this);
        } else if (emailStr.isEmpty() || (!GlobalMethods.isEmailValid(emailStr))) {
            shakeAnimEdt(mEmailEdt);
            DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.enter_email));
        } else if (pwdStr.isEmpty()) {
            shakeAnimEdt(mPwdEdt);
            DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.enter_pwd));
        } else {
            //Register User API Call
            String latlangStrArr[] = getCurrentLatLong().split(",");
            String latStr = "", longStr = "";

            if (latlangStrArr.length == 2) {
                latStr = latlangStrArr[0];
                longStr = latlangStrArr[1];
            }
            if (addressStr.equals(getString(R.string.not_ava))) {
                addressStr = "";
            }
            String mainPicture = "", otherPictures = "";
            for (int i = 0; i < mImageStrArrList.size() && i < 5; i++) {

                if (!mImageStrArrList.get(i).equals(AppConstants.FAILURE_CODE)) {
                    if (i == 0) {
                        mainPicture = mImageStrArrList.get(i);
                    } else {
                        if (i == 1) {
                            otherPictures = mImageStrArrList.get(i);
                        } else {
                            otherPictures += "," + mImageStrArrList.get(i);
                        }
                    }
                }
            }

            LoginRegResetInputEntity loginRegResetInputEntity = new LoginRegResetInputEntity(AppConstants.API_UPDATE_PROFILE, AppConstants.PARAMS_UPDATE_PROFILE,
                    GlobalMethods.getUserID(getActivity()), emailStr, pwdStr, userNameStr, latStr, longStr, addressStr, interestsStr, "", mainPicture, otherPictures);
            APIRequestHandler.getInstance().callUpdateProfileAPI(loginRegResetInputEntity, this);
        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private void setImgTxt(int pos) {
        mChangeProfileTxt.setText(mImageStrArrList.get(pos).equals(AppConstants.FAILURE_CODE) ? getString(R.string.add_pic) : getString(R.string.change_pic));

    }

    @Override
    public void onRequestSuccess(Object resObj) {
        super.onRequestSuccess(resObj);
        if (resObj instanceof UserDetailsResponse) {

            UserDetailsResponse userRes = (UserDetailsResponse) resObj;
            if (userRes.getResponse_code().equalsIgnoreCase(AppConstants.SUCCESS_CODE)) {
                ArrayList<UserDetailsEntity> userDetails = userRes.getResult();
                userDetails.get(0).setUsername(mUserNameEdt.getText().toString().trim());
                userDetails.get(0).setEmail_id(mEmailEdt.getText().toString().trim());
                userDetails.get(0).setPassword(mPwdEdt.getText().toString().trim());
                GlobalMethods.storeUserDetails(getActivity(), userDetails.get(0));
                DialogManager.getInstance().showAlertPopup(getActivity(), getString(R.string.profile_updated));

            } else {
                DialogManager.getInstance().showAlertPopup(getActivity(), userRes.getMessage());
            }
        } else if (resObj instanceof ImageResponse) {
            ImageResponse mResponse = (ImageResponse) resObj;
            if (mResponse.getResponse_code().equalsIgnoreCase(AppConstants.SUCCESS_CODE)) {
                String url = mResponse.getResult().getPicture();
                mImageStrArrList.set(mIndicatorPosInt, url);
                checkProfileImg();

                setImgTxt(mIndicatorPosInt);

                mUserImgPager.setAdapter(new UserImagesPager(getActivity(), mImageStrArrList));
                mUserImgPager.setCurrentItem(mIndicatorPosInt);
            }
        }
        if (resObj instanceof CommonResponse) {
            CommonResponse userDetailRes = (CommonResponse) resObj;
            if (userDetailRes.getResponse_code().equals(AppConstants.SUCCESS_CODE)) {
                userEdtField(false);
            } else {
                DialogManager.getInstance().showAlertPopup(getActivity(), userDetailRes.getMessage());
            }

        } else if (resObj instanceof AddressResponse) {
            AddressResponse userAddressRes = (AddressResponse) resObj;

            String addStrArr[] = userAddressRes.getResults().get(0).getFormatted_address().split(",");
            String cityStr = addStrArr[addStrArr.length - 1], areaStr = addStrArr[addStrArr.length - 2];
            if (addStrArr.length > 4) {
                cityStr = addStrArr[addStrArr.length - 3];
                areaStr = addStrArr[addStrArr.length - 4];
            } else if (addStrArr.length > 3) {
                cityStr = addStrArr[addStrArr.length - 2];
                areaStr = addStrArr[addStrArr.length - 3];
            }

            mAddressEdt.setText(areaStr + ", " + cityStr);
        }
    }

    @Override
    public void onYesClick() {

    }

    private class UserImagesPager extends PagerAdapter {
        private Context mContext;
        private ArrayList<String> mUserStrArrList;

        private UserImagesPager(Context context, ArrayList<String> userList) {
            mContext = context;
            mUserStrArrList = userList;
        }

        @Override
        public int getCount() {
            if (mUserStrArrList.size() > 5) {
                return 5;
            }
            return mUserStrArrList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            ViewGroup rootViewGrp = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.adp_pager_img_view,
                    container, false);

            final ImageView profileImg = (ImageView) rootViewGrp.findViewById(R.id.profile_img);
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (mUserStrArrList.get(position).isEmpty() || mUserStrArrList.get(position).equals(AppConstants.FAILURE_CODE)) {
//                        profileImg.setImageResource(R.drawable.default_user_img);
                        Glide.with(mContext)
                                .load(R.drawable.default_user_img).asBitmap().into(profileImg);
                        profileImg.setVisibility(View.VISIBLE);
                    } else {
                        try {
                            Glide.with(mContext)
                                    .load(mUserStrArrList.get(position)).asBitmap().into(profileImg);
                            profileImg.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            Log.e(AppConstants.TAG, e.toString());
                            profileImg.setImageResource(R.drawable.default_user_img);
                        }
                    }
                }
            });



            profileImg.setTag(position);
            profileImg.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    showPictureUploadPopup(!mImageStrArrList.get(mIndicatorPosInt).equals(AppConstants.FAILURE_CODE));
                }
            });
//            profileImg.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    int posInt = (int) view.getTag();
//                    DialogManager.getInstance().showOriginalImgPopup(getActivity(), mUserStrArrList.get(posInt));
//                    return true;
//                }
//            });


            container.addView(rootViewGrp);
            return rootViewGrp;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout) object);
        }

    }


    Uri selectedImage = null;

    private void onSelectFromResult(Intent data, int selectmode) {
        Bitmap chooseImg;

        if (selectmode == mPicRequestGalleryInt) {
            if (data != null) {
                try {

                    chooseImg = ProfileImageSelectionUtil.getImage(data, getActivity());

                    selectedImage = data.getData();
                    chooseImg = getCorrectOrientationImage
                            (getActivity(), selectedImage, chooseImg);
                    selectedImage = getImageUri(getContext(), chooseImg);
                    cropCapturedImage(selectedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (selectmode == mPicRequestCameraInt) {
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + ImageName);
            try {
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), getImageContentUri(getActivity(), file));
                    Uri crop_uri = getImageUri(getActivity(),
                            getCorrectOrientationImage(getActivity(), getImageContentUri(getActivity(), file), bitmap));
                    cropCapturedImage(crop_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (ActivityNotFoundException aNFE) {
                //display an error message if user device doesn't support
                DialogManager.showToast(getActivity(), getString(R.string.device_not_support));
            }
        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (resultCode == -1) {
            if (requestCode == mPicRequestGalleryInt ||
                    requestCode == mPicRequestCameraInt) {
                onSelectFromResult(imageReturnedIntent, requestCode);
            } else if (requestCode == mPicRequestCropInt) {
                if (imageReturnedIntent != null) {
                    try {
                        String filePath = Environment.getExternalStorageDirectory()
                                + "/" + getString(R.string.app_name) + ".png";
                        APIRequestHandler.getInstance().callProfileImageUploadAPI(filePath, UpdateProfileFragment.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

            }
        }

    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, getString(R.string.app_name), null);
        return Uri.parse(path);
    }

    public void cropCapturedImage(Uri picUri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        //indicate image type and Uri
        cropIntent.setDataAndType(picUri, "image/*");
        //set crop properties
        cropIntent.putExtra("crop", "true");
        //indicate aspect of desired crop
//        cropIntent.putExtra("aspectX", 8);
//        cropIntent.putExtra("aspectY", 6);
//        cropIntent.putExtra("outputX", 800);
//        cropIntent.putExtra("outputY", 600);
        cropIntent.putExtra("return-data", false);

        File f = new File(Environment.getExternalStorageDirectory(),
                "/" + getString(R.string.app_name) + ".png");
        try {
            //noinspection ResultOfMethodCallIgnored
            f.createNewFile();

        } catch (IOException ex) {
            Log.e("io", ex.getMessage());
        }

        Uri uriOutput = Uri.fromFile(f);

        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriOutput);

        startActivityForResult(cropIntent, mPicRequestCropInt);
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        @SuppressLint("Recycle")
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public Dialog showPictureUploadPopup(boolean isRemoveImg) {

        if (mPictureDialog != null && mPictureDialog.isShowing()) {
            try {
                mPictureDialog.dismiss();
            } catch (Exception e) {
                Log.e(AppConstants.DIALOG_TAG, e.getMessage());
            }
        }
        mPictureDialog = new Dialog(getActivity());
        mPictureDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mPictureDialog.setContentView(R.layout.popup_photo_selection);
        if (mPictureDialog.getWindow() != null) {
            mPictureDialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            mPictureDialog.getWindow().setGravity(Gravity.CENTER);
            mPictureDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
        }
        WindowManager.LayoutParams layoutParams = mPictureDialog.getWindow().getAttributes();

        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

        Button cameraBtn, galleryBtn, removePicBtn, cancelBtn;

        cameraBtn = (Button) mPictureDialog.findViewById(R.id.camera_btn);
        galleryBtn = (Button) mPictureDialog.findViewById(R.id.gallery_btn);
        removePicBtn = (Button) mPictureDialog.findViewById(R.id.remove_btn);
        cancelBtn = (Button) mPictureDialog.findViewById(R.id.cancel_btn);

        removePicBtn.setVisibility(isRemoveImg ? View.VISIBLE : View.GONE);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPictureDialog.dismiss();
                Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String fileName = dateFormat.format(new Date());
                ImageName = getString(R.string.app_name) + "-" + fileName + ".jpg";
                file = new File(Environment.getExternalStorageDirectory() + File.separator + ImageName);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(takePicture, mPicRequestCameraInt);
            }
        });
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPictureDialog.dismiss();
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, mPicRequestGalleryInt);
            }
        });
        removePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageStrArrList.set(mIndicatorPosInt, AppConstants.FAILURE_CODE);
                checkProfileImg();
                setImgTxt(mIndicatorPosInt > 0 ? mIndicatorPosInt - 1 : 0);
                mUserImgPager.setAdapter(new UserImagesPager(getActivity(), mImageStrArrList));
                mUserImgPager.setCurrentItem(mIndicatorPosInt > 0 ? mIndicatorPosInt - 1 : 0);
                mPictureDialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPictureDialog.dismiss();
            }
        });

        if (mPictureDialog != null) {
            try {
                mPictureDialog.show();
            } catch (Exception e) {
                Log.e(AppConstants.DIALOG_TAG, e.getMessage());
            }
        }
        return mPictureDialog;
    }

    private void checkProfileImg() {

        ArrayList<String> imgStrArrList = new ArrayList<>();

        for (int i = 0; i < mImageStrArrList.size(); i++) {
            if (!mImageStrArrList.get(i).equals(AppConstants.FAILURE_CODE)) {
                imgStrArrList.add(mImageStrArrList.get(i));
            }
        }
        int imageSize = imgStrArrList.size();
        for (int i = imageSize; i < 5; i++) {
            imgStrArrList.add(AppConstants.FAILURE_CODE);
        }

        mImageStrArrList.clear();
        mImageStrArrList.addAll(imgStrArrList);
    }

    private void setSelectedTrendsAdapter() {
        if (mInterestsStrArr.size() > 0) {
            mInterestsFlowLay.removeAllViews();
            for (int pos = 0; pos < mInterestsStrArr.size(); pos++) {
                final ViewGroup nullParent = null;
                View selectedView = LayoutInflater.from(getActivity()).inflate(R.layout.adap_selected_pro_trends_view, nullParent);
                final TextView trendsTxt = (TextView) selectedView.findViewById(R.id.trends_txt);
                final TextView cancelTxt = (TextView) selectedView.findViewById(R.id.cancel_txt);
                trendsTxt.setText(mInterestsStrArr.get(pos));
                cancelTxt.setTag(pos);
                trendsTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mInterestsEdt.setVisibility(View.VISIBLE);
                        mInterestsScrollView.setVisibility(View.GONE);
                    }
                });
                cancelTxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int posInt = (Integer) view.getTag();

                        mInterestsStrArr.remove(posInt);
                        if (mInterestsStrArr.size() > 0) {
                            setSelectedTrendsAdapter();
                        } else {
                            mInterestsFlowLay.removeAllViews();
                            mInterestsEdt.setVisibility(View.VISIBLE);
                            mInterestsScrollView.setVisibility(View.GONE);
                        }
                        setEdtValues(mInterestsStrArr);


                    }
                });

                mInterestsFlowLay.addView(selectedView);
            }
        } else {
            mInterestsEdt.setVisibility(View.VISIBLE);
            mInterestsScrollView.setVisibility(View.GONE);
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
        mInterestsEdt.setText(topicStr);
    }

    private void edtFindText() {
        String edtStr = mInterestsEdt.getText().toString().trim();
        if (!edtStr.isEmpty()) {
            hideSoftKeyboard();
            String ede[] = edtStr.split("\\s+");

            List<String> trEdtStrList = new ArrayList<>(Arrays.asList(ede));
            LinkedHashSet<String> hs = new LinkedHashSet<>();
            hs.addAll(trEdtStrList);
            trEdtStrList.clear();
            trEdtStrList.addAll(hs);

            //Remove
            for (int j = 0; j < mInterestsStrArr.size(); j++) {
                boolean isTopStrInBool = false;
                for (int i = 0; i < trEdtStrList.size(); i++) {
                    if (mInterestsStrArr.get(j).trim().equalsIgnoreCase(trEdtStrList.get(i).trim())) {
                        isTopStrInBool = true;
                        break;
                    }
                }
                if (!isTopStrInBool) {
                    mInterestsStrArr.remove(j);
                }
            }

            //Add
            for (int k = 0; k < trEdtStrList.size(); k++) {
                boolean isStrInBool = false;
                for (int j = 0; j < mInterestsStrArr.size(); j++) {
                    if (trEdtStrList.get(k).trim().equalsIgnoreCase(mInterestsStrArr.get(j).trim())) {
                        isStrInBool = true;
                        break;
                    }
                }
                if (!isStrInBool && !trEdtStrList.get(k).trim().isEmpty()) {
                    mInterestsStrArr.add(trEdtStrList.get(k).trim());
                }

            }

            mInterestsEdt.setVisibility(View.GONE);
            mInterestsScrollView.setVisibility(View.VISIBLE);
        } else {

            mInterestsStrArr.clear();
        }
        setSelectedTrendsAdapter();
    }


    TextWatcher mInterestTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String ede[] = charSequence.toString().split("\\s+");
            if (ede.length > 9) {
                mInterestsEdt.removeTextChangedListener(mInterestTextWatcher);
                String result = charSequence.toString().trim();
                mInterestsEdt.setText(result);
                mInterestsEdt.setSelection(result.length());
                mInterestsEdt.addTextChangedListener(mInterestTextWatcher);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };


//    private void removeImage() {
//        int originalImgSize = 0;
//        for (int imgPos = 0; imgPos < mImageStrArrList.size(); imgPos++) {
//            originalImgSize += imgPos;
//            if (mImageStrArrList.get(imgPos).equals(AppConstants.FAILURE_CODE)) {
//                break;
//            }
//        }
//        sysOut("originalImgSize---" + originalImgSize);
//
//        if(mIndicatorPosInt==0){
//            if(mImageStrArrList.size()==5){
//                mImageStrArrList.add();
//            }
//
//        }
//
//    }
}
