/**
 * 
 */
package com.hexairbot.hexmini;


/**
 * @author koupoo
 *
 */
public interface SettingsViewControllerDelegate {

	public void leftHandedValueDidChange(boolean isLeftHanded);
	public void accModeValueDidChange(boolean isAccMode);
	public void beginnerModeValueDidChange(boolean isBeginnerMode);

	public void didConnect();
	public void didDisconnect();
	public void didFailToConnect();
	public void tryingToConnect(String target);
}
