import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { FormFieldOption } from "@/types/formField";
import {
  Controller,
  type Control,
  type FieldError,
  type UseFormRegisterReturn,
  type FieldValues,
} from "react-hook-form";

interface CommonFormFieldProps {
  field: FormFieldOption;
  register: UseFormRegisterReturn;
  control: Control<FieldValues>;
  error?: FieldError;
}

export const CommonFormField = ({
  field,
  register,
  control,
  error,
}: CommonFormFieldProps) => {
  const placeholder = field.placeholder || field.label;

  return (
    <div className="space-y-2">
      {field.type !== "checkbox" && (
        <Label htmlFor={register.name}>{field.label}</Label>
      )}

      {(field.type === "text" || field.type === "password") && (
        <Input
          id={register.name}
          type={field.type}
          placeholder={placeholder}
          {...register}
        />
      )}

      {field.type === "checkbox" && (
        <Controller
          name={register.name}
          control={control}
          render={({ field: controllerField }) => (
            <div className="flex items-center space-x-2">
              <Checkbox
                id={register.name}
                checked={!!controllerField.value}
                onCheckedChange={controllerField.onChange}
              />
              <Label
                htmlFor={register.name}
                className="text-sm font-normal cursor-pointer"
              >
                {field.label}
              </Label>
            </div>
          )}
        />
      )}

      {error && <p className="text-sm text-destructive">{error.message}</p>}
    </div>
  );
};
