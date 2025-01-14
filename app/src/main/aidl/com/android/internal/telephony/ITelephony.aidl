package com.android.internal.telephony;

 interface ITelephony {
  boolean endCall();

  void answerRingingCall();

  void silenceRinger();

  boolean enableDataConnectivity();

  boolean disableDataConnectivity();

  boolean isDataConnectivityPossible();
 }