package com.example.projectresult.ui.account;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.example.projectresult.Navigation2Directions;
import com.example.projectresult.R;

public class AccountFragment2Directions {
  private AccountFragment2Directions() {
  }

  @NonNull
  public static NavDirections toAddUser() {
    return new ActionOnlyNavDirections(R.id.to_addUser);
  }

  @NonNull
  public static NavDirections toNavAccount() {
    return Navigation2Directions.toNavAccount();
  }
}
