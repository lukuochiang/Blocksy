import { computed, ref, type Ref } from "vue";

export interface PagedListState<T> {
  page: Ref<number>;
  pageSize: number;
  total: Ref<number>;
  pagedItems: Ref<T[]>;
  resetPage: () => void;
  prevPage: () => void;
  nextPage: () => void;
}

export function usePagedList<T>(source: Ref<T[]>, pageSize: number): PagedListState<T> {
  const page = ref(1);
  const total = computed(() => source.value.length);
  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)));
  const pagedItems = computed(() => {
    const start = (page.value - 1) * pageSize;
    return source.value.slice(start, start + pageSize);
  });

  function resetPage() {
    page.value = 1;
  }

  function prevPage() {
    if (page.value <= 1) {
      return;
    }
    page.value -= 1;
  }

  function nextPage() {
    if (page.value >= totalPages.value) {
      return;
    }
    page.value += 1;
  }

  return {
    page,
    pageSize,
    total,
    pagedItems,
    resetPage,
    prevPage,
    nextPage
  };
}
