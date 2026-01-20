import type z from "zod";
import type { ZodType } from "zod";

export type FieldType = "text" | "checkbox" | "password";

export interface FormFieldOption {
  name: string;
  label: string;
  type: FieldType;
  placeholder?: string;
  defaultValue?: string | boolean;
}

export interface CommonFormRefs<T> {
  getValues: () => z.infer<T>;
  validate: () => Promise<boolean>;
  reset: () => void;
}

export interface CommonFormProps<T extends ZodType> {
  schema: T;
  fields: FormFieldOption[];
  ref?: React.Ref<CommonFormRefs<T>>;
}
