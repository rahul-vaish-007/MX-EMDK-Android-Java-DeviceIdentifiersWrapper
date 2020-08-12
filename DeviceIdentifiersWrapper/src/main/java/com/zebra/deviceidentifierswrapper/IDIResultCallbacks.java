package com.zebra.deviceidentifierswrapper;

public interface IDIResultCallbacks {
    void onSuccess(final String message);
    void onError(final String message);
    void onDebugStatus(final String message);
}
