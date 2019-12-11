package com.example.projectresult.ui.home;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.example.projectresult.R;

public class HomeFragmentDirections {
  private HomeFragmentDirections() {
  }

  @NonNull
  public static NavDirections toCamera() {
    return new ActionOnlyNavDirections(R.id.to_camera);
  }
}
