import { http } from "@/utils/http";
import type { ResultMessage } from "./types";

export type SuperAdminMenuRecord = Record<string, any>;

function toFormBody(payload: Record<string, any>) {
  const body = new URLSearchParams();
  Object.entries(payload).forEach(([key, value]) => {
    if (value === undefined || value === null) return;
    body.append(key, String(value));
  });
  return body;
}

export const getSettingConfig = (key: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/setting/setting/get/${key}`
  );
};

export const saveSettingConfig = (key: string, data: Record<string, any>) => {
  return http.request<ResultMessage<null>>(
    "put",
    `/manager/setting/setting/put/${key}`,
    { data: JSON.stringify(data) }
  );
};

export const getStoreMenuList = (params?: Record<string, any>) => {
  return http.request<ResultMessage<SuperAdminMenuRecord[]>>(
    "get",
    "/manager/permission/storeMenu",
    { params }
  );
};

export const getStoreMenuTree = () => {
  return http.request<ResultMessage<SuperAdminMenuRecord[]>>(
    "get",
    "/manager/permission/storeMenu/tree"
  );
};

export const createStoreMenu = (data: Record<string, any>) => {
  return http.request<ResultMessage<SuperAdminMenuRecord>>(
    "post",
    "/manager/permission/storeMenu",
    { data }
  );
};

export const updateStoreMenu = (id: string, data: Record<string, any>) => {
  return http.request<ResultMessage<SuperAdminMenuRecord>>(
    "put",
    `/manager/permission/storeMenu/${id}`,
    { data }
  );
};

export const deleteStoreMenu = (ids: string[]) => {
  return http.request<ResultMessage<null>>(
    "delete",
    `/manager/permission/storeMenu/${ids.join(",")}`
  );
};

export const getRolePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/permission/role",
    { params }
  );
};

export const getPlatformMenuTree = () => {
  return http.request<ResultMessage<SuperAdminMenuRecord[]>>(
    "get",
    "/manager/permission/menu/tree"
  );
};

export const getRoleMenuList = (roleId: string) => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    `/manager/permission/roleMenu/${roleId}`
  );
};

export const saveRoleMenuList = (
  roleId: string,
  data: Array<Record<string, any>>
) => {
  return http.request<ResultMessage<null>>(
    "post",
    `/manager/permission/roleMenu/${roleId}`,
    { data }
  );
};

export const getDepartmentRoles = (departmentId: string) => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    `/manager/permission/departmentRole/${departmentId}`
  );
};

export const saveDepartmentRoles = (
  departmentId: string,
  data: Array<Record<string, any>>
) => {
  return http.request<ResultMessage<null>>(
    "put",
    `/manager/permission/departmentRole/${departmentId}`,
    { data }
  );
};

export const getUserRoleDetail = (userId: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/permission/userRole/${userId}`
  );
};

export const saveUserRoleDetail = (
  userId: string,
  data: Array<Record<string, any>>
) => {
  return http.request<ResultMessage<null>>(
    "put",
    `/manager/permission/userRole/${userId}`,
    { data }
  );
};

export const getMemberPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/passport/member",
    { params }
  );
};

export const getMemberDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/passport/member/${id}`
  );
};

export const createMember = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/passport/member",
    { data }
  );
};

export const updateMember = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    "/manager/passport/member",
    { data }
  );
};

export const updateMemberStatus = (memberIds: string[], disabled: boolean) => {
  return http.request<ResultMessage<boolean>>(
    "put",
    "/manager/passport/member/updateMemberStatus",
    { data: { memberIds, disabled } }
  );
};

export const updateMemberPoint = (
  memberId: string,
  point: number,
  type: string
) => {
  return http.request<ResultMessage<boolean>>(
    "put",
    "/manager/passport/member/updateMemberPoint",
    { data: { memberId, point, type } }
  );
};

export const getMemberGroupPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/member/memberGroup/getByPage",
    { params }
  );
};

export const getMemberGroupDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/member/memberGroup/get/${id}`
  );
};

export const createMemberGroup = (data: Record<string, any>) => {
  return http.request<ResultMessage<boolean>>(
    "post",
    "/manager/member/memberGroup",
    { data }
  );
};

export const updateMemberGroup = (id: string, data: Record<string, any>) => {
  return http.request<ResultMessage<boolean>>(
    "put",
    `/manager/member/memberGroup/update/${id}`,
    { data }
  );
};

export const deleteMemberGroup = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/member/memberGroup/delete/${id}`
  );
};

export const getMemberGroupUsers = (groupId: string, params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/member/memberGroup/${groupId}/users`,
    { params }
  );
};

