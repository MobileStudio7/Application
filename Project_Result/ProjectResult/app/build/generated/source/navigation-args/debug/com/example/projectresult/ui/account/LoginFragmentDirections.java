package com.example.projectresult.ui.account;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.example.projectresult.Navigation2Directions;
import com.example.projectresult.R;

public class LoginFragmentDirections {
  private LoginFragmentDirections() {
  }

  @NonNull
  public static NavDirections toLoginForm() {
    return new ActionOnlyNavDirections(R.id.to_LoginForm);
  }

  @NonNull
  public static NavDirections toNavAccount() {
    return Navigation2Directions.toNavAccount();
  }
}
