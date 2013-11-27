/**
 * 
 */
package com.hexairbot.hexmini;

/**
 * @author koupoo
 *
 */
public interface SettingsViewControllerDelegate {
	public void interfaceOpacityValueDidChange(float newValue);
	public void leftHandedValueDidChange(boolean isLeftHanded);
	public void accModeValueDidChange(boolean isAccMode);
	public void headfreeModeValueDidChange(boolean isHeadfree);
	public void aileronAndElevatorDeadBandValueDidChange(float newValue);
	public void rudderDeadBandValueDidChange(float newValue);
}
