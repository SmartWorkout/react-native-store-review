export async function requestReview(): Promise<void> {
  throw new Error('In-app store reviews are not supported on web.');
}

export async function isAvailableAsync(): Promise<boolean> {
  return false;
}
