interface FormItemProps {
  id?: string;
  /** 用于判断是`新增`还是`修改` */
  title: string;
  higherDeptOptions: Record<string, unknown>[];
  departmentId: string;
  nickname: string;
  username: string;
  password: string;
  phone: string;
  email: string;
  status: boolean;
  roleIds: string[];
  dept?: {
    id?: string;
    name?: string;
  };
  remark: string;
  avatar?: string;
  isSuper?: boolean;
  description?: string;
}
interface FormProps {
  formInline: FormItemProps;
}

interface RoleFormItemProps {
  username: string;
  nickname: string;
  /** 角色列表 */
  roleOptions: any[];
  /** 选中的角色列表 */
  ids: string[];
}
interface RoleFormProps {
  formInline: RoleFormItemProps;
}

export type { FormItemProps, FormProps, RoleFormItemProps, RoleFormProps };
