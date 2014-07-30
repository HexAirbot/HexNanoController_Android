package com.vmc.ipc.service;

public interface OnIpcConnectChangedListener {
    public void OnIpcConnected();

    public void OnIpcDisConnected();

    public void onIpcPaused();

    public void onIpcResumed();
}
