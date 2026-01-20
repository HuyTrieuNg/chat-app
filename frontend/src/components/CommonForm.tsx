import { useEffect, useImperativeHandle } from "react";
import { useForm, type FieldError, type FieldValues } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import type { z } from "zod";
import type { ZodType } from "zod";
import { CommonFormField } from "@/components/CommonFormField";
import type { CommonFormProps } from "@/types/formField";

export function CommonForm<T extends ZodType>({
  schema,
  fields,
  ref,
}: CommonFormProps<T>) {
  type FormData = z.infer<T>;

  const {
    register,
    formState: { errors },
    getValues,
    reset,
    trigger,
    control,
    setFocus,
  } = useForm<FieldValues>({
    // @ts-expect-error - Generic zod schema compatibility
    resolver: zodResolver(schema),
    defaultValues: fields.reduce<Record<string, unknown>>((acc, field) => {
      if (field.defaultValue !== undefined) {
        acc[field.name] = field.defaultValue;
      }
      return acc;
    }, {}),
  });

  useEffect(() => {
    if (fields.length > 0) {
      setFocus(fields[0].name);
    }
  }, [fields, setFocus]);

  useImperativeHandle(
    ref,
    () => ({
      getValues: () => getValues() as FormData,
      validate: async () => {
        return await trigger();
      },
      reset: () => reset(),
    }),
    [getValues, trigger, reset]
  );

  return (
    <form className="space-y-4">
      {fields.map((field) => {
        const fieldError = errors[field.name] as FieldError | undefined;

        return (
          <CommonFormField
            key={field.name}
            field={field}
            register={register(field.name)}
            control={control}
            error={fieldError}
          />
        );
      })}
    </form>
  );
}
