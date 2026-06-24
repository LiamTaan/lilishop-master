interface FormItemProps {
  /** 角色名称 */
  name: string;
  /** 是否默认角色 */
  defaultRole: boolean;
  /** 备注 */
  remark: string;
}
interface FormProps {
  formInline: FormItemProps;
}

export type { FormItemProps, FormProps };
