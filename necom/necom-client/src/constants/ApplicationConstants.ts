const trimTrailingSlash = (value: string) => value.replace(/\/+$/, '');

class ApplicationConstants {
  static HOME_PATH = '';

  // If not provided, client uses same-origin backend (works for local Docker and reverse proxy setups).
  private static API_ORIGIN = trimTrailingSlash(process.env.REACT_APP_API_ORIGIN || window.location.origin);
  private static WS_ORIGIN = trimTrailingSlash(
    process.env.REACT_APP_WS_ORIGIN || ApplicationConstants.API_ORIGIN.replace(/^http/, 'ws')
  );

  static API_PATH = `${ApplicationConstants.API_ORIGIN}/api`;
  static CLIENT_API_PATH = `${ApplicationConstants.API_ORIGIN}/client-api`;
  static WEBSOCKET_PATH = `${ApplicationConstants.WS_ORIGIN}/ws`;

  static DEFAULT_TAX = 0.1;
  static DEFAULT_SHIPPING_COST = 0;

  static DEFAULT_CLIENT_CATEGORY_PAGE_SIZE = 9;
  static DEFAULT_CLIENT_SEARCH_PAGE_SIZE = 12;
  static DEFAULT_CLIENT_WISHLIST_PAGE_SIZE = 5;
  static DEFAULT_CLIENT_PREORDER_PAGE_SIZE = 5;
  static DEFAULT_CLIENT_USER_REVIEW_PAGE_SIZE = 5;
  static DEFAULT_CLIENT_PRODUCT_REVIEW_PAGE_SIZE = 5;
  static DEFAULT_CLIENT_NOTIFICATION_PAGE_SIZE = 5;
  static DEFAULT_CLIENT_ORDER_PAGE_SIZE = 5;
}

export default ApplicationConstants;
