/*
 * Copyright (c) 2020 Proton Technologies AG
 * 
 * This file is part of ProtonMail.
 * 
 * ProtonMail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ProtonMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ProtonMail. If not, see https://www.gnu.org/licenses/.
 */
package ch.protonmail.android.activities.fragments;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.OnClick;
import ch.protonmail.android.R;
import ch.protonmail.android.events.SendVerificationCodeEvent;
import ch.protonmail.android.events.Status;
import ch.protonmail.android.utils.extensions.CommonExtensionsKt;
import ch.protonmail.android.utils.extensions.TextExtensions;

/**
 * Created by dkadrikj on 1/20/16.
 */
public class HumanVerificationEmailDialogFragment extends HumanVerificationDialogFragment {

    private static final int MIN_CLICK_DIFF = 60000;

    @BindView(R.id.email_address)
    EditText mEmailAddress;
    @BindView(R.id.send_verification_code)
    Button mSendCode;
    @BindView(R.id.verification_code)
    EditText mVerificationCode;
    @BindView(R.id.verify)
    Button mVerify;

    @BindView(R.id.sending_email_circular)
    ProgressBar mSendingEmailProgress;
    @BindView(R.id.sending_email_icon)
    ImageView mSendingEmailIcon;
    @BindView(R.id.sending_email_description)
    TextView mSendingEmailDescription;

    private String token;

    public static HumanVerificationEmailDialogFragment newInstance(String token) {
        HumanVerificationEmailDialogFragment fragment = new HumanVerificationEmailDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARGUMENT_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.fragment_email_verification;
    }

    private boolean isValidCode() {
        String code = mVerificationCode.getText().toString();
        return !TextUtils.isEmpty(code);
    }

    private boolean isValidVerificationCode() {
        return !TextUtils.isEmpty(token);
    }

    long mSendVerificationClickedTimestamp = 0;

    @OnClick(R.id.send_verification_code)
    public void onSendVerificationCode() {
        long timestamp = System.currentTimeMillis();
        long diff = timestamp - mSendVerificationClickedTimestamp;
        if (diff < MIN_CLICK_DIFF) {
            TextExtensions.showToast(getContext(), String.format(getString(R.string.send_code_wait), ((int)
                    (MIN_CLICK_DIFF - diff) / 1000)));
            return;
        }
        mSendVerificationClickedTimestamp = timestamp;
        String email = mEmailAddress.getText().toString();
        if (CommonExtensionsKt.isValidEmail(email)) {
            mSendCode.setClickable(false);
            mSendingEmailProgress.setVisibility(View.VISIBLE);
//            mListener.sendVerificationCode(email, null);
        } else {
            TextExtensions.showToast(getContext(), R.string.invalid_email);
        }
    }

    @OnClick(R.id.verify)
    public void onVerifyCode() {
        if (!isValidCode()) {
            TextExtensions.showToast(getContext(), R.string.invalid_code);
            return;
        }
        token = mVerificationCode.getText().toString();
        if (isValidVerificationCode()) {
            mProgressBar.setVisibility(View.VISIBLE);
//            mListener.createUser(Constants.Token.EMAIL, token);
        }
    }

    @Subscribe
    public void onSendVerificationCodeEvent(SendVerificationCodeEvent event) {
        mSendingEmailProgress.setVisibility(View.GONE);
        if (event.getStatus() != null && event.getStatus() == Status.SUCCESS) {
            mSendingEmailIcon.setImageResource(R.drawable.ic_check_circle_black);
            mSendingEmailIcon.setColorFilter(0xFFA2C276, PorterDuff.Mode.SRC_IN);
            mSendingEmailDescription.setTextColor(getResources().getColor(R.color.green));
            mSendingEmailDescription.setText(getString(R.string.sending_email_success));
        } else {
            mSendingEmailIcon.setImageResource(R.drawable.ic_cancel_black);
            mSendingEmailIcon.setColorFilter(0xFFBF0000, PorterDuff.Mode.SRC_IN);
            mSendingEmailDescription.setTextColor(getResources().getColor(R.color.red));
            String error = getString(R.string.sending_email_failed);
            if (!TextUtils.isEmpty(event.getReason())) {
                error = event.getReason();
            }
            mSendingEmailDescription.setText(error);
        }
        mSendCode.setClickable(true);
        mSendCode.setText(getString(R.string.resend_verification_code));
    }
}
