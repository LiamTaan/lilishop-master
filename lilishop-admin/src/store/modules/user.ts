import { defineStore } from "pinia";
import {
  type userType,
  store,
  router,
  resetRouter,
  routerArrays,
  storageLocal
} from "../utils";
import {
  type UserResult,
  type RefreshTokenResult,
  type UserInfo,
  getLogin,
  getMine,
  logoutApi,
  mobileLoginApi,
  normalizeUserInfo,
  refreshTokenApi
} from "@/api/user";
import { useMultiTagsStoreHook } from "./multiTags";
import { type DataInfo, setToken, removeToken, userKey } from "@/utils/auth";
import { isSuccessResult, unwrapResult } from "@/utils/result";

export const useUserStore = defineStore("pure-user", {
  state: (): userType => ({
    // 头像
    avatar: storageLocal().getItem<DataInfo<number>>(userKey)?.avatar ?? "",
    // 用户名
    username: storageLocal().getItem<DataInfo<number>>(userKey)?.username ?? "",
    // 昵称
    nickname: storageLocal().getItem<DataInfo<number>>(userKey)?.nickname ?? "",
    // 页面级别权限
    roles: storageLocal().getItem<DataInfo<number>>(userKey)?.roles ?? [],
    // 按钮级别权限
    permissions:
      storageLocal().getItem<DataInfo<number>>(userKey)?.permissions ?? [],
    // 判断登录页面显示哪个组件（0：登录（默认）、1：手机登录、4：忘记密码）
    currentPage: 0,
    // 是否勾选了登录页的免登录
    isRemembered: false,
    // 登录页的免登录存储几天，默认7天
    loginDay: 7
  }),
  actions: {
    /** 存储头像 */
    SET_AVATAR(avatar: string) {
      this.avatar = avatar;
    },
    /** 存储用户名 */
    SET_USERNAME(username: string) {
      this.username = username;
    },
    /** 存储昵称 */
    SET_NICKNAME(nickname: string) {
      this.nickname = nickname;
    },
    /** 存储角色 */
    SET_ROLES(roles: Array<string>) {
      this.roles = roles;
    },
    /** 存储按钮级别权限 */
    SET_PERMS(permissions: Array<string>) {
      this.permissions = permissions;
    },
    /** 更新个人资料 */
    UPDATE_PROFILE(profile: { avatar?: string; nickname?: string }) {
      this.avatar = profile.avatar ?? this.avatar;
      this.nickname = profile.nickname ?? this.nickname;
      const current = storageLocal().getItem<DataInfo<number>>(userKey) ?? {};
      storageLocal().setItem(userKey, {
        ...current,
        avatar: this.avatar,
        username: this.username,
        nickname: this.nickname,
        roles: this.roles,
        permissions: this.permissions
      });
    },
    /** 存储登录页面显示哪个组件 */
    SET_CURRENTPAGE(value: number) {
      this.currentPage = value;
    },
    /** 存储是否勾选了登录页的免登录 */
    SET_ISREMEMBERED(bool: boolean) {
      this.isRemembered = bool;
    },
    /** 设置登录页的免登录存储几天 */
    SET_LOGINDAY(value: number) {
      this.loginDay = Number(value);
    },
    /** 登入 */
    async loginByUsername(data) {
      return new Promise<UserResult>((resolve, reject) => {
        getLogin(data)
          .then(async response => {
            if (!isSuccessResult(response)) {
              reject(response.message);
              return;
            }
            const tokenData = unwrapResult(response);
            if (!tokenData?.accessToken || !tokenData?.refreshToken) {
              reject("登录返回的 token 数据为空");
              return;
            }
            setToken({
              accessToken: tokenData.accessToken,
              refreshToken: tokenData.refreshToken,
              expires: new Date(Date.now() + 24 * 60 * 60 * 1000)
            });
            const userInfoRes = await getMine();
            if (!isSuccessResult(userInfoRes)) {
              removeToken();
              reject(userInfoRes.message);
              return;
            }
            const userInfo: UserInfo = normalizeUserInfo(unwrapResult(userInfoRes));
            if (!userInfo?.username) {
              removeToken();
              reject("未获取到当前登录用户信息");
              return;
            }
            setToken({
              accessToken: tokenData.accessToken,
              refreshToken: tokenData.refreshToken,
              expires: new Date(Date.now() + 24 * 60 * 60 * 1000),
              avatar: userInfo.avatar,
              username: userInfo.username,
              nickname: userInfo.nickname,
              roles: [userInfo.isSuper ? "super" : "manager"],
              permissions: userInfo.isSuper ? ["*:*:*"] : []
            });
            resolve(response);
          })
          .catch(error => {
            removeToken();
            reject(error);
          });
      });
    },
    /** 手机号登录 */
    async loginByMobile(data) {
      return new Promise<UserResult>((resolve, reject) => {
        mobileLoginApi(data)
          .then(async response => {
            if (!isSuccessResult(response)) {
              reject(response.message);
              return;
            }
            const tokenData = unwrapResult(response);
            if (!tokenData?.accessToken || !tokenData?.refreshToken) {
              reject("登录返回的 token 数据为空");
              return;
            }
            setToken({
              accessToken: tokenData.accessToken,
              refreshToken: tokenData.refreshToken,
              expires: new Date(Date.now() + 24 * 60 * 60 * 1000)
            });
            const userInfoRes = await getMine();
            if (!isSuccessResult(userInfoRes)) {
              removeToken();
              reject(userInfoRes.message);
              return;
            }
            const userInfo: UserInfo = normalizeUserInfo(unwrapResult(userInfoRes));
            if (!userInfo?.username) {
              removeToken();
              reject("未获取到当前登录用户信息");
              return;
            }
            setToken({
              accessToken: tokenData.accessToken,
              refreshToken: tokenData.refreshToken,
              expires: new Date(Date.now() + 24 * 60 * 60 * 1000),
              avatar: userInfo.avatar,
              username: userInfo.username,
              nickname: userInfo.nickname,
              roles: [userInfo.isSuper ? "super" : "manager"],
              permissions: userInfo.isSuper ? ["*:*:*"] : []
            });
            resolve(response);
          })
          .catch(error => {
            removeToken();
            reject(error);
          });
      });
    },
    /** 登出 */
    logOut() {
      void logoutApi().catch(() => {
        // 后端登出失败时仍然执行前端清理，避免页面卡死在登录态
      });
      this.username = "";
      this.avatar = "";
      this.nickname = "";
      this.roles = [];
      this.permissions = [];
      removeToken();
      useMultiTagsStoreHook().handleTags("equal", [...routerArrays]);
      resetRouter();
      router.push("/login");
    },
    /** 刷新`token` */
    async handRefreshToken(data) {
      return new Promise<RefreshTokenResult>((resolve, reject) => {
        refreshTokenApi(data.refreshToken)
          .then(response => {
            if (isSuccessResult(response)) {
              const tokenData = unwrapResult(response);
              if (!tokenData?.accessToken || !tokenData?.refreshToken) {
                reject("刷新 token 返回为空");
                return;
              }
              setToken({
                ...tokenData,
                expires: new Date(Date.now() + 24 * 60 * 60 * 1000)
              });
              resolve({
                ...response,
                data: tokenData
              });
            } else {
              reject(response.message);
            }
          })
          .catch(error => {
            reject(error);
          });
      });
    }
  }
});

export function useUserStoreHook() {
  return useUserStore(store);
}
