function sleep(ms: number) {
  return new Promise<void>((resolve) => {
    setTimeout(resolve, ms);
  });
}

export async function withMinDuration<T>(task: Promise<T> | (() => Promise<T>), minDurationMs = 420): Promise<T> {
  const start = Date.now();
  try {
    const promise = typeof task === "function" ? task() : task;
    const result = await promise;
    const elapsed = Date.now() - start;
    if (elapsed < minDurationMs) {
      await sleep(minDurationMs - elapsed);
    }
    return result;
  } catch (error) {
    const elapsed = Date.now() - start;
    if (elapsed < minDurationMs) {
      await sleep(minDurationMs - elapsed);
    }
    throw error;
  }
}
