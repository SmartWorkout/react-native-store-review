export async function requestReview(): Promise<void> {
  throw new Error('In-app store reviews are not supported on web.');
}

export async function isAvailableAsync(): Promise<boolean> {
  return false;
}

/** Whether this module can start an in-app review flow (no configured store URL fallback). */
export async function hasAction(): Promise<boolean> {
  return isAvailableAsync();
}