export const addMemberGroupUsers = (groupId: string, memberIds: string[]) => {
  return http.request<ResultMessage<boolean>>(
    "post",
    `/manager/member/memberGroup/${groupId}/users`,
    { data: { memberIds } }
  );
};

export const removeMemberGroupUser = (groupId: string, memberId: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/member/memberGroup/${groupId}/user/${memberId}`
  );
};

export const getMemberBenefitPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/member/benefit/getByPage",
    { params }
  );
};

export const getMemberBenefitDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/member/benefit/get/${id}`
  );
};

export const createMemberBenefit = (data: Record<string, any>) => {
  return http.request<ResultMessage<boolean>>(
    "post",
    "/manager/member/benefit",
    { data }
  );
};

export const updateMemberBenefit = (id: string, data: Record<string, any>) => {
  return http.request<ResultMessage<boolean>>(
    "put",
    `/manager/member/benefit/update/${id}`,
    { data }
  );
};

export const deleteMemberBenefit = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/member/benefit/delete/${id}`
  );
};

export const updateMemberBenefitState = (id: string, state: string) => {
  return http.request<ResultMessage<boolean>>(
    "put",
    `/manager/member/benefit/state/${id}`,
    { data: { state } }
  );
};

export const getMemberBenefitTypes = () => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    "/manager/member/benefit/types"
  );
};

export const getMemberPointsHistoryPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/member/memberPointsHistory/getByPage",
    { params }
  );
};

export const getMemberPointsOverview = (memberId: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/member/memberPointsHistory/getMemberPointsHistoryVO",
    { params: { memberId } }
  );
};

export const getMemberPointsStatistics = () => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/member/memberPointsHistory/queryMemberPointsStatistics"
  );
};

export const getMemberEvaluationPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/member/evaluation/getByPage",
    { params }
  );
};

export const getMemberEvaluationDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/member/evaluation/get/${id}`
  );
};

export const updateMemberEvaluationStatus = (id: string, status: string) => {
  return http.request<ResultMessage<boolean>>(
    "get",
    `/manager/member/evaluation/updateStatus/${id}`,
    { params: { status } }
  );
};

export const updateMemberEvaluationTop = (id: string, top: boolean) => {
  return http.request<ResultMessage<boolean>>(
    "put",
    `/manager/member/evaluation/updateTop/${id}`,
    { params: { top } }
  );
};

export const deleteMemberEvaluation = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "put",
    `/manager/member/evaluation/delete/${id}`
  );
};

export const getMemberAddressListPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/member/address",
    { params }
  );
};

export const getMemberAddressPage = (
  memberId: string,
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/member/address/${memberId}`,
    { params }
  );
};

export const createMemberAddress = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/member/address",
    { data }
  );
};

export const updateMemberAddress = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    "/manager/member/address",
    { data }
  );
};

export const deleteMemberAddress = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/member/address/delById/${id}`
  );
};

export const getMemberNoticePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/message/memberNotice/page",
    { params }
  );
};

export const getMemberNoticeDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/message/memberNotice/${id}`
  );
};

export const readMemberNotice = (ids: string[]) => {
  return http.request<ResultMessage<boolean>>(
    "post",
    `/manager/message/memberNotice/read/${ids.join(",")}`
  );
};

export const readAllMemberNotice = () => {
  return http.request<ResultMessage<boolean>>(
    "post",
    "/manager/message/memberNotice/read/all"
  );
};

export const deleteMemberNotice = (ids: string[]) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/message/memberNotice/${ids.join(",")}`
  );
};

export const deleteAllMemberNotice = () => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    "/manager/message/memberNotice"
  );
};

export const getMemberNoticeLogPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/message/memberNoticeLog/getByPage",
    { params }
  );
};

export const getMemberNoticeLogDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/message/memberNoticeLog/get/${id}`
  );
};

export const deleteMemberNoticeLog = (ids: string[]) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/message/memberNoticeLog/delByIds/${ids.join(",")}`
  );
};

export const getMemberNoticeSenderPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/message/memberNoticeSenter/getByPage",
    { params }
  );
};

export const getMemberNoticeSenderDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/message/memberNoticeSenter/get/${id}`
  );
};

export const createMemberNoticeSender = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/message/memberNoticeSenter/insertOrUpdate",
    { data }
  );
};

export const deleteMemberNoticeSender = (ids: string[]) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/message/memberNoticeSenter/delByIds/${ids.join(",")}`
  );
};

export const getServiceNoticePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/message/serviceNotice/page",
    { params }
  );
};

