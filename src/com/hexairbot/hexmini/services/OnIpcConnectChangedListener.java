package com.hexairbot.hexmini.services;

public interface OnIpcConnectChangedListener {
    public void OnIpcConnected();

    public void OnIpcDisConnected();

    public void onIpcPaused();

    public void onIpcResumed();
}
