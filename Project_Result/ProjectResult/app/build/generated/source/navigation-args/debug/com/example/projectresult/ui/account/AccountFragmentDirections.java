package com.example.projectresult.ui.account;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.example.projectresult.R;

public class AccountFragmentDirections {
  private AccountFragmentDirections() {
  }

  @NonNull
  public static NavDirections toNestedLogin() {
    return new ActionOnlyNavDirections(R.id.to_nestedLogin);
  }

  @NonNull
  public static NavDirections toStatistic() {
    return new ActionOnlyNavDirections(R.id.to_statistic);
  }
}
