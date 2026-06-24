interface FormItemProps {
  higherDeptOptions: Record<string, unknown>[];
  parentId: string;
  title: string;
  sortOrder: number;
}
interface FormProps {
  formInline: FormItemProps;
}

export type { FormItemProps, FormProps };
