// Third party dependencies.
import { StyleSheet } from 'react-native';

// External dependencies.
import { Theme } from '../../../../util/theme/models';

/**
 * Style sheet function for AccountPermissionsRevoke screen.
 * @returns StyleSheet object.
 */
const styleSheet = (params: { theme: Theme }) => {
  const { colors } = params.theme;

  return StyleSheet.create({
    body: {
      paddingHorizontal: 16,
    },
    description: {
      marginVertical: 16,
      color: colors.text.alternative,
    },
    sheetActionContainer: {
      marginVertical: 16,
    },
    ctaButtonsContainer: {
      marginTop: 24,
      flexDirection: 'row',
    },
    button: { flex: 1 },
    buttonSeparator: {
      width: 16,
    },
    downCaretContainer: { justifyContent: 'center', flex: 1 },
    disconnectButton: { alignSelf: 'center' },
    permissionContainer: { flexDirection: 'row', alignItems: 'center' },
    securityContainer: {
      flexDirection: 'row',
      alignItems: 'center',
      marginVertical: 16,
    },
    permissionDescription: { marginLeft: 16 },
  });
};

export default styleSheet;
