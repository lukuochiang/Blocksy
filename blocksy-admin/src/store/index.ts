import { defineStore } from "pinia";

export const useAdminStore = defineStore("admin", {
  state: () => ({
    token: localStorage.getItem("blocksy_admin_token") || "",
    username: localStorage.getItem("blocksy_admin_username") || ""
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token)
  },
  actions: {
    setAuth(token: string, username: string) {
      this.token = token;
      this.username = username;
      localStorage.setItem("blocksy_admin_token", token);
      localStorage.setItem("blocksy_admin_username", username);
    },
    logout() {
      this.token = "";
      this.username = "";
      localStorage.removeItem("blocksy_admin_token");
      localStorage.removeItem("blocksy_admin_username");
    }
  }
});