export const getServiceNoticeDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/message/serviceNotice/${id}`
  );
};

export const createServiceNotice = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/message/serviceNotice",
    { data }
  );
};

export const updateServiceNotice = (id: string, data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    `/manager/message/serviceNotice/${id}`,
    { data }
  );
};

export const deleteServiceNotice = (ids: string[]) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/message/serviceNotice/${ids.join(",")}`
  );
};

export const getSmsSignPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/sms/sign/querySmsSignPage",
    { params }
  );
};

export const getSmsSignDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/sms/sign/${id}`
  );
};

export const createSmsSign = (data: Record<string, any>) => {
  return http.request<ResultMessage<boolean>>("post", "/manager/sms/sign", {
    params: data
  });
};

export const modifySmsSign = (data: Record<string, any>) => {
  return http.request<ResultMessage<boolean>>(
    "put",
    "/manager/sms/sign/modifySmsSign",
    { params: data }
  );
};

export const querySmsSignStatus = () => {
  return http.request<ResultMessage<boolean>>(
    "put",
    "/manager/sms/sign/querySmsSign"
  );
};

export const deleteSmsSign = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/sms/sign/${id}`
  );
};

export const getSmsTemplatePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/sms/template/querySmsTemplatePage",
    { params }
  );
};

export const createSmsTemplate = (data: Record<string, any>) => {
  return http.request<ResultMessage<boolean>>(
    "post",
    "/manager/sms/template",
    { params: data }
  );
};

export const modifySmsTemplate = (data: Record<string, any>) => {
  return http.request<ResultMessage<boolean>>(
    "put",
    "/manager/sms/template/modifySmsTemplate",
    { params: data }
  );
};

export const querySmsTemplateStatus = () => {
  return http.request<ResultMessage<boolean>>(
    "put",
    "/manager/sms/template/querySmsSign"
  );
};

export const deleteSmsTemplate = (templateCode: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    "/manager/sms/template",
    { params: { templateCode } }
  );
};

export const getSmsChannelPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/sms/sms",
    { params }
  );
};

export const getArticlePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/article/getByPage",
    { params }
  );
};

export const getArticleDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/other/article/${id}`
  );
};

export const createArticle = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/other/article",
    { data }
  );
};

export const updateArticle = (id: string, data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    `/manager/other/article/update/${id}`,
    { data }
  );
};

export const updateArticleStatus = (id: string, status: boolean) => {
  return http.request<ResultMessage<boolean>>(
    "put",
    `/manager/other/article/update/status/${id}`,
    { params: { status } }
  );
};

export const deleteArticle = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/other/article/delByIds/${id}`
  );
};

export const getArticleCategoryPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    "/manager/other/articleCategory/all-children",
    { params }
  );
};

export const getArticleCategoryDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/other/articleCategory/${id}`
  );
};

export const createArticleCategory = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/other/articleCategory",
    { data }
  );
};

export const updateArticleCategory = (
  id: string,
  data: Record<string, any>
) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    `/manager/other/articleCategory/update/${id}`,
    { data }
  );
};

export const deleteArticleCategory = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/other/articleCategory/${id}`
  );
};

export const getSensitiveWordsPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/sensitiveWords",
    { params }
  );
};

export const getSensitiveWordDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/other/sensitiveWords/${id}`
  );
};

export const createSensitiveWord = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/other/sensitiveWords",
    { data }
  );
};

export const updateSensitiveWord = (id: string, data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    `/manager/other/sensitiveWords/${id}`,
    { data }
  );
};

export const deleteSensitiveWord = (ids: string[]) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/other/sensitiveWords/delByIds/${ids.join(",")}`
  );
};

export const getCustomWordsPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/customWords/page",
    { params }
  );
};

export const createCustomWord = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/other/customWords",
    { params: data }
  );
};

export const updateCustomWord = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    "/manager/other/customWords",
    { params: data }
  );
};

export const deleteCustomWord = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/other/customWords/${id}`
  );
};

export const getSpecialPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/special",
    { params }
  );
};

export const getSpecialDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/other/special/${id}`
  );
};

export const createSpecial = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/other/special",
    { data }
  );
};

export const updateSpecial = (id: string, data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    `/manager/other/special/${id}`,
    { data }
  );
};

export const deleteSpecial = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/other/special/${id}`
  );
};

export const getAppVersionPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/appVersion",
    { params }
  );
};

export const createAppVersion = (data: Record<string, any>) => {
  return http.request<ResultMessage<boolean>>(
    "post",
    "/manager/other/appVersion",
    { data }
  );
};

export const updateAppVersion = (id: string, data: Record<string, any>) => {
  return http.request<ResultMessage<boolean>>(
    "put",
    `/manager/other/appVersion/${id}`,
    { data }
  );
};

export const deleteAppVersion = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/other/appVersion/${id}`
  );
};

