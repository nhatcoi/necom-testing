import NotifyUtils from './NotifyUtils';
import { showNotification } from '@mantine/notifications';

jest.mock('@mantine/notifications', () => ({
  showNotification: jest.fn(),
}));

describe('NotifyUtils', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should call showNotification for simple success message', () => {
    NotifyUtils.simpleSuccess('Saved successfully');

    expect(showNotification).toHaveBeenCalledTimes(1);
    expect(showNotification).toHaveBeenCalledWith(
      expect.objectContaining({
        title: 'Thông báo',
        message: 'Saved successfully',
        color: 'teal',
      })
    );
  });

  it('should call showNotification for failed message with red color', () => {
    NotifyUtils.simpleFailed('Failed to save');

    expect(showNotification).toHaveBeenCalledTimes(1);
    expect(showNotification).toHaveBeenCalledWith(
      expect.objectContaining({
        title: 'Thông báo',
        message: 'Failed to save',
        color: 'red',
      })
    );
  });
});
