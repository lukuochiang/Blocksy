import { defineStore } from "pinia";
import { getCurrentUser, getMyCommunities, selectMyCommunity, UserCommunityItem, UserProfile } from "../api/user";
import { getStorage, removeStorage, setStorage } from "../utils/storage";

export const useUserStore = defineStore("user", {
  state: () => ({
    token: "",
    profile: null as UserProfile | null,
    currentCommunityId: null as number | null,
    communities: [] as UserCommunityItem[]
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    displayName: (state) => state.profile?.nickname || state.profile?.username || ""
  },
  actions: {
    hydrate() {
      this.token = getStorage<string>("blocksy_token") || "";
      this.profile = getStorage<UserProfile>("blocksy_user");
      this.currentCommunityId =
        getStorage<number>("blocksy_current_community_id") || this.profile?.defaultCommunityId || null;
      this.communities = getStorage<UserCommunityItem[]>("blocksy_user_communities") || [];
    },
    setAuth(token: string) {
      this.token = token;
      setStorage("blocksy_token", token);
    },
    setProfile(profile: UserProfile) {
      this.profile = profile;
      setStorage("blocksy_user", profile);
      if (!this.currentCommunityId && profile.defaultCommunityId) {
        this.setCurrentCommunity(profile.defaultCommunityId);
      }
    },
    setCurrentCommunity(communityId: number | null) {
      this.currentCommunityId = communityId;
      if (communityId) {
        setStorage("blocksy_current_community_id", communityId);
      } else {
        removeStorage("blocksy_current_community_id");
      }
    },
    setCommunities(communities: UserCommunityItem[]) {
      this.communities = communities;
      setStorage("blocksy_user_communities", communities);
      const selected = communities.find((item) => item.isDefault);
      if (selected) {
        this.setCurrentCommunity(selected.communityId);
      }
    },
    async fetchMe() {
      const profile = await getCurrentUser();
      this.setProfile(profile);
      return profile;
    },
    async fetchCommunities() {
      const communities = await getMyCommunities();
      this.setCommunities(communities);
      return communities;
    },
    async changeCommunity(communityId: number) {
      await selectMyCommunity(communityId);
      this.setCurrentCommunity(communityId);
      this.communities = this.communities.map((item) => ({
        ...item,
        isDefault: item.communityId === communityId
      }));
      setStorage("blocksy_user_communities", this.communities);
    },
    logout() {
      this.token = "";
      this.profile = null;
      this.currentCommunityId = null;
      this.communities = [];
      removeStorage("blocksy_token");
      removeStorage("blocksy_user");
      removeStorage("blocksy_current_community_id");
      removeStorage("blocksy_user_communities");
    }
  }
});