export const getMessageChannelPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/message",
    { params }
  );
};

export const getMemberMessagePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/memberMessage",
    { params }
  );
};

export const getStoreMessagePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/storeMessage",
    { params }
  );
};

export const createMessageChannel = (data: Record<string, any>) => {
  return http.request<ResultMessage<boolean>>(
    "post",
    "/manager/other/message",
    { params: data }
  );
};

export const deleteMessageChannel = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/other/message/${id}`
  );
};

export const getFeedbackPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/feedback",
    { params }
  );
};

export const getFeedbackDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/other/feedback/${id}`
  );
};

export const getVerificationSourcePage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/verificationSource",
    { params }
  );
};

export const getVerificationSourceDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/other/verificationSource/${id}`
  );
};

export const createVerificationSource = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/other/verificationSource",
    { params: data }
  );
};

export const updateVerificationSource = (
  id: string,
  data: Record<string, any>
) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    `/manager/other/verificationSource/${id}`,
    { params: data }
  );
};

export const deleteVerificationSource = (ids: string[]) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/other/verificationSource/${ids.join(",")}`
  );
};

export const getVerificationRecordPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/verificationRecord",
    { params }
  );
};

export const getVerificationRecordSummary = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/verificationRecord/summary",
    { params }
  );
};

export const getVerificationExceptionPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/verificationRecord/exception",
    { params }
  );
};

export const getVerificationExceptionList = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    "/manager/other/verificationRecord/exception/list",
    { params }
  );
};

export const getVerificationExceptionSummary = (
  params?: Record<string, any>
) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/verificationRecord/exception/summary",
    { params }
  );
};

export const getLogisticsPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/other/logistics/getByPage",
    { params }
  );
};

export const getLogisticsDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/other/logistics/${id}`
  );
};

export const createLogistics = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/other/logistics",
    { params: data }
  );
};

export const updateLogistics = (id: string, data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    `/manager/other/logistics/${id}`,
    { params: data }
  );
};

export const deleteLogistics = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/other/logistics/${id}`
  );
};

export const getRegionRootList = () => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    "/manager/setting/region/item/0"
  );
};

export const getRegionAllCityTree = () => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    "/common/common/region/allCity"
  );
};

export const getRegionDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/setting/region/${id}`
  );
};

export const getRegionChildren = (id: string) => {
  return http.request<ResultMessage<Record<string, any>[]>>(
    "get",
    `/manager/setting/region/item/${id}`
  );
};

export const createRegion = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/setting/region/save",
    { params: data }
  );
};

export const updateRegion = (id: string, data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    `/manager/setting/region/${id}`,
    { params: data }
  );
};

export const deleteRegion = (ids: string[]) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/setting/region/${ids.join(",")}`
  );
};

export const syncRegion = (url: string) => {
  return http.request<ResultMessage<boolean>>(
    "post",
    "/manager/setting/region/sync",
    { params: { url } }
  );
};

export const getAfterSaleReasonPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/order/afterSaleReason/getByPage",
    { params }
  );
};

export const getAfterSaleReasonDetail = (id: string) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    `/manager/order/afterSaleReason/${id}`
  );
};

export const createAfterSaleReason = (data: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "post",
    "/manager/order/afterSaleReason",
    { params: data }
  );
};

export const updateAfterSaleReason = (
  id: string,
  data: Record<string, any>
) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "put",
    `/manager/order/afterSaleReason/update/${id}`,
    { params: data }
  );
};

export const deleteAfterSaleReason = (id: string) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/order/afterSaleReason/delByIds/${id}`
  );
};

export const getSettingLogPage = (params?: Record<string, any>) => {
  return http.request<ResultMessage<Record<string, any>>>(
    "get",
    "/manager/setting/log/getAllByPage",
    { params }
  );
};

export const deleteSettingLog = (ids: string[]) => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    `/manager/setting/log/${ids.join(",")}`
  );
};

export const deleteAllSettingLog = () => {
  return http.request<ResultMessage<boolean>>(
    "delete",
    "/manager/setting/log"
  );
};

export const getWxChannelsSetting = () => {
  return getSettingConfig("WX_CHANNELS");
};

export const saveWxChannelsSetting = (data: Record<string, any>) => {
  return saveSettingConfig("WX_CHANNELS", data);
};
