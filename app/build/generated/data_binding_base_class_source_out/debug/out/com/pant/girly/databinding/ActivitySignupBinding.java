// Generated by view binder compiler. Do not edit!
package com.pant.girly.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.pant.girly.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivitySignupBinding implements ViewBinding {
  @NonNull
  private final ScrollView rootView;

  @NonNull
  public final Button btnGetOtp;

  @NonNull
  public final Button btnSignUp;

  @NonNull
  public final CheckBox chkOtpVerification;

  @NonNull
  public final EditText etConfirmPassword;

  @NonNull
  public final EditText etEmail;

  @NonNull
  public final EditText etMobile;

  @NonNull
  public final EditText etOtp;

  @NonNull
  public final EditText etPassword;

  @NonNull
  public final ImageView imgLogo;

  @NonNull
  public final ImageView imgShowHideConfirmPassword;

  @NonNull
  public final ImageView imgShowHidePassword;

  @NonNull
  public final ScrollView scrollView;

  @NonNull
  public final TextView tvLogin;

  @NonNull
  public final TextView tvTitle;

  private ActivitySignupBinding(@NonNull ScrollView rootView, @NonNull Button btnGetOtp,
      @NonNull Button btnSignUp, @NonNull CheckBox chkOtpVerification,
      @NonNull EditText etConfirmPassword, @NonNull EditText etEmail, @NonNull EditText etMobile,
      @NonNull EditText etOtp, @NonNull EditText etPassword, @NonNull ImageView imgLogo,
      @NonNull ImageView imgShowHideConfirmPassword, @NonNull ImageView imgShowHidePassword,
      @NonNull ScrollView scrollView, @NonNull TextView tvLogin, @NonNull TextView tvTitle) {
    this.rootView = rootView;
    this.btnGetOtp = btnGetOtp;
    this.btnSignUp = btnSignUp;
    this.chkOtpVerification = chkOtpVerification;
    this.etConfirmPassword = etConfirmPassword;
    this.etEmail = etEmail;
    this.etMobile = etMobile;
    this.etOtp = etOtp;
    this.etPassword = etPassword;
    this.imgLogo = imgLogo;
    this.imgShowHideConfirmPassword = imgShowHideConfirmPassword;
    this.imgShowHidePassword = imgShowHidePassword;
    this.scrollView = scrollView;
    this.tvLogin = tvLogin;
    this.tvTitle = tvTitle;
  }

  @Override
  @NonNull
  public ScrollView getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivitySignupBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivitySignupBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_signup, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivitySignupBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnGetOtp;
      Button btnGetOtp = ViewBindings.findChildViewById(rootView, id);
      if (btnGetOtp == null) {
        break missingId;
      }

      id = R.id.btnSignUp;
      Button btnSignUp = ViewBindings.findChildViewById(rootView, id);
      if (btnSignUp == null) {
        break missingId;
      }

      id = R.id.chkOtpVerification;
      CheckBox chkOtpVerification = ViewBindings.findChildViewById(rootView, id);
      if (chkOtpVerification == null) {
        break missingId;
      }

      id = R.id.etConfirmPassword;
      EditText etConfirmPassword = ViewBindings.findChildViewById(rootView, id);
      if (etConfirmPassword == null) {
        break missingId;
      }

      id = R.id.etEmail;
      EditText etEmail = ViewBindings.findChildViewById(rootView, id);
      if (etEmail == null) {
        break missingId;
      }

      id = R.id.etMobile;
      EditText etMobile = ViewBindings.findChildViewById(rootView, id);
      if (etMobile == null) {
        break missingId;
      }

      id = R.id.etOtp;
      EditText etOtp = ViewBindings.findChildViewById(rootView, id);
      if (etOtp == null) {
        break missingId;
      }

      id = R.id.etPassword;
      EditText etPassword = ViewBindings.findChildViewById(rootView, id);
      if (etPassword == null) {
        break missingId;
      }

      id = R.id.imgLogo;
      ImageView imgLogo = ViewBindings.findChildViewById(rootView, id);
      if (imgLogo == null) {
        break missingId;
      }

      id = R.id.imgShowHideConfirmPassword;
      ImageView imgShowHideConfirmPassword = ViewBindings.findChildViewById(rootView, id);
      if (imgShowHideConfirmPassword == null) {
        break missingId;
      }

      id = R.id.imgShowHidePassword;
      ImageView imgShowHidePassword = ViewBindings.findChildViewById(rootView, id);
      if (imgShowHidePassword == null) {
        break missingId;
      }

      ScrollView scrollView = (ScrollView) rootView;

      id = R.id.tvLogin;
      TextView tvLogin = ViewBindings.findChildViewById(rootView, id);
      if (tvLogin == null) {
        break missingId;
      }

      id = R.id.tvTitle;
      TextView tvTitle = ViewBindings.findChildViewById(rootView, id);
      if (tvTitle == null) {
        break missingId;
      }

      return new ActivitySignupBinding((ScrollView) rootView, btnGetOtp, btnSignUp,
          chkOtpVerification, etConfirmPassword, etEmail, etMobile, etOtp, etPassword, imgLogo,
          imgShowHideConfirmPassword, imgShowHidePassword, scrollView, tvLogin, tvTitle);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
