package com.example.projectresult.ui.home;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;
import com.example.projectresult.R;

public class CameraFragmentDirections {
  private CameraFragmentDirections() {
  }

  @NonNull
  public static NavDirections toCamera() {
    return new ActionOnlyNavDirections(R.id.to_camera);
  }

  @NonNull
  public static NavDirections toHome() {
    return new ActionOnlyNavDirections(R.id.to_home);
  }
}
