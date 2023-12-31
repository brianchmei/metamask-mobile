import TestHelpers from '../../../../helpers';
import {
  BACK_ARROW_BUTTON_ID,
  CHANGE_PASSWORD_BUTTON_ID,
  CHANGE_PASSWORD_TITLE_ID,
  REVEAL_SECRET_RECOVERY_PHRASE_BUTTON_ID,
} from '../../../../../app/constants/test-ids';
import { SECURITY_PRIVACY_REMEMBER_ME_TOGGLE } from '../../../../../wdio/screen-objects/testIDs/Screens/SecurityPrivacy.testIds';

const SECURITY_SETTINGS_SCROLL_ID = 'security-settings-scrollview';
//const PRIVACY_MODE_SECTION_ID = 'privacy-mode-section';
const METAMETRICS_SWITCH_ID = 'metametrics-switch';
export default class SecurityAndPrivacy {
  static async tapRevealSecretRecoveryPhrase() {
    await TestHelpers.tap(REVEAL_SECRET_RECOVERY_PHRASE_BUTTON_ID);
  }

  static async tapChangePasswordButton() {
    await TestHelpers.tap(CHANGE_PASSWORD_BUTTON_ID);
  }

  static async tapBackButton() {
    await TestHelpers.tap(BACK_ARROW_BUTTON_ID);
  }

  static async scrollToChangePasswordView() {
    // Scroll to the bottom
    if (device.getPlatform() === 'android') {
      await TestHelpers.swipe(SECURITY_SETTINGS_SCROLL_ID, 'up', 'slow');
      await TestHelpers.delay(1000);
    } else {
      await TestHelpers.swipe(CHANGE_PASSWORD_TITLE_ID, 'up', 'slow', 0.2);
    }
  }

  static async scrollToBottomOfView() {
    // Scroll to the bottom
    if (device.getPlatform() === 'android') {
      await TestHelpers.swipe(SECURITY_SETTINGS_SCROLL_ID, 'up', 'fast');
      await TestHelpers.delay(1000);
    } else {
      await TestHelpers.swipe(SECURITY_SETTINGS_SCROLL_ID, 'up', 'fast', 0.9);
    }
    //await TestHelpers.swipe(PRIVACY_MODE_SECTION_ID, 'up', 'fast');
  }

  static async scrollToTurnOnRememberMe() {
    // Scroll to the bottom
    if (device.getPlatform() === 'android') {
      await TestHelpers.swipe(SECURITY_SETTINGS_SCROLL_ID, 'up', 'slow');
      await TestHelpers.delay(1000);
    } else {
      await TestHelpers.swipe(CHANGE_PASSWORD_TITLE_ID, 'up', 'slow', 0.6);
    }
    //await TestHelpers.swipe(PRIVACY_MODE_SECTION_ID, 'up', 'fast');
  }

  static async tapOKAlertButton() {
    await TestHelpers.tapAlertWithButton('OK');
  }

  static async tapMetaMetricsToggle() {
    await TestHelpers.tap(METAMETRICS_SWITCH_ID);
  }

  static async tapTurnOnRememberMeToggle() {
    await TestHelpers.tap(SECURITY_PRIVACY_REMEMBER_ME_TOGGLE);
  }

  static async isMetaMetricsToggleOn() {
    await TestHelpers.checkIfToggleIsOn(METAMETRICS_SWITCH_ID);
  }

  static async isMetaMetricsToggleOff() {
    await TestHelpers.checkIfToggleIsOff(METAMETRICS_SWITCH_ID);
  }

  static async isRememberMeToggleOn() {
    await TestHelpers.checkIfToggleIsOn(SECURITY_PRIVACY_REMEMBER_ME_TOGGLE);
  }

  static async isRememberMeToggleOff() {
    await TestHelpers.checkIfToggleIsOff(SECURITY_PRIVACY_REMEMBER_ME_TOGGLE);
  }

  static async isChangePasswordSectionVisible() {
    await TestHelpers.checkIfVisible(CHANGE_PASSWORD_TITLE_ID);
  }
}
