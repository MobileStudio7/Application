package com.example.projectresult;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

public class Navigation2Directions {
  private Navigation2Directions() {
  }

  @NonNull
  public static NavDirections toNavAccount() {
    return new ActionOnlyNavDirections(R.id.to_navAccount);
  }
}
