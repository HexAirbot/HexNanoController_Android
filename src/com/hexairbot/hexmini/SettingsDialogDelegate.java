package com.hexairbot.hexmini;

import com.hexairbot.hexmini.SettingsDialog;

public interface SettingsDialogDelegate
{
    public void prepareDialog(SettingsDialog dialog);
    public void onDismissed(SettingsDialog settingsDialog);
}
