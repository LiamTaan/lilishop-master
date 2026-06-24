interface FormItemProps {
  higherMenuOptions: Record<string, unknown>[];
  parentId: string;
  title: string;
  name: string;
  frontRoute: string;
  path: string;
  permission: string;
  sortOrder: number;
}
interface FormProps {
  formInline: FormItemProps;
}

export type { FormItemProps, FormProps };
