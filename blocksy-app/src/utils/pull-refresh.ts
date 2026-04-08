import { computed, onBeforeUnmount, onMounted, ref } from "vue";

type PullStatus = "idle" | "pulling" | "ready" | "refreshing";

export function useH5PullRefresh(onRefresh: () => Promise<void> | void, threshold = 72) {
  const status = ref<PullStatus>("idle");
  const offset = ref(0);

  let startY = 0;
  let tracking = false;
  let refreshing = false;

  const text = computed(() => {
    if (status.value === "refreshing") {
      return "刷新中...";
    }
    if (status.value === "ready") {
      return "松手刷新";
    }
    if (status.value === "pulling") {
      return "下拉刷新";
    }
    return "";
  });

  function reset() {
    status.value = "idle";
    offset.value = 0;
    startY = 0;
    tracking = false;
  }

  function onTouchStart(event: TouchEvent) {
    if (refreshing) {
      return;
    }
    if (window.scrollY > 0) {
      return;
    }
    startY = event.touches[0]?.clientY ?? 0;
    tracking = true;
  }

  function onTouchMove(event: TouchEvent) {
    if (!tracking || refreshing) {
      return;
    }
    const y = event.touches[0]?.clientY ?? 0;
    const delta = y - startY;
    if (delta <= 0) {
      return;
    }
    offset.value = Math.min(delta * 0.6, 92);
    status.value = offset.value >= threshold ? "ready" : "pulling";
  }

  async function onTouchEnd() {
    if (!tracking || refreshing) {
      reset();
      return;
    }
    if (status.value !== "ready") {
      reset();
      return;
    }
    refreshing = true;
    status.value = "refreshing";
    offset.value = 54;
    try {
      await onRefresh();
    } finally {
      refreshing = false;
      reset();
    }
  }

  onMounted(() => {
    window.addEventListener("touchstart", onTouchStart, { passive: true });
    window.addEventListener("touchmove", onTouchMove, { passive: true });
    window.addEventListener("touchend", onTouchEnd, { passive: true });
  });

  onBeforeUnmount(() => {
    window.removeEventListener("touchstart", onTouchStart);
    window.removeEventListener("touchmove", onTouchMove);
    window.removeEventListener("touchend", onTouchEnd);
  });

  return {
    pullStatus: status,
    pullOffset: offset,
    pullText: text
  };
}
